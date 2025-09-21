# Language: en
# Domain: Investment Tracking System

Feature: Data Import
  As a system administrator
  I want to import position data from brokers
  So that the system has current investment information

  # NOTE: Each broker has a different export format
  # TODO: Add specific scenarios for each broker's format once samples are provided:
  # - Broker A format (to be defined)
  # - Broker B format (to be defined)
  # - Broker C format (to be defined)
  # - IKE account format (to be defined)
  # - IKZE account format (to be defined)
  # - Polish Government Bonds account format (to be defined)

  Scenario: Import positions from CSV file
    Given I have a CSV file from "Broker A" with position data
    And the CSV contains:
      | Instrument | Quantity | Average Cost | Current Price |
      | Apple      | 100      | 600         | 650          |
      | Microsoft  | 50       | 1200        | 1300         |
    When I import the CSV file
    Then the system should create 2 positions
    And the position for "Apple" should have 100 shares at 600 PLN average cost
    And the position for "Microsoft" should have 50 shares at 1200 PLN average cost
    And I should see a success message "Imported 2 positions from Broker A"

  Scenario: Import positions from multiple brokers
    Given I have already imported positions from "Broker A"
    And "Broker A" has 50 shares of "Apple" at 600 PLN
    When I import positions from "Broker B"
    And "Broker B" has 30 shares of "Apple" at 620 PLN
    Then the aggregated position for "Apple" should have 80 shares
    And the average cost should be 607.50 PLN

  Scenario: Manual position entry
    Given I want to manually add a position
    When I enter the following position data:
      | Field         | Value           |
      | Instrument    | Tesla           |
      | Quantity      | 25              |
      | Average Cost  | 800             |
      | Account       | Broker C        |
    Then a new position for "Tesla" should be created
    And the position should have 25 shares at 800 PLN average cost

  Scenario: Import validation - missing required fields
    Given I have a CSV file with missing quantity data
    When I try to import the CSV file
    Then I should see an error message "Import failed: Quantity is required"
    And no positions should be created

  Scenario: Import validation - negative values
    Given I have a CSV file with negative quantity
    When I try to import the CSV file
    Then I should see an error message "Import failed: Quantity must be positive"
    And no positions should be created

  Scenario: Update existing positions
    Given I have an existing position of 100 shares of "Apple" at 600 PLN
    When I import updated data with 120 shares of "Apple" at 610 PLN
    Then the position for "Apple" should be updated to 120 shares
    And the average cost should be 610 PLN
    And I should see a message "Updated 1 position"

  Scenario: Import Polish government bonds
    Given I have bond data to import
    When I import the following bond position:
      | Field           | Value                    |
      | Instrument Type | Polish Government Bond   |
      | Series          | EDO0435                  |
      | Invested Amount | 100000                   |
      | Current Value   | 103500                   |
      | Account         | Government Bonds Account |
    Then a bond position should be created
    And the position should show invested amount of 100000 PLN
    And the position should show current value of 103500 PLN