Feature: CaseKaro shopping flow for iPhone 16 Pro cases
  As a shopper on casekaro.com
  I want to find my exact phone model and buy all material variants of a case
  So that I can compare Hard, Soft and Glass options in my cart

  Scenario: Search iPhone 16 Pro, add all 3 material variants to cart and validate
    Given I navigate to the CaseKaro home page
    When I click on "Mobile Covers" from the top navigation menu
    And I scroll to the phone cases by model search section
    When I clear the search box and search for "iPhone 16 Pro"
    And I wait for the autocomplete suggestions to appear
    And I click on "iPhone 16 Pro" from the dropdown, not "iPhone 16 Pro Max"
    Then I should land on the iPhone 16 Pro collection page

    When I click "Choose Options" on the first product card
    Then the product page should offer "Hard", "Soft" and "Glass" material variants

    When I add the "Hard" material variant to the cart
    And I add the "Soft" material variant to the cart
    And I add the "Glass" material variant to the cart
    And I open the cart

    Then the cart should contain exactly 3 items
    And the cart should contain materials "Hard", "Soft" and "Glass"
    And I print all cart item details to the console
