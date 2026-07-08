package com.gocomet.config;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

public class PlaywrightManager {

    private static Playwright playwright;
    private static Browser browser;
    private static BrowserContext context;
    private static Page page;

    private PlaywrightManager() {
    }

    public static void initBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions()
                        .setHeadless(false)
        );
        context = browser.newContext(
                new Browser.NewContextOptions()
                        .setViewportSize(1400, 900)
        );
        context.setDefaultNavigationTimeout(90_000);
        context.setDefaultTimeout(60_000);
        page = context.newPage();
    }

    public static Page getPage() {
        return page;
    }

    public static void closeBrowser() {
        if (page != null) page.close();
        if (context != null) context.close();
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }
}
