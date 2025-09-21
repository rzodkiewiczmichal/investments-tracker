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

  # Generic import scenario (format will be replaced with specific broker formats)
  Scenario: Import positions from broker file
    Given I have an export file from "Broker A" with position data
    And the file contains valid position data
    When I import the file
    Then the positions should be created in the system
    And I should see a success message "Import completed successfully"

  # NOTE: Import is one-time operation in MVP - no updates via import

  # Validation Scenarios for Required Fields

  Scenario: Import validation - missing instrument identifier
    Given I have an import file with a row missing instrument name
    When I try to import the file
    Then I should see an error "Row 2: Instrument identifier is required"
    And no positions should be created

  Scenario: Import validation - missing quantity
    Given I have an import file with a row missing quantity
    When I try to import the file
    Then I should see an error "Row 3: Quantity is required"
    And no positions should be created

  Scenario: Import validation - missing account identifier
    Given I have an import file without account information
    When I try to import the file
    Then I should see an error "Account identifier is required"
    And no positions should be created

  Scenario: Import validation - invalid quantity (negative)
    Given I have an import file with quantity "-50"
    When I try to import the file
    Then I should see an error "Row 2: Quantity must be positive"
    And no positions should be created

  Scenario: Import validation - invalid quantity (zero)
    Given I have an import file with quantity "0"
    When I try to import the file
    Then I should see an error "Row 2: Quantity must be greater than zero"
    And no positions should be created

  Scenario: Import validation - invalid quantity (non-numeric)
    Given I have an import file with quantity "ABC"
    When I try to import the file
    Then I should see an error "Row 2: Quantity must be a number"
    And no positions should be created

  Scenario: Import validation - invalid average cost
    Given I have an import file with average cost "-100"
    When I try to import the file
    Then I should see an error "Row 2: Average cost must be positive"
    And no positions should be created

  Scenario: Import with missing optional fields
    Given I have an import file with:
      | Instrument | Quantity | Account  |
      | Apple      | 100      | Broker A |
      | Microsoft  | 50       | Broker A |
    And average cost is not provided
    When I import the file
    Then positions should be created without cost basis
    And I should see a warning "2 positions imported without average cost - manual entry required"

  Scenario: Import aggregation across accounts
    Given I import file from "Broker A" with 50 shares of "Apple"
    And I import file from "Broker B" with 30 shares of "Apple"
    When I view my portfolio
    Then I should see a single position for "Apple" with 80 shares

  Scenario: Import duplicate prevention
    Given I have already imported data from "Broker A"
    When I try to import the same file again
    Then I should see a message "This file has already been imported"
    And no duplicate positions should be created