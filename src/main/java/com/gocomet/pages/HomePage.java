package com.gocomet.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.microsoft.playwright.options.WaitUntilState;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Page Object for the CaseKaro home page (https://casekaro.com/).
 *
 * The top navigation ("Home", "Glass Cases", "Mobile Covers", "Reviews", ...) is
 * rendered TWICE in the DOM (once for the mobile drawer menu, once for the desktop
 * header), so any lookup by link text must filter down to the currently VISIBLE
 * instance before clicking, otherwise Playwright will time out trying to click a
 * hidden element.
 */
public class HomePage {

    private static final String BASE_URL = "https://casekaro.com";

    private final Page page;

    public HomePage(Page page) {
        this.page = page;
    }

    /**
     * Navigates to the CaseKaro home page and waits for the header to render.
     */
    public void navigateToHome() {
        page.navigate(BASE_URL, new Page.NavigateOptions()
                .setWaitUntil(WaitUntilState.LOAD)
                .setTimeout(90_000));

        page.waitForSelector("header", new Page.WaitForSelectorOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(15_000));
    }

    /**
     * Clicks a link identified by its visible text in the top navigation menu
     * (e.g. "Mobile Covers"). Only the first VISIBLE match is clicked, since the
     * same link text is duplicated in the hidden mobile nav markup.
     */
    public void clickTopNavLink(String linkText) {
        Locator matches = page.locator("a").filter(new Locator.FilterOptions().setHasText(linkText));

        assertThat(matches.count())
                .as("Top navigation should contain a link with text '%s'", linkText)
                .isGreaterThan(0);

        for (Locator link : matches.all()) {
            if (link.isVisible()) {
                link.scrollIntoViewIfNeeded();
                link.click();
                page.waitForLoadState(LoadState.LOAD);
                return;
            }
        }

        throw new AssertionError("No VISIBLE top navigation link found for text: '" + linkText + "'");
    }
}
