package com.investments.tracker.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * ArchUnit tests enforcing domain layer purity.
 * <p>
 * Ensures domain layer has no dependencies on:
 * - Spring Framework
 * - JPA/Hibernate
 * - Jakarta/JavaEE annotations
 * - Any infrastructure concerns
 * </p>
 * <p>
 * Note: Tests will pass if domain package is empty (no classes exist yet).
 * They will fail only when code is added that violates the rules.
 * </p>
 */
@DisplayName("Domain Layer Purity Tests")
class DomainLayerRulesTest {

    private static JavaClasses allClasses;

    @BeforeAll
    static void setUp() {
        allClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.investments.tracker");
    }

    @Test
    @DisplayName("Domain layer should not depend on Spring Framework")
    void domainShouldNotDependOnSpring() {
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage("org.springframework..")
                .allowEmptyShould(true)
                .check(allClasses);
    }

    @Test
    @DisplayName("Domain layer should not depend on JPA/Hibernate")
    void domainShouldNotDependOnJpa() {
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "jakarta.persistence..",
                        "org.hibernate.."
                )
                .allowEmptyShould(true)
                .check(allClasses);
    }

    @Test
    @DisplayName("Domain layer should not depend on infrastructure package")
    void domainShouldNotDependOnInfrastructure() {
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("..infrastructure..")
                .allowEmptyShould(true)
                .check(allClasses);
    }

    @Test
    @DisplayName("Domain layer should not depend on application package")
    void domainShouldNotDependOnApplication() {
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("..application..")
                .allowEmptyShould(true)
                .check(allClasses);
    }
}
