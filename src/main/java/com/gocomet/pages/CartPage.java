package com.gocomet.pages;

import com.gocomet.model.CartItem;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class CartPage {

    // Scope ONLY to the main content, not the CartDrawer
    private static final String MAIN_CART_ITEM = "main tr.cart-item, #MainContent tr.cart-item, " +
                                                  "main .cart-item:not(#CartDrawer .cart-item), " +
                                                  "[id^='CartItem-']";

    private static final String CART_ITEM_NAME = "a.cart-item__name, .cart-item__details a";
    private static final String CART_ITEM_MATERIAL = ".product-option dt, dl dt";
    private static final String CART_ITEM_PRICE = ".price:not(.price--on-sale .price--compare), .cart-item__price-wrapper .price, .price, [class*='price']";

    private static final String BASE_URL = "https://casekaro.com";

    private final Page page;

    public CartPage(Page page) {
        this.page = page;
    }

    /**
     * Navigates directly to the cart page and waits for items to load.
     */
    public void openCart() {
        page.navigate(BASE_URL + "/cart", new Page.NavigateOptions()
                .setWaitUntil(com.microsoft.playwright.options.WaitUntilState.LOAD)
                .setTimeout(90_000));

        // Wait for a cart item that is NOT in the CartDrawer
        page.waitForSelector("main tr.cart-item, [id^='CartItem-'], main .cart-item",
                new Page.WaitForSelectorOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(15000));
    }

    /**
     * Returns the number of distinct line-item rows in the MAIN cart page (not the drawer).
     */
    public int getCartItemCount() {
        return (int) extractCartItems().size();
    }

    /**
     * Asserts the cart contains exactly {@code expectedCount} line items.
     */
    public void assertCartItemCount(int expectedCount) {
        List<CartItem> items = extractCartItems();
        assertThat(items.size())
                .as("Cart should contain exactly %d items but found %d", expectedCount, items.size())
                .isEqualTo(expectedCount);
    }

    /**
     * Asserts all given material names appear in at least one cart item's variant text.
     */
    public void assertMaterialsInCart(List<String> expectedMaterials) {
        List<CartItem> items = extractCartItems();
        List<String> materialLines = items.stream().map(CartItem::getMaterial).toList();

        System.out.println("[INFO] Variant lines found in cart: " + materialLines);

        for (String material : expectedMaterials) {
            assertThat(materialLines)
                    .as("Material '%s' should be present in cart variant lines", material)
                    .anyMatch(line -> line.toLowerCase().contains(material.toLowerCase()));
        }
    }

    /**
     * Extracts all cart items from the MAIN cart page using JavaScript.
     * Scoped to exclude CartDrawer items (which are hidden when on /cart page).
     */
    public List<CartItem> extractCartItems() {
        page.waitForTimeout(1000);

        List<CartItem> items = new ArrayList<>();
        Locator rows = page.locator(MAIN_CART_ITEM);
        for (Locator row : rows.all()) {
            if (!row.isVisible()) {
                continue;
            }

            String name = firstVisibleText(row.locator(CART_ITEM_NAME));
            String link = firstVisibleHref(row.locator(CART_ITEM_NAME));
            String material = extractMaterialFromRow(row);
            String price = firstVisibleText(row.locator(CART_ITEM_PRICE));

            if (material.isBlank()) {
                material = extractMaterialFromName(name);
            }

            items.add(new CartItem(name, material, price, link));
        }
        return items;
    }

    /**
     * Prints all cart item details to System.out (console).
     * Required by the assignment: material, price, link.
     */
    public void printCartItemDetails() {
        List<CartItem> items = extractCartItems();
        System.out.println("\n╔══════════════════════════════════════════════╗");
        System.out.println("║          CART ITEM DETAILS (Console)         ║");
        System.out.println("╚══════════════════════════════════════════════╝");
        for (int i = 0; i < items.size(); i++) {
            CartItem item = items.get(i);
            System.out.printf("%n  ── Item %d ──%n", i + 1);
            System.out.printf("  Product  : %s%n", item.getProductName());
            System.out.printf("  Material : %s%n", item.getMaterial());
            System.out.printf("  Price    : %s%n", item.getPrice());
            System.out.printf("  Link     : %s%n", item.getProductLink());
        }
        System.out.println("\n════════════════════════════════════════════════\n");
    }

    // ----------------------------------------------------------------
    // Private helpers
    // ----------------------------------------------------------------

    private String extractMaterialFromName(String name) {
        String lower = name.toLowerCase();
        if (lower.contains("glass")) return "Glass";
        if (lower.contains("soft"))  return "Soft";
        if (lower.contains("hard"))  return "Hard";
        return "Unknown";
    }

    private String extractMaterialFromRow(Locator row) {
        Locator dts = row.locator(CART_ITEM_MATERIAL);
        for (Locator dt : dts.all()) {
            if (!dt.isVisible()) {
                continue;
            }

            if (readText(dt).equalsIgnoreCase("material")) {
                Locator dd = dt.locator("xpath=following-sibling::dd[1]");
                for (Locator value : dd.all()) {
                    if (value.isVisible()) {
                        String text = readText(value);
                        if (!text.isBlank()) {
                            return text;
                        }
                    }
                }
            }
        }

        Locator fallbackValues = row.locator(".product-option dd, [class*='product-option'] dd");
        for (Locator value : fallbackValues.all()) {
            if (value.isVisible()) {
                String text = readText(value);
                if (!text.isBlank()) {
                    return text;
                }
            }
        }

        return "";
    }

    private String firstVisibleText(Locator locators) {
        for (Locator locator : locators.all()) {
            if (!locator.isVisible()) {
                continue;
            }

            String text = readText(locator);
            if (!text.isBlank()) {
                return text;
            }
        }

        return "";
    }

    private String firstVisibleHref(Locator locators) {
        for (Locator locator : locators.all()) {
            if (!locator.isVisible()) {
                continue;
            }

            String href = locator.getAttribute("href");
            if (href != null && !href.isBlank()) {
                return href.startsWith("http") ? href : BASE_URL + href;
            }
        }

        return BASE_URL + "/cart";
    }

    private String readText(Locator locator) {
        String text = locator.innerText();
        return text == null ? "" : text.trim();
    }
}
