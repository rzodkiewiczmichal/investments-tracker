# Language: en
# Domain: Investment Tracking System

Feature: Broker Transaction History Import
  As an individual investor
  I want to import transaction history from my broker
  So that my positions are automatically computed from buy/sell records

  @FR-021 @FR-032
  @v0.2 @import
  Scenario: Import with all instruments matched
    Given the instrument catalog contains "ETFBCASH" with name "Beta ETF Cash" and type "ETF"
    When I upload an mBank CSV with the following transactions:
      | instrument | side | quantity | price  | commission |
      | ETFBCASH   | K    | 100      | 140.40 | 5.00       |
    Then the import session status should be "READY_TO_CONFIRM"
    And the import summary should show 1 matched instruments
    And the import summary should show 0 unmatched instruments
    When I confirm the import for account "mBank eMAKLER"
    Then the import session status should be "COMPLETED"
    And a position for "ETFBCASH" should exist with quantity 100

  @FR-021 @FR-032
  @v0.2 @import
  Scenario: Import with unmatched instruments requires user mapping
    Given the instrument catalog contains "TOR" with name "Torpol S.A." and type "STOCK"
    When I upload an mBank CSV with the following transactions:
      | instrument | side | quantity | price | commission |
      | TORPOL     | K    | 163      | 24.80 | 15.77      |
    Then the import session status should be "PENDING_REVIEW"
    And the import summary should show 0 matched instruments
    And the import summary should show 1 unmatched instruments
    When I confirm the import with mappings for account "mBank eMAKLER":
      | brokerName | catalogSymbol |
      | TORPOL     | TOR           |
    Then the import session status should be "COMPLETED"
    And a position for "TOR" should exist with quantity 163

  @FR-021 @FR-032
  @v0.2 @import
  Scenario: Fully sold instruments are silently skipped
    When I upload an mBank CSV with the following transactions:
      | instrument | side | quantity | price | commission |
      | ALLEGRO    | K    | 100      | 31.30 | 12.21      |
      | ALLEGRO    | S    | 100      | 30.16 | 11.76      |
    Then the import session status should be "READY_TO_CONFIRM"
    And the import summary should show 0 matched instruments
    And the import summary should show 0 unmatched instruments

  @FR-021 @FR-032
  @v0.2 @import
  Scenario: Confirm with incomplete mappings fails
    When I upload an mBank CSV with the following transactions:
      | instrument | side | quantity | price | commission |
      | TORPOL     | K    | 100      | 24.80 | 5.00       |
    Then the import session status should be "PENDING_REVIEW"
    When I confirm the import without mappings for account "mBank eMAKLER"
    Then the import confirmation should fail with status 422

  @FR-021 @FR-032
  @v0.2 @import
  Scenario: Import replaces previous holdings for same account
    Given the instrument catalog contains "ETFBCASH" with name "Beta ETF Cash" and type "ETF"
    And account "mBank eMAKLER" exists with broker "mBank"
    And a position for "ETFBCASH" exists with 50 units at 130.00 cost in account "mBank eMAKLER"
    When I upload an mBank CSV with the following transactions:
      | instrument | side | quantity | price  | commission |
      | ETFBCASH   | K    | 200      | 140.40 | 5.00       |
    And I confirm the import for account "mBank eMAKLER"
    Then a position for "ETFBCASH" should exist with quantity 200
