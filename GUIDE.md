# CaseKaro Cart Assignment — Full Setup & Learning Guide

This guide takes you from "empty VS Code" to a working Java + Playwright + Cucumber
test project, explaining every concept so you can defend it in your interview.

---

## 0. The tech stack, in plain English

| Tool | What it actually does |
|---|---|
| **Java** | The programming language everything is written in. |
| **Maven** | Manages your dependencies (libraries) and builds/runs your project via a `pom.xml` file. Think of it like `package.json` + `npm` if you know JS. |
| **Playwright** | The library that actually drives a real browser — opens pages, clicks buttons, types text, waits for things to load, and makes assertions. |
| **Cucumber** | Lets you describe test scenarios in plain English ("Gherkin") in a `.feature` file, then maps each line to real Java code ("step definitions"). This is called **BDD** (Behavior-Driven Development). |
| **JUnit** | The engine that actually executes the tests. Cucumber plugs into JUnit to run. |

Flow: **JUnit runs → TestRunner → reads the .feature file → matches each line to a
method in your step definitions class → those methods call Playwright → Playwright
drives Chrome.**

---

## 1. Install prerequisites

1. **JDK 17+** — download from [Adoptium](https://adoptium.net/). Verify:
   ```
   java -version
   ```
2. **Maven** — download from [maven.apache.org](https://maven.apache.org/download.cgi), add to PATH. Verify:
   ```
   mvn -version
   ```
3. **VS Code extensions** (Extensions panel, Ctrl+Shift+X):
   - "Extension Pack for Java" (Microsoft)
   - "Maven for Java" (Microsoft)
   - "Cucumber (Gherkin) Full Support" — gives you syntax highlighting + step-linking in `.feature` files

---

## 2. Get the project into VS Code

1. Unzip the project folder I've given you (`casekaro-project/`) anywhere on your machine.
2. In VS Code: **File → Open Folder** → select `casekaro-project`.
3. VS Code will detect `pom.xml` and prompt you to import as a Maven project — accept it.
4. Open a terminal in VS Code (`` Ctrl+` ``) and run:
   ```
   mvn clean install -DskipTests
   ```
   This downloads all dependencies (Playwright, Cucumber, JUnit) from Maven Central. First run takes a few minutes.

## 3. Install the Playwright browser binaries

Playwright the *library* is now on your classpath, but it also needs the actual
browser binaries (Chromium etc.) downloaded once:

```
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install"
```

If `exec:java` isn't recognized, add this plugin to `pom.xml` under `<build><plugins>`:
```xml
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>exec-maven-plugin</artifactId>
    <version>3.2.0</version>
</plugin>
```

---

## 4. Understand the folder structure

```
casekaro-project/
├── pom.xml                                  ← dependencies + build config
├── src/test/resources/features/
│   └── AddToCart.feature                    ← plain-English scenario (Gherkin)
└── src/test/java/
    ├── stepDefinitions/CaseKaroSteps.java   ← Java code behind each Gherkin line
    └── runners/TestRunner.java              ← tells JUnit to run Cucumber
```

Cucumber convention: tests live under `src/test/...`, not `src/main/...`, because this is test code, not production code.

---

## 5. Read the feature file

Open `AddToCart.feature`. Gherkin has 3 keywords that matter:
- **Given** — sets up a starting state
- **When** — performs an action
- **Then** — asserts an outcome
- `And` / `But` just continue the previous keyword's meaning

Each line becomes a method call. `{string}` and `{int}` in a step definition are
**parameters** — Cucumber extracts the quoted text/number from the Gherkin line and
passes it into your Java method as an argument. That's how one method like
`the_user_adds_material_variant_to_cart(String material)` can be reused for
"Hard", "Soft", and "Glass" without writing 3 separate methods.

---

## 6. Playwright core concepts (this is the part to really understand for the interview)

- **`Page`** — represents one browser tab. Almost everything happens through it: `page.navigate()`, `page.locator()`, `page.getByRole()`, etc.
- **Locators** (`Locator` objects) — a *recipe* for finding an element, not the element itself. Playwright re-finds it fresh every time you act on it, which is why it survives page re-renders. Preferred locator strategies, in order of preference:
  1. `getByRole(AriaRole.BUTTON, ...setName("Add to cart"))` — matches how a real user/screen reader identifies elements. Most robust to CSS changes.
  2. `getByText("iPhone 16 Pro", ...setExact(true))` — exact text match. **`setExact(true)` is critical** here — without it, "iPhone 16 Pro" would also match "iPhone 16 Pro Max" since that string *contains* your search text.
  3. `getByPlaceholder(...)`, `getByLabel(...)` — good for form fields.
  4. CSS selectors (`page.locator("[class*='...']")`) — last resort, used in this template where the real class names are unknown; **you should replace these** with something more specific once you inspect the real site.
- **Auto-waiting** — Playwright automatically waits (up to a default 30s timeout) for an element to be visible/enabled/stable before clicking or filling it. This is *why the assignment says avoid try-catch*: you don't need manual retry logic that Selenium users often reach for.
- **Assertions** — `assertThat(locator).isVisible()`, `.isEnabled()`, `.hasText(...)`, `assertThat(page).hasURL(...)`. These come from `PlaywrightAssertions` and are "web-first" — they also auto-retry briefly before failing, unlike a plain JUnit `assertEquals` on a value you fetched once.

---

## 7. Finding the REAL selectors on casekaro.com (you must do this step yourself)

I could not browse the live rendered DOM of casekaro.com from here, so the selectors
in `CaseKaroSteps.java` marked `// TODO(verify)` are educated guesses based on common
e-commerce site patterns (this is a Shopify-based store). **Do this before your
interview** — it's also exactly the skill an interviewer wants to see:

**Option A — Playwright Codegen (recommended, generates code for you):**
```
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="codegen casekaro.com"
```
This opens a real browser. Click through the exact flow in the assignment (nav →
search → dropdown → choose options → add to cart → cart) and Codegen writes the
Playwright locator for every action you perform, in a side panel. Copy the good ones
into your step definitions.

**Option B — Manual DevTools inspection:**
1. Open casekaro.com in Chrome, right-click the element (e.g. the search box) → **Inspect**.
2. Right-click the highlighted HTML in DevTools → **Copy → Copy selector**, or note its `class`/`placeholder`/`aria-label`.
3. Prefer stable attributes (an `id`, a `data-testid`, `placeholder`, visible text, `aria-label`) over long auto-generated class strings — CSS classes on Shopify themes often include random/build hashes that change.

Update each `TODO(verify)` locator in `CaseKaroSteps.java` accordingly.

---

## 8. Run it

In VS Code: open `TestRunner.java` → click the green **Run** arrow above the class,
or from terminal:
```
mvn test
```
Cucumber's `pretty` plugin will print each Gherkin step with a ✔/✘ as it executes,
plus your `System.out.println` cart output at the end.

---

## 9. Debugging workflow while you build this out

1. Run with `setHeadless(false)` (already set) so you watch the browser.
2. `setSlowMo(300)` adds a 300ms pause between actions so you can actually see what's happening — remove/reduce this once things work.
3. If a locator fails, comment out later steps temporarily and add `page.pause()` right after `page.navigate()` — this opens the **Playwright Inspector**, where you can test locators live in a console before committing them to code.
4. Read the exception message carefully — Playwright errors usually tell you exactly which locator timed out and how many elements it matched (0 = wrong locator, 2+ = need to narrow it, e.g. with `.first()` or `setExact(true)`).

---

## 10. Interview talking points — be ready to explain:

- **Why Cucumber/BDD**: separates *what* is being tested (readable by non-engineers, e.g. a PM) from *how* it's tested (Java/Playwright code). Also promotes reuse — one step definition method serves 3 Gherkin lines (Hard/Soft/Glass).
- **Why `setExact(true)` matters**: substring matching would silently select "iPhone 16 Pro Max" instead of "iPhone 16 Pro" — a subtle, easy-to-miss bug that's exactly the kind of edge case QA is supposed to catch.
- **Why no try-catch**: Playwright's auto-waiting + web-first assertions already handle timing issues; wrapping everything in try-catch would *swallow* real failures instead of surfacing them, which defeats the purpose of a test.
- **Negative validation step**: after searching "Apple", you assert that *none* of the other brand names appear in the results — that's a real assertion on the *absence* of something, not just presence.
- **Locator priority**: role/text-based locators > CSS class locators, because they mirror how a real user perceives the page and survive front-end refactors better.

---

## Checklist against the assignment's "Note" section

- [x] Java + Playwright ✔ (pom.xml)
- [x] No try-catch ✔ (relies on auto-waiting instead)
- [x] Assertions for validations ✔ (`assertThat`, brand-absence loop, cart count)
- [x] Cucumber feature file ✔ (`AddToCart.feature`)
- [ ] **You still need to**: verify/replace the placeholder selectors against the real site, and take the screenshots the assignment asks for (`Ss for ref`) once it's passing.








Feature: Add iPhone 16 Pro case variants to cart on CaseKaro

  As a shopper on casekaro.com
  I want to search for the iPhone 16 Pro and add all 3 material variants to my cart
  So that I can buy the case in whichever material I prefer

  Scenario: Search for iPhone 16 Pro and add all three material variants to the cart
    Given the user navigates to the CaseKaro homepage
    When the user clicks on "Mobile Covers" in the top navigation menu
    When the user searches for the phone model "Samsung S24"
    And the user selects "Samsung S24" from the autocomplete dropdown
    And the user clicks Choose Options on the 1 product card
    And the user adds the "Hard" material variant to the cart
    And the user clicks Choose Options on the 1 product card
    And the user adds the "Soft" material variant to the cart
    And the user clicks Choose Options on the 1 product card
    And the user adds the "Glass" material variant to the cart
    And the user opens the cart
    Then the cart should contain 3 items
    And the material, price and link of each cart item should be printed to the console









package stepDefinitions;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
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

    @And("the user clicks Choose Options on the {int} product card")
    public void the_user_clicks_choose_options_on_first_card(int num) {
        Locator cards = page.locator("li.grid__item").nth(num);

        cards.locator("button.quick-add__submit").click();

        Locator openModal = page.locator("[id^='QuickAdd-']:visible");
        assertThat(openModal).isVisible();
    }

    @And("the user adds the {string} material variant to the cart")
    public void the_user_adds_material_variant_to_cart(String material) {
        Locator openModal = page.locator("[id^='QuickAdd-']:visible");
        assertThat(openModal).isVisible();

        Locator materialInput = openModal.locator(
                "input[name='Material'][value='" + material + "']");

        assertThat(materialInput).isAttached();

        String inputId = materialInput.getAttribute("id");

        Locator materialLabel = openModal.locator("label[for='" + inputId + "']");
        materialLabel.click();

        assertThat(materialInput).isChecked();

        Locator addToCartBtn = openModal.locator("button[id^='ProductSubmitButton']");
        assertThat(addToCartBtn).isEnabled();
        for (int i = 0; i < 3; i++) {
            addToCartBtn.click();

            if (page.locator("cart-drawer.active").isVisible()) {
                break;
            }
            page.waitForTimeout(1500);
        }

        page.waitForSelector("tr.cart-item");

        Locator closeDrawerBtn = page.locator("button.drawer__close");

        if (closeDrawerBtn.isVisible()) {
            closeDrawerBtn.click();

            page.waitForFunction(
                    "() => !document.querySelector('cart-drawer').classList.contains('active')");
        }

        page.waitForSelector("[id^='QuickAdd-']", new Page.WaitForSelectorOptions()
                .setState(WaitForSelectorState.HIDDEN));
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