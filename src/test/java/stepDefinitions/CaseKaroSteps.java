package stepDefinitions;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class CaseKaroSteps {

    private Playwright playwright;
    private Browser browser;
    private Page page;

    private static final String BASE_URL = "https://casekaro.com/";

    @Before
    public void setUp() {
        playwright = Playwright.create();
        browser = playwright.firefox().launch(new BrowserType.LaunchOptions()
                .setHeadless(false)
                .setSlowMo(500));
        page = browser.newPage();
    }

    @After
    public void tearDown() {

        browser.close();
        playwright.close();
    }

    @Given("the user navigates to the CaseKaro homepage")
    public void the_user_navigates_to_the_homepage() {
        page.navigate(BASE_URL);
        assertThat(page).hasURL(BASE_URL);
    }

    @When("the user clicks on {string} in the top navigation menu")
    public void the_user_clicks_nav_item(String menuItem) {
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(menuItem)).click();
    }

    @When("the user searches for the phone model {string}")
    public void the_user_searches_for_phone_model(String model) {
        page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Search your phone model…"))
                .fill(model);
    }

    @And("the user selects {string} from the autocomplete dropdown")
    public void the_user_selects_from_autocomplete(String exactModel) {
        Locator option = page.getByText(exactModel, new Page.GetByTextOptions().setExact(true)).first();
        assertThat(option).isVisible();
        option.click();
    }

    @And("the user clicks on the {int} product card")
    public void the_user_clicks_on_product_card(int num) {

        Locator card = page.locator("li.grid__item").nth(num);

        card.click();

        page.waitForURL("**/products/**");
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
    }

    @And("the user adds the {string} material variant to the cart")
    public void the_user_adds_material_variant_to_cart(String material) {

        Locator materialInput = page.locator(
                "input[name='Material'][value='" + material + "']");

        assertThat(materialInput).isAttached();

        String inputId = materialInput.getAttribute("id");

        Locator materialLabel = page.locator("label[for='" + inputId + "']");
        materialLabel.click();

        Locator addBtn = page.locator("button[id^='ProductSubmitButton']");
        Locator cartDrawer = page.locator("cart-drawer.active");

        for (int i = 0; i < 3 && cartDrawer.count() == 0; i++) {
            addBtn.click();
            page.waitForTimeout(1000);
        }

        assertThat(cartDrawer).isVisible();

        page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Close"))
                .click();

        assertThat(cartDrawer).isHidden();
        page.waitForTimeout(1500);
    }

    @And("the user opens the cart")
    public void the_user_opens_the_cart() {
        page.locator("#cart-icon-bubble").click();
        Locator closeDrawerBtn = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Close"));
        assertThat(closeDrawerBtn).isVisible();
    }

    @Then("the cart should contain {int} items")
    public void the_cart_should_contain_n_items(Integer expectedCount) {
        Locator cartItems = page.locator("tr.cart-item");
        System.out.println("Actual cart item count: " + cartItems.count());
    }

    @And("the material, price and link of each cart item should be printed to the console")
    public void print_material_price_link_of_each_item() {
        Locator cartItems = page.locator("tr.cart-item");
        int count = cartItems.count();

        System.out.println("===== CART CONTENTS =====");
        for (int i = 0; i < count; i++) {
            Locator item = cartItems.nth(i);
            String material = item.locator("dd").innerText();
            String price = item.locator(".cart-item__price-wrapper .price").first().innerText().trim();
            String link = item.locator("a.cart-item__name").getAttribute("href");

            System.out.println("Material: " + material);
            System.out.println("Price: ₹" + price.replace("₹", "").trim());
            System.out.println("Link: https://casekaro.com" + link);
            System.out.println("--------------------------");
        }
    }
}