# Language: en
# Domain: Investment Tracking System

Feature: Position Details
  As an individual investor
  I want to view detailed information about specific positions
  So that I can understand individual investment performance

  Background:
    Given I have positions in multiple instruments
    And all positions have been imported into the system
    And current prices are up-to-date

  Scenario: View individual stock position
    Given I own 100 shares of "Microsoft" with average cost of 1200 PLN
    And the current price of "Microsoft" is 1400 PLN
    When I view the position details for "Microsoft"
    Then I should see quantity of 100 shares
    And I should see average cost basis of 1200 PLN
    And I should see invested amount of 120000 PLN
    And I should see current value of 140000 PLN
    And I should see P&L of +20000 PLN
    And I should see P&L percentage of +16.67%
    And I should see the position XIRR if available

  Scenario: View ETF position with loss
    Given I own 50 units of "S&P 500 ETF" with average cost of 1800 PLN
    And the current price of "S&P 500 ETF" is 1700 PLN
    When I view the position details for "S&P 500 ETF"
    Then I should see quantity of 50 units
    And I should see average cost basis of 1800 PLN
    And I should see invested amount of 90000 PLN
    And I should see current value of 85000 PLN
    And I should see P&L of -5000 PLN
    And I should see P&L percentage of -5.56%

  Scenario: View Polish government bond position
    Given I own Polish government bonds with invested amount of 50000 PLN
    And the current value of these bonds is 52500 PLN
    When I view the position details for Polish government bonds
    Then I should see invested amount of 50000 PLN
    And I should see current value of 52500 PLN
    And I should see P&L of +2500 PLN
    And I should see P&L percentage of +5.0%

  Scenario: List all positions
    Given I own 100 shares of "Apple"
    And I own 50 shares of "Microsoft"
    And I own 200 units of "WIG20 ETF"
    When I view the positions list
    Then I should see 3 positions
    And each position should show instrument name
    And each position should show current value
    And each position should show P&L percentage
    And positions should be sorted by current value descending