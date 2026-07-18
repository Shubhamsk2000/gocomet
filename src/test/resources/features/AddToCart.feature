Feature: Add iPhone 16 Pro case variants to cart on CaseKaro

  As a shopper on casekaro.com
  I want to search for the iPhone 16 Pro and add all 3 material variants to my cart
  So that I can buy the case in whichever material I prefer

  Scenario: Search for iPhone 16 Pro and add all three material variants to the cart
    Given the user navigates to the CaseKaro homepage
    When the user clicks on "Mobile Covers" in the top navigation menu
    When the user searches for the phone model "Samsung S24"
    And the user selects "Samsung S24" from the autocomplete dropdown
    And the user clicks on the 0 product card
    And the user adds the "Hard" material variant to the cart
    And the user adds the "Soft" material variant to the cart
    And the user adds the "Glass" material variant to the cart
    And the user opens the cart
    Then the cart should contain 3 items
    And the material, price and link of each cart item should be printed to the console
