# Language: en
# Domain: Investment Tracking System

Feature: Broker Reconciliation
  As a system administrator
  I want to reconcile system data with broker statements
  So that I can ensure data accuracy

  @FR-061 @FR-014
  @v0.7 @reconciliation
  Scenario: Successful reconciliation
    Given I have the following positions in the system:
      | Instrument | Quantity | Current Value |
      | Apple      | 100      | 65000        |
      | Microsoft  | 50       | 65000        |
    And my broker statement shows:
      | Instrument | Quantity | Current Value |
      | Apple      | 100      | 65000        |
      | Microsoft  | 50       | 65000        |
    When I run reconciliation for this broker
    Then I should see "Reconciliation successful - all positions match"
    And the reconciliation status should be "MATCHED"

  @FR-062 @FR-061
  @v0.7 @reconciliation
  Scenario: Reconciliation with quantity mismatch
    Given I have 100 shares of "Apple" in the system
    And my broker statement shows 95 shares of "Apple"
    When I run reconciliation
    Then I should see "Reconciliation failed - 1 mismatch found"
    And I should see "Apple: System shows 100 shares, Broker shows 95 shares"
    And the reconciliation status should be "MISMATCH"

  @FR-063 @FR-061
  @v0.7 @reconciliation
  Scenario: Reconciliation with missing position in system
    Given I have positions for "Apple" and "Microsoft" in the system
    And my broker statement includes "Apple", "Microsoft", and "Tesla"
    When I run reconciliation
    Then I should see "Position missing in system: Tesla"
    And the reconciliation status should be "MISMATCH"

  @FR-064 @FR-061
  @v0.7 @reconciliation
  Scenario: Reconciliation with extra position in system
    Given I have positions for "Apple", "Microsoft", and "Tesla" in the system
    And my broker statement only includes "Apple" and "Microsoft"
    When I run reconciliation
    Then I should see "Position not found in broker statement: Tesla"
    And the reconciliation status should be "MISMATCH"

  @FR-065 @FR-061
  @v0.7 @reconciliation
  Scenario: Value reconciliation within tolerance
    Given reconciliation tolerance is set to 1%
    And I have "Apple" with current value of 65000 PLN in the system
    And my broker statement shows "Apple" with value of 65500 PLN
    When I run reconciliation
    Then I should see "Reconciliation successful - values within tolerance"
    And I should see "Apple: 0.77% difference (within 1% tolerance)"
    And the reconciliation status should be "MATCHED"

  @FR-066 @FR-061
  @v1.0 @reconciliation @history
  Scenario: Reconciliation history
    Given I have run reconciliations on different dates
    When I view reconciliation history
    Then I should see a list of past reconciliations
    And each entry should show:
      | Field             | Description                |
      | Date              | When reconciliation ran    |
      | Broker            | Which broker was checked   |
      | Status            | MATCHED or MISMATCH        |
      | Positions Checked | Number of positions        |
      | Issues Found      | Number of discrepancies    |
