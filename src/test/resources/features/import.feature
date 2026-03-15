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
    Then the import session status should be "PENDING_PRICES"
    When I provide prices for the import:
      | symbol   | price  | currency |
      | ETFBCASH | 140.40 | PLN      |
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
    Then the import session status should be "PENDING_PRICES"
    When I provide prices for the import:
      | symbol | price | currency |
      | TOR    | 24.80 | PLN      |
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
    And I provide prices for the import:
      | symbol   | price  | currency |
      | ETFBCASH | 140.40 | PLN      |
    Then a position for "ETFBCASH" should exist with quantity 200

  @FR-021 @FR-032
  @v0.2 @import @mbank
  Scenario: Import mBank IKE account
    Given the instrument catalog contains "PKO" with name "PKO BP S.A." and type "STOCK"
    When I upload an mBank CSV for account "mBank IKE" with the following transactions:
      | instrument | side | quantity | price | commission |
      | PKO        | K    | 50       | 45.20 | 10.00      |
    Then the import session status should be "READY_TO_CONFIRM"
    When I confirm the import for account "mBank IKE"
    Then the import session status should be "PENDING_PRICES"
    When I provide prices for the import:
      | symbol | price | currency |
      | PKO    | 45.20 | PLN      |
    Then the import session status should be "COMPLETED"
    And a position for "PKO" should exist with quantity 50

  @FR-021 @FR-032
  @v0.2 @import @mbank
  Scenario: Import mBank IKZE account
    Given the instrument catalog contains "PZU" with name "PZU S.A." and type "STOCK"
    When I upload an mBank CSV for account "mBank IKZE" with the following transactions:
      | instrument | side | quantity | price | commission |
      | PZU        | K    | 30       | 38.50 | 8.00       |
    Then the import session status should be "READY_TO_CONFIRM"
    When I confirm the import for account "mBank IKZE"
    Then the import session status should be "PENDING_PRICES"
    When I provide prices for the import:
      | symbol | price | currency |
      | PZU    | 38.50 | PLN      |
    Then the import session status should be "COMPLETED"
    And a position for "PZU" should exist with quantity 30

  @FR-021 @FR-032
  @v0.2 @import @mbank
  Scenario: Import handles Polish characters in instrument names
    When I upload an mBank CSV for account "mBank eMAKLER" with the following transactions:
      | instrument              | side | quantity | price  | commission |
      | ŻYWIEC TRADE S.A.      | K    | 10       | 520.00 | 15.00      |
    Then the import session status should be "PENDING_REVIEW"
    And the import summary should show 0 matched instruments
    And the import summary should show 1 unmatched instruments

  @FR-021 @FR-032
  @v0.2 @import @mbank
  Scenario: Import skips transactions with unsupported currencies
    Given the instrument catalog contains "ETFBCASH" with name "Beta ETF Cash" and type "ETF"
    When I upload an mBank CSV for account "mBank eMAKLER" with mixed currency transactions:
      | instrument | side | quantity | price  | commission | currency |
      | ETFBCASH   | K    | 100      | 140.40 | 5.00       | PLN      |
      | FW20       | K    | 1        | 2350.00| 9.00       | PKT      |
    Then the import session status should be "READY_TO_CONFIRM"
    And the import summary should show 1 matched instruments
    And the import summary should show 0 unmatched instruments

  @FR-021 @FR-032
  @v0.2 @import @xtb
  Scenario: XTB import with auto-matched instruments via Closed Positions
    Given the instrument catalog contains "MSFT.US" with name "Microsoft" and type "STOCK" on market "US" in currency "USD"
    When I upload an XTB XLSX for account "XTB" with closed positions and transactions:
      | closedInstrument | closedTicker |
      | Microsoft        | MSFT.US      |
    And the following XTB cash operations:
      | type           | instrument | comment                   |
      | Stock purchase | Microsoft  | OPEN BUY 10 @ 420.50      |
    Then the import session status should be "READY_TO_CONFIRM"
    And the import summary should show 1 matched instruments
    And the import summary should show 0 unmatched instruments
    When I confirm the import for account "XTB"
    Then the import session status should be "PENDING_PRICES"
    When I provide prices for the import:
      | symbol  | price  | currency |
      | MSFT.US | 425.00 | USD      |
    Then the import session status should be "COMPLETED"
    And a position for "MSFT.US" should exist with quantity 10

  @FR-021 @FR-032
  @v0.2 @import @xtb
  Scenario: XTB import with unmatched instruments requires user mapping
    Given the instrument catalog contains "NVDA.US" with name "Nvidia" and type "STOCK" on market "US" in currency "USD"
    When I upload an XTB XLSX for account "XTB" with transactions:
      | type           | instrument | comment              |
      | Stock purchase | Nvidia     | OPEN BUY 5 @ 900.00 |
    Then the import session status should be "PENDING_REVIEW"
    And the import summary should show 0 matched instruments
    And the import summary should show 1 unmatched instruments
    When I confirm the import with mappings for account "XTB":
      | brokerName | catalogSymbol |
      | Nvidia     | NVDA.US       |
    Then the import session status should be "PENDING_PRICES"
    When I provide prices for the import:
      | symbol  | price  | currency |
      | NVDA.US | 910.00 | USD      |
    Then the import session status should be "COMPLETED"
    And a position for "NVDA.US" should exist with quantity 5

  @FR-021 @FR-032
  @v0.2 @import @xtb
  Scenario: XTB import reuses previously persisted mapping
    Given the instrument catalog contains "AAPL.US" with name "Apple" and type "STOCK" on market "US" in currency "USD"
    And a broker mapping exists for "XTB" mapping "Apple Inc." to "AAPL.US"
    When I upload an XTB XLSX for account "XTB" with transactions:
      | type           | instrument | comment               |
      | Stock purchase | Apple Inc. | OPEN BUY 20 @ 185.00  |
    Then the import session status should be "READY_TO_CONFIRM"
    And the import summary should show 1 matched instruments
    And the import summary should show 0 unmatched instruments
