package com.gocomet.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Page Object for a product detail page on casekaro.com.
 */
public class ProductPage {

    // Scope to the product add-to-cart form
    private static final String PRODUCT_FORM = "form[action*='/cart/add'], .product-form, #product-form";
    // Cart drawer close button
    private static final String CART_DRAWER_CLOSE = "cart-drawer button[aria-label*='lose'], " +
                                                     ".cart-notification__close, " +
                                                     "button[aria-label='Close cart'], " +
                                                     "cart-drawer .icon-close";

    private final Page page;

    public ProductPage(Page page) {
        this.page = page;
    }

    /**
     * Waits for the product add-to-cart form and extracts material variant labels
     */
    public List<String> getMaterialVariantLabels() {
        page.waitForSelector(PRODUCT_FORM, new Page.WaitForSelectorOptions()
                .setState(WaitForSelectorState.ATTACHED)
                .setTimeout(20000));

        page.waitForTimeout(1500);

        List<String> labels = new ArrayList<>();
        Locator labelLocators = page.locator("form[action*='/cart/add'] fieldset label, .product-form__input label, .product-form fieldset label");
        for (Locator label : labelLocators.all()) {
            if (!label.isVisible()) {
                continue;
            }

            String text = readText(label);
            if (!text.isBlank()) {
                labels.add(text);
            }
        }

        if (labels.isEmpty()) {
            Locator radios = page.locator(PRODUCT_FORM + " input[type='radio'], input[type='radio']");
            for (Locator radio : radios.all()) {
                if (!radio.isVisible()) {
                    continue;
                }

                String value = radio.getAttribute("value");
                if (value != null && !value.isBlank()) {
                    labels.add(value.trim());
                }
            }
        }

        return labels;
    }

    /**
     * Asserts that Hard, Soft, and Glass are among the available material variants.
     * (Other variants like "Black Soft" and "Metal" may also be present — that is fine.)
     */
    public void assertThreeMaterialVariants() {
        List<String> labels = getMaterialVariantLabels();
        System.out.println("[INFO] Material variant labels on product page: " + labels);

        for (String required : Arrays.asList("Hard", "Soft", "Glass")) {
            assertThat(labels)
                    .as("Material variant '%s' should be available on the product page", required)
                    .anyMatch(label -> label.equalsIgnoreCase(required));
        }
    }

    /**
     * Selects the specified material variant and clicks "Add to cart".
     * Uses multiple strategies to locate and click the correct label.
     *
     * @param material "Hard", "Soft", or "Glass"
     */
    public void selectMaterialAndAddToCart(String material) {
        boolean clicked = clickMaterialLabel(material);

        assertThat(clicked)
                .as("Material variant '%s' should be selectable on the product page", material)
                .isTrue();

        page.waitForTimeout(1000);

        boolean addClicked = clickAddToCartButton();

        assertThat(addClicked)
                .as("Add to cart button should become clickable after selecting material '%s' (it might be out of stock)", material)
                .isTrue();

        Locator cartConfirmation = page.locator(
                "cart-drawer.active, cart-drawer.is-empty:not(.is-empty), .cart-notification.active, #cart-drawer.drawer--active");
        page.waitForTimeout(500);
        if (cartConfirmation.count() == 0) {
            System.out.println("[INFO] Cart drawer not detected immediately for material: " + material + " — falling back to fixed wait.");
            page.waitForTimeout(4500);
        }
        page.waitForTimeout(1000);

        closeCartDrawerIfOpen();
    }

    // ----------------------------------------------------------------
    // Private helpers
    // ----------------------------------------------------------------

    private void closeCartDrawerIfOpen() {
        Locator closeBtn = page.locator(CART_DRAWER_CLOSE);
        if (closeBtn.count() > 0) {
            for (Locator btn : closeBtn.all()) {
                if (btn.isVisible()) {
                    btn.click();
                    page.waitForTimeout(800);
                    return;
                }
            }
        }
        // Fallback: press Escape to dismiss any overlay
        page.keyboard().press("Escape");
        page.waitForTimeout(600);
    }

    private boolean clickMaterialLabel(String material) {
        Locator labels = page.locator("form[action*='/cart/add'] fieldset label, .product-form__input label, .product-form fieldset label");
        for (Locator label : labels.all()) {
            if (!label.isVisible()) {
                continue;
            }

            String text = readText(label);
            if (text.equalsIgnoreCase(material) || text.toLowerCase().contains(material.toLowerCase())) {
                label.click();
                return true;
            }
        }

        Locator radios = page.locator(PRODUCT_FORM + " input[type='radio'], input[type='radio']");
        for (Locator radio : radios.all()) {
            if (!radio.isVisible()) {
                continue;
            }

            String value = radio.getAttribute("value");
            if (value != null && value.equalsIgnoreCase(material)) {
                Locator associatedLabel = page.locator("label[for='" + radio.getAttribute("id") + "']");
                if (associatedLabel.count() > 0 && associatedLabel.first().isVisible()) {
                    associatedLabel.first().click();
                } else {
                    radio.click();
                }
                return true;
            }
        }

        return false;
    }

    private boolean clickAddToCartButton() {
        Locator addButtons = page.locator("form[action*='/cart/add'] button[name='add'], form[action*='/cart/add'] button[type='submit'], button.product-form__submit, #ProductSubmitButton, button:has-text('Add to cart')");
        for (Locator button : addButtons.all()) {
            if (button.isVisible() && button.isEnabled()) {
                button.click();
                return true;
            }
        }

        return false;
    }

    private String readText(Locator locator) {
        String text = locator.innerText();
        return text == null ? "" : text.trim();
    }
}
