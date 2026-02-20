# Language: en
# Domain: Investment Tracking System

Feature: Account Management
  As an individual investor with multiple brokerage accounts
  I want to create and manage my brokerage accounts
  So that I can assign positions to the correct accounts

  @FR-073
  @v0.1 @account
  Scenario: Create new account
    Given I want to add a new brokerage account
    When I enter the following account data:
      | Field  | Value |
      | Name   | XTB   |
      | Broker | XTB   |
    Then a new account "XTB" should be created
    And the account should be available for position assignment

  @FR-074
  @v0.1 @account @validation
  Scenario: Create account validation - missing required fields
    Given I want to add a new brokerage account
    When I try to create an account without a name
    Then I should see an account error "Account name is required"
    And no account should be created

  @FR-074
  @v0.1 @account @validation
  Scenario: Create account validation - missing broker name
    Given I want to add a new brokerage account
    When I try to create an account without a broker name
    Then I should see an account error "Broker name is required"
    And no account should be created

  @FR-075
  @v0.1 @account @validation
  Scenario: Create account validation - duplicate name
    Given an account named "XTB" already exists
    When I try to create another account named "XTB"
    Then I should see an account error "already exists"
    And no account should be created

  @FR-076
  @v0.1 @account @manual-entry
  Scenario: Inline account creation during position entry
    Given the instrument catalog contains "PKO" with name "PKO Bank Polski" and type "STOCK"
    And I want to manually add a position
    And no accounts exist in the system
    When I create a new account "mBank" with broker "mBank" during position entry
    And I enter the following position data:
      | Field         | Value     |
      | Instrument    | PKO       |
      | Quantity      | 100       |
      | Average Cost  | 45        |
      | Account       | mBank     |
    Then a new position for "PKO" should be created
    And the position should be assigned to account "mBank"
