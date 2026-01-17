# Language: en
# Domain: Investment Tracking System

Feature: Portfolio Viewing
  As an individual investor with multiple brokerage accounts
  I want to view my aggregated portfolio in one place
  So that I can understand my total financial position

  Background:
    Given I have positions in multiple brokerage accounts
    And all positions have been imported into the system
    And current prices are up-to-date

  @FR-001 @FR-006 @FR-081 @FR-082 @FR-083 @FR-084 @FR-087 @FR-089
  @v0.1 @v0.3 @portfolio @metrics
  Scenario: View total portfolio value
    When I view my portfolio
    Then I should see the total current value in PLN
    And I should see the total invested amount in PLN
    And I should see the total P&L in PLN
    And I should see the total P&L as a percentage
    And I should see the portfolio XIRR percentage

  @FR-002 @FR-081 @FR-082 @FR-083 @FR-084 @FR-089
  @v0.1 @portfolio @metrics
  Scenario: View portfolio with positive returns
    Given my total invested amount is 100000 PLN
    And my total current value is 108000 PLN
    When I view my portfolio
    Then I should see total current value of 108000 PLN
    And I should see total invested amount of 100000 PLN
    And I should see P&L of +8000 PLN
    And I should see P&L percentage of +8.0%

  @FR-003 @FR-081 @FR-082 @FR-083 @FR-084 @FR-089
  @v0.1 @portfolio @metrics
  Scenario: View portfolio with negative returns
    Given my total invested amount is 100000 PLN
    And my total current value is 95000 PLN
    When I view my portfolio
    Then I should see total current value of 95000 PLN
    And I should see total invested amount of 100000 PLN
    And I should see P&L of -5000 PLN
    And I should see P&L percentage of -5.0%

  @FR-005 @FR-030 @FR-071 @FR-081 @FR-082 @FR-083 @FR-085 @FR-093
  @v0.2 @portfolio @multi-account @aggregation
  Scenario: View aggregated positions across accounts
    Given I own 50 shares of "Apple" in account "Broker A" bought at 500 PLN
    And I own 30 shares of "Apple" in account "Broker B" bought at 520 PLN
    And the current price of "Apple" is 550 PLN
    When I view my portfolio
    Then I should see a single position for "Apple" with 80 shares
    And the average cost basis should be 507.50 PLN
    And the current value should be 44000 PLN
    And the P&L should be +3400 PLN

  @FR-004 @FR-089
  @v0.1 @portfolio
  Scenario: View empty portfolio
    Given I have no positions in any account
    When I view my portfolio
    Then I should see total current value of 0 PLN
    And I should see total invested amount of 0 PLN
    And I should see a message "No positions found"
