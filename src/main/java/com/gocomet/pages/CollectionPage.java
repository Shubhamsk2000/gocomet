package com.gocomet.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;

import static org.assertj.core.api.Assertions.assertThat;


public class CollectionPage {

    private static final String PRODUCT_CARD   = "li.grid__item, .product-item, .card-wrapper";

    private final Page page;

    public CollectionPage(Page page) {
        this.page = page;
    }

    /**
     * Asserts the browser is on the iPhone 16 Pro collection (not Pro Max).
     */
    public void assertOnIPhone16ProCollection() {
        assertThat(page.url())
                .as("URL should point to iPhone 16 Pro collection – not Pro Max")
                .contains("iphone-16-pro-back-covers")
                .doesNotContain("iphone-16-pro-max");
    }

    /**
     * Navigates to the product detail page of the first product card.
     *
     * Strategy: work inside the first visible product card and click its "Choose Options"
     * action directly using locators.
     */
    public void clickChooseOptionsOnFirstCard() {
        page.waitForSelector("#product-grid, ul#product-grid, .collection .grid", new Page.WaitForSelectorOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(15000));

        Locator productCards = page.locator(PRODUCT_CARD);
        assertThat(productCards.count())
                .as("Collection page should contain at least one product card")
                .isGreaterThan(0);

        for (Locator card : productCards.all()) {
            if (!card.isVisible()) {
                continue;
            }

            Locator chooseOptions = card.locator("a, button").filter(new Locator.FilterOptions().setHasText("Choose Options"));
            for (Locator option : chooseOptions.all()) {
                if (option.isVisible()) {
                    option.click();
                    page.waitForLoadState(LoadState.LOAD);
                    assertThat(page.url())
                            .as("Should be on a product detail page after clicking the first card")
                            .contains("/products/");
                    return;
                }
            }

            Locator productLinks = card.locator("a[href*='/products/'], a.card__heading--link, a.card__heading, h3.card__heading a, .card__information a[href*='/products/']");
            for (Locator link : productLinks.all()) {
                if (link.isVisible()) {
                    link.click();
                    page.waitForLoadState(LoadState.LOAD);
                    assertThat(page.url())
                            .as("Should be on a product detail page after clicking the first card")
                            .contains("/products/");
                    return;
                }
            }
        }

        throw new AssertionError("Could not click a visible product card link on the collection page");
    }
}
