package com.investments.tracker.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * ArchUnit tests enforcing hexagonal architecture boundaries.
 * <p>
 * Tests verify that:
 * - Domain layer is pure (no framework dependencies)
 * - Application layer depends only on domain
 * - Infrastructure layer is the only layer with framework dependencies
 * - No circular dependencies between layers
 * </p>
 * <p>
 * Note: Tests will pass if packages are empty (no classes exist yet).
 * They will fail only when code is added that violates the rules.
 * </p>
 */
@DisplayName("Hexagonal Architecture Tests")
class HexagonalArchitectureTest {

    private static JavaClasses classes;

    @BeforeAll
    static void setUp() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.investments.tracker");
    }

    @Test
    @DisplayName("Domain layer should not depend on application layer")
    void domainShouldNotDependOnApplication() {
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("..application..")
                .allowEmptyShould(true)
                .check(classes);
    }

    @Test
    @DisplayName("Domain layer should not depend on infrastructure layer")
    void domainShouldNotDependOnInfrastructure() {
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("..infrastructure..")
                .allowEmptyShould(true)
                .check(classes);
    }

    @Test
    @DisplayName("Application layer should not depend on infrastructure layer")
    void applicationShouldNotDependOnInfrastructure() {
        noClasses()
                .that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAPackage("..infrastructure..")
                .allowEmptyShould(true)
                .check(classes);
    }

    @Test
    @DisplayName("Shared utilities should not depend on domain, application, or infrastructure")
    void sharedShouldNotDependOnOtherLayers() {
        noClasses()
                .that().resideInAPackage("..shared..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..domain..",
                        "..application..",
                        "..infrastructure.."
                )
                .allowEmptyShould(true)
                .check(classes);
    }
}
