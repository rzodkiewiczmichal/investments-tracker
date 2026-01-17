package com.investments.tracker.cucumber;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.FEATURES_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.FILTER_TAGS_PROPERTY_NAME;

/**
 * JUnit 5 runner for Cucumber BDD tests.
 * <p>
 * Configuration:
 * - Features: loaded from classpath (src/test/resources/features)
 * - Glue: step definitions in com.investments.tracker.cucumber.steps
 * - Plugins: pretty output, HTML report, JSON report
 * </p>
 * <p>
 * Run with: ./gradlew cucumberTest
 * </p>
 *
 * @see CucumberSpringConfiguration
 * @see <a href="../../docs/adr/ADR-012-test-architecture.md">ADR-012: Test Architecture</a>
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.investments.tracker.cucumber")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty, html:build/reports/cucumber/cucumber-report.html, json:build/reports/cucumber/cucumber-report.json")
@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "@v0.1 and not @ignored")
public class RunCucumberTest {
    // This class is used only as a holder for the above annotations
}
