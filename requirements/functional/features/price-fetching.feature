# Language: en
# Domain: Investment Tracking System

Feature: Price Fetching from Yahoo Finance API
  As a user importing positions
  I want prices to be automatically fetched from Yahoo Finance API
  So that my portfolio values are accurate without manual price entry

  @FR-051 @FR-053 @FR-081
  @v0.2 @price-management @api
  Scenario: Import positions with API price fetch
    Given I have a CSV file with positions:
      | Instrument    | Quantity | Average Cost | Account |
      | AAPL          | 100      | 600         | Broker1 |
      | MSFT          | 50       | 1300        | Broker1 |
    When I import the file
    Then the system should fetch current prices from Yahoo Finance API
    And "AAPL" should have current price from API stored in database
    And "MSFT" should have current price from API stored in database
    And position values should be calculated using fetched prices
    And I should see "Successfully imported 2 positions with current prices"

  @FR-051 @FR-054 @FR-055
  @v0.2 @price-management @validation
  Scenario: API price validation
    Given I am importing positions
    When Yahoo Finance API returns invalid price data:
      | Instrument | API Price |
      | INVALID1   | -100      |
      | INVALID2   | 0         |
    Then the system should reject negative prices
    And the system should reject zero prices
    And I should see an error "Invalid price data received from API for INVALID1, INVALID2"
    And positions should not be created

  @FR-051
  @v0.2 @price-management @error-handling
  Scenario: Import fails when API unavailable
    Given I have a CSV file with valid positions
    When I import the file
    And Yahoo Finance API is unavailable
    Then the import should fail
    And I should see an error "Unable to fetch prices from Yahoo Finance. Please try again later."
    And no positions should be imported

  @FR-056 @FR-001
  @v0.2 @price-management @portfolio
  Scenario: Display last price refresh date in portfolio
    Given I imported positions at "2025-12-21 10:00:00"
    When I view my portfolio summary
    Then I should see "Prices last updated: 2025-12-21 10:00:00"
    And I should see current prices for all positions

  @FR-053 @FR-001 @FR-081 @FR-083 @FR-084
  @v0.2 @price-management @portfolio @metrics
  Scenario: Fetched prices affect portfolio metrics
    Given I import 100 shares of "AAPL" bought at 600 PLN
    And Yahoo Finance API returns current price of 660 PLN for "AAPL"
    When the import completes
    Then my portfolio should show:
      | Metric          | Value     |
      | Invested Amount | 60000 PLN |
      | Current Value   | 66000 PLN |
      | P&L Amount      | +6000 PLN |
      | P&L Percentage  | +10%      |

