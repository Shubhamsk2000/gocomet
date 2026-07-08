package com.gocomet.steps;

import com.gocomet.config.PlaywrightManager;
import com.gocomet.pages.CartPage;
import com.gocomet.pages.CollectionPage;
import com.gocomet.pages.HomePage;
import com.gocomet.pages.PhoneCasesByModelPage;
import com.gocomet.pages.ProductPage;
import com.microsoft.playwright.Page;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Step definitions for casekaro_shopping.feature.
 * Each scenario gets its own browser instance (see @Before / @After).
 */
public class CaseKaroSteps {

    private Page page;
    private HomePage homePage;
    private PhoneCasesByModelPage modelPage;
    private CollectionPage collectionPage;
    private ProductPage productPage;
    private CartPage cartPage;

    @Before
    public void setUp() {
        PlaywrightManager.initBrowser();
        page = PlaywrightManager.getPage();

        homePage = new HomePage(page);
        modelPage = new PhoneCasesByModelPage(page);
        collectionPage = new CollectionPage(page);
        productPage = new ProductPage(page);
        cartPage = new CartPage(page);
    }

    @After
    public void tearDown() {
        PlaywrightManager.closeBrowser();
    }

    // ---------------------------------------------------------------
    // Step 1-2: Navigate + Mobile Covers nav
    // ---------------------------------------------------------------

    @Given("I navigate to the CaseKaro home page")
    public void i_navigate_to_the_casekaro_home_page() {
        homePage.navigateToHome();
    }

    @When("I click on {string} from the top navigation menu")
    public void i_click_on_from_the_top_navigation_menu(String linkText) {
        homePage.clickTopNavLink(linkText);
    }

    @When("I scroll to the phone cases by model search section")
    public void i_scroll_to_the_phone_cases_by_model_search_section() {
        modelPage.scrollToModelSearch();
    }

    @When("I search for {string} in the phone model search box")
    public void i_search_for_in_the_phone_model_search_box(String query) {
        modelPage.typeInModelSearch(query);
    }

    // ---------------------------------------------------------------
    // Step 3-5: Search "iPhone 16 Pro", wait for autocomplete, click exact match
    // ---------------------------------------------------------------

    @When("I clear the search box and search for {string}")
    public void i_clear_the_search_box_and_search_for(String model) {
        modelPage.clearModelSearch();
        modelPage.typeInModelSearch(model);
    }

    @When("I wait for the autocomplete suggestions to appear")
    public void i_wait_for_the_autocomplete_suggestions_to_appear() {
        List<String> suggestions = modelPage.waitForAutocompleteAndGetSuggestions();
        assertThat(suggestions)
                .as("Autocomplete suggestions should appear for the searched model")
                .isNotEmpty();
    }

    @When("I click on {string} from the dropdown, not {string}")
    public void i_click_on_from_the_dropdown_not(String wanted, String notWanted) {
        modelPage.clickIPhone16ProFromDropdown();
    }

    @Then("I should land on the iPhone 16 Pro collection page")
    public void i_should_land_on_the_iphone_16_pro_collection_page() {
        collectionPage.assertOnIPhone16ProCollection();
    }

    // ---------------------------------------------------------------
    // Step 6-7: Choose Options on first card, verify 3 material variants
    // ---------------------------------------------------------------

    @When("I click {string} on the first product card")
    public void i_click_on_the_first_product_card(String buttonLabel) {
        collectionPage.clickChooseOptionsOnFirstCard();
    }

    @Then("the product page should offer {string}, {string} and {string} material variants")
    public void the_product_page_should_offer_material_variants(String m1, String m2, String m3) {
        productPage.assertThreeMaterialVariants();
    }

    // ---------------------------------------------------------------
    // Step 8: Add all 3 materials to cart
    // ---------------------------------------------------------------

    @When("I add the {string} material variant to the cart")
    public void i_add_the_material_variant_to_the_cart(String material) {
        productPage.selectMaterialAndAddToCart(material);
    }

    // ---------------------------------------------------------------
    // Step 9-11: Open cart, validate count + materials, print details
    // ---------------------------------------------------------------

    @When("I open the cart")
    public void i_open_the_cart() {
        cartPage.openCart();
    }

    @Then("the cart should contain exactly {int} items")
    public void the_cart_should_contain_exactly_items(int expectedCount) {
        cartPage.assertCartItemCount(expectedCount);
    }

    @And("the cart should contain materials {string}, {string} and {string}")
    public void the_cart_should_contain_materials(String m1, String m2, String m3) {
        cartPage.assertMaterialsInCart(List.of(m1, m2, m3));
    }

    @And("I print all cart item details to the console")
    public void i_print_all_cart_item_details_to_the_console() {
        cartPage.printCartItemDetails();
    }
}
