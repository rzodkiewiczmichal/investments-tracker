# Language: en
# Domain: Investment Tracking System

Feature: Price Updates
  As a system administrator
  I want to update current prices for instruments
  So that portfolio values are accurate

  Scenario: Manual price update for single instrument
    Given I have a position in "Apple"
    And the last price was 600 PLN
    When I update the price of "Apple" to 650 PLN
    Then the current price of "Apple" should be 650 PLN
    And the position value should be recalculated
    And I should see "Price updated for Apple"

  Scenario: Bulk price update from file
    Given I have positions in multiple instruments
    When I import a price file with:
      | Instrument    | Price |
      | Apple         | 650   |
      | Microsoft     | 1350  |
      | S&P 500 ETF   | 1750  |
    Then all prices should be updated
    And portfolio values should be recalculated
    And I should see "Updated prices for 3 instruments"

  Scenario: Price update affects portfolio metrics
    Given I have 100 shares of "Apple" bought at 600 PLN
    And the current price is 600 PLN
    And my portfolio shows 0% return
    When I update the price of "Apple" to 660 PLN
    Then my portfolio P&L should show +6000 PLN
    And my portfolio return should show +10%

  Scenario: Price validation - negative price
    Given I have a position in "Microsoft"
    When I try to update the price to -100 PLN
    Then I should see an error "Price must be positive"
    And the price should not be updated

  Scenario: Price validation - zero price
    Given I have a position in "Microsoft"
    When I try to update the price to 0 PLN
    Then I should see an error "Price cannot be zero"
    And the price should not be updated

  Scenario: View price update history
    Given I have updated prices multiple times for "Apple"
    When I view price history for "Apple"
    Then I should see a list of price changes:
      | Date       | Old Price | New Price | Change |
      | 2025-09-16 | 600      | 650      | +8.33% |
      | 2025-09-15 | 580      | 600      | +3.45% |

  Scenario: Polish government bond value update
    Given I have Polish government bonds
    And the current value is 100000 PLN
    When I update the bond value to 102000 PLN
    Then the bond position should show current value of 102000 PLN
    And the P&L should be recalculated based on the new value