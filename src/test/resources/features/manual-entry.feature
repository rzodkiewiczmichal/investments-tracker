# Language: en
# Domain: Investment Tracking System

Feature: Manual Position Entry
  As an individual investor
  I want to manually enter position data
  So that I can add positions when import is not available

  @FR-041 @FR-091
  @v0.1 @manual-entry
  Scenario: Manual entry of stock position
    Given the instrument catalog contains "TSLA" with name "Tesla Inc." and type "STOCK"
    And I want to manually add a position
    When I enter the following position data:
      | Field         | Value    |
      | Instrument    | TSLA     |
      | Quantity      | 25       |
      | Average Cost  | 800      |
      | Account       | Broker C |
    Then a new position for "TSLA" should be created
    And the position should have 25 shares at 800 PLN average cost
    And I should see "Position added successfully"

  @FR-042 @FR-091
  @v0.1 @manual-entry
  Scenario: Manual entry of ETF position
    Given the instrument catalog contains "ETFSP500" with name "Beta ETF S&P 500" and type "ETF"
    And I want to manually add a position
    When I enter the following position data:
      | Field         | Value        |
      | Instrument    | ETFSP500     |
      | Quantity      | 100          |
      | Average Cost  | 1750         |
      | Account       | IKE Account  |
    Then a new position for "ETFSP500" should be created
    And the position should have 100 units at 1750 PLN average cost

  @FR-043 @FR-091
  @v0.6 @manual-entry @bonds
  Scenario: Manual entry of Polish government bonds
    Given I want to manually add a bond position
    When I enter the following bond data:
      | Field           | Value                    |
      | Instrument Type | Polish Government Bond   |
      | Series          | EDO0435                  |
      | Invested Amount | 100000                   |
      | Current Value   | 103500                   |
      | Account         | Government Bonds Account |
    Then a bond position should be created
    And the position should show invested amount of 100000 PLN
    And the position should show current value of 103500 PLN

  @FR-041 @FR-081 @FR-083
  @v0.1 @manual-entry
  Scenario: Newly added position shows no P&L until price is available
    Given the instrument catalog contains "TSLA" with name "Tesla Inc." and type "STOCK"
    And I want to manually add a position
    When I enter the following position data:
      | Field        | Value    |
      | Instrument   | TSLA     |
      | Quantity     | 25       |
      | Average Cost | 800      |
      | Account      | Broker C |
    Then a new position for "TSLA" should be created
    And the position should have 25 shares at 800 PLN average cost
    And the position should show invested amount of 20000 PLN
    But the position should not show current value
    And the position should not show P&L

  @FR-044
  @v0.1 @manual-entry @validation
  Scenario: Manual entry validation - instrument not in catalog
    Given I want to manually add a position
    When I try to save position with unknown instrument "UNKN"
    Then I should see an error "Instrument not found"
    And no position should be created

  @FR-045
  @v0.1 @manual-entry @validation
  Scenario: Manual entry validation - invalid quantity
    Given the instrument catalog contains "TEST" with name "Test Instrument" and type "STOCK"
    And I want to manually add a position
    When I enter quantity as "-10"
    Then I should see an error "Quantity must be positive"
    And no position should be created

  @FR-046
  @v0.1 @manual-entry @validation
  Scenario: Manual entry validation - invalid average cost
    Given the instrument catalog contains "TEST" with name "Test Instrument" and type "STOCK"
    And I want to manually add a position
    When I enter average cost as "0"
    Then I should see an error "Average cost must be greater than zero"
    And no position should be created
