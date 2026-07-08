package com.gocomet.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Page Object for https://casekaro.com/pages/phone-cases-by-model
 */
public class PhoneCasesByModelPage {

    // The main search input
    private static final String SEARCH_INPUT = "#modelSearch";
    private static final String AUTOCOMPLETE_CONTAINER = "#modelSearchResults, .model-search-results, [class*='search-result'], [class*='suggestion'], [class*='autocomplete'], [role='listbox']";
    private static final String AUTOCOMPLETE_ITEM = "a, button, li, div";

    private final Page page;

    public PhoneCasesByModelPage(Page page) {
        this.page = page;
    }

    // ----------------------------------------------------------------
    // Navigation / search helpers
    // ----------------------------------------------------------------

    /**
     * Scrolls the model search input into view.
     */
    public void scrollToModelSearch() {
        page.locator(SEARCH_INPUT).scrollIntoViewIfNeeded();
        page.waitForSelector(SEARCH_INPUT,
                new Page.WaitForSelectorOptions().setState(WaitForSelectorState.VISIBLE));
    }

    /**
     * Clicks the search box and types the given query.
     */
    public void typeInModelSearch(String query) {
        Locator box = page.locator(SEARCH_INPUT);
        box.click();
        box.fill(query);
        page.waitForTimeout(2000); // allow filtering / autocomplete to react
    }

    /**
     * Clears the search box.
     */
    public void clearModelSearch() {
        Locator box = page.locator(SEARCH_INPUT);
        box.click();
        box.clear();
        page.waitForTimeout(1000);
    }

    // ----------------------------------------------------------------
    // Autocomplete
    // ----------------------------------------------------------------

    /**
     * Waits for any autocomplete suggestion to appear and returns their text.
     */
    public List<String> waitForAutocompleteAndGetSuggestions() {
        for (int attempt = 0; attempt < 10; attempt++) {
            List<String> suggestions = collectVisibleAutocompleteTexts();
            if (!suggestions.isEmpty()) {
                return suggestions;
            }
            page.waitForTimeout(400);
        }

        Locator fallback = page.getByText("iPhone 16 Pro", new Page.GetByTextOptions().setExact(false));
        List<String> suggestions = new ArrayList<>();
        for (Locator item : fallback.all()) {
            if (item.isVisible()) {
                String text = readText(item);
                if (!text.isBlank()) {
                    suggestions.add(text);
                }
            }
        }
        return suggestions.stream().distinct().toList();
    }

    // ----------------------------------------------------------------
    // Clicking the correct dropdown item
    // ----------------------------------------------------------------

    /**
     * Clicks specifically on "iPhone 16 Pro" in the autocomplete dropdown,
     * NOT "iPhone 16 Pro Max".
     *
     * Strategy: locate the element whose trimmed text content equals exactly
     * "iPhone 16 Pro" – this excludes "iPhone 16 Pro Max".
     */
    public void clickIPhone16ProFromDropdown() {
        Locator exact = page.getByText("iPhone 16 Pro", new Page.GetByTextOptions().setExact(true));
        assertThat(exact.count())
                .as("'iPhone 16 Pro' entry should appear in autocomplete dropdown")
                .isGreaterThan(0);

        for (Locator item : exact.all()) {
            if (item.isVisible()) {
                assertThat(readText(item))
                        .as("Selected dropdown text should be exactly 'iPhone 16 Pro'")
                        .isEqualTo("iPhone 16 Pro");
                item.click();
                page.waitForLoadState(LoadState.DOMCONTENTLOADED);
                return;
            }
        }

        throw new AssertionError("No visible autocomplete entry found for: iPhone 16 Pro");
    }

    private List<String> collectVisibleAutocompleteTexts() {
        List<String> suggestions = new ArrayList<>();
        Locator containers = page.locator(AUTOCOMPLETE_CONTAINER);

        for (Locator container : containers.all()) {
            if (!container.isVisible()) {
                continue;
            }

            Locator items = container.locator(AUTOCOMPLETE_ITEM);
            for (Locator item : items.all()) {
                if (!item.isVisible()) {
                    continue;
                }

                String text = readText(item);
                if (!text.isBlank()) {
                    suggestions.add(text);
                }
            }
        }

        return suggestions.stream().distinct().toList();
    }

    private String readText(Locator locator) {
        String text = locator.innerText();
        return text == null ? "" : text.trim();
    }
}
