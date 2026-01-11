package com.investments.tracker.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * ArchUnit tests enforcing strict dependency rules.
 * <p>
 * Ensures that:
 * - Domain layer uses ONLY Java standard library (no external libraries)
 * - Application layer uses ONLY Java standard library and domain classes
 * - Infrastructure layer is the only place for framework dependencies
 * </p>
 * <p>
 * Note: Tests will pass if packages are empty (no classes exist yet).
 * They will fail only when code is added that violates the rules.
 * </p>
 */
@DisplayName("Strict Dependency Rules Tests")
class DependencyRulesTest {

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
    @DisplayName("Domain layer should not use JPA/Hibernate")
    void domainShouldNotUseJpaHibernate() {
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
    @DisplayName("Domain layer should not use Lombok")
    void domainShouldNotUseLombok() {
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage("lombok..")
                .allowEmptyShould(true)
                .check(allClasses);
    }

    @Test
    @DisplayName("Application layer should not depend on Spring Framework")
    void applicationShouldNotDependOnSpring() {
        noClasses()
                .that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAnyPackage("org.springframework..")
                .allowEmptyShould(true)
                .check(allClasses);
    }

    @Test
    @DisplayName("Application layer should not use JPA/Hibernate")
    void applicationShouldNotUseJpaHibernate() {
        noClasses()
                .that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "jakarta.persistence..",
                        "org.hibernate.."
                )
                .allowEmptyShould(true)
                .check(allClasses);
    }

    @Test
    @DisplayName("Application layer should not use Lombok")
    void applicationShouldNotUseLombok() {
        noClasses()
                .that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAnyPackage("lombok..")
                .allowEmptyShould(true)
                .check(allClasses);
    }

    @Test
    @DisplayName("Domain and application layers should not use Jackson")
    void domainAndApplicationShouldNotUseJackson() {
        noClasses()
                .that().resideInAnyPackage("..domain..", "..application..")
                .should().dependOnClassesThat().resideInAnyPackage("com.fasterxml.jackson..")
                .allowEmptyShould(true)
                .check(allClasses);
    }

    @Test
    @DisplayName("Domain and application layers should not use logging frameworks")
    void domainAndApplicationShouldNotUseLogging() {
        noClasses()
                .that().resideInAnyPackage("..domain..", "..application..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.slf4j..",
                        "ch.qos.logback..",
                        "org.apache.logging..",
                        "java.util.logging.."
                )
                .allowEmptyShould(true)
                .check(allClasses);
    }
}
