package com.investments.tracker.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

/**
 * ArchUnit tests enforcing naming conventions.
 * <p>
 * Ensures consistent naming across the codebase:
 * - JPA entities end with "JpaEntity"
 * - Repositories end with "Repository"
 * - Use cases/services end with "Service" or "UseCase"
 * - DTOs end with "DTO", "Request", or "Command"
 * - REST controllers end with "Controller"
 * </p>
 * <p>
 * Note: Tests will pass if packages are empty (no classes exist yet).
 * They will fail only when code is added that violates the rules.
 * </p>
 */
@DisplayName("Naming Convention Tests")
class NamingConventionTest {

    private static JavaClasses allClasses;

    @BeforeAll
    static void setUp() {
        allClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.investments.tracker");
    }

    @Test
    @DisplayName("JPA entities should be named *JpaEntity")
    void jpaEntitiesShouldBeNamedCorrectly() {
        classes()
                .that().resideInAPackage("..infrastructure.adapter.out.persistence.jpa.entity..")
                .and().areAnnotatedWith("jakarta.persistence.Entity")
                .should().haveSimpleNameEndingWith("JpaEntity")
                .allowEmptyShould(true)
                .check(allClasses);
    }

    @Test
    @DisplayName("Repositories should be named *Repository")
    void repositoriesShouldBeNamedCorrectly() {
        classes()
                .that().resideInAPackage("..application.port.out..")
                .and().areInterfaces()
                .should().haveSimpleNameEndingWith("Repository")
                .allowEmptyShould(true)
                .check(allClasses);
    }

    @Test
    @DisplayName("Application services should be named *Service")
    void applicationServicesShouldBeNamedCorrectly() {
        classes()
                .that().resideInAPackage("..application.service..")
                .and().areNotInterfaces()
                .should().haveSimpleNameEndingWith("Service")
                .allowEmptyShould(true)
                .check(allClasses);
    }

    @Test
    @DisplayName("REST controllers should be named *Controller")
    void controllersShouldBeNamedCorrectly() {
        classes()
                .that().resideInAPackage("..infrastructure.adapter.in.web..")
                .and().areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
                .should().haveSimpleNameEndingWith("Controller")
                .allowEmptyShould(true)
                .check(allClasses);
    }

    @Test
    @DisplayName("DTOs should be named *DTO, *Request, or *Command")
    void dtosShouldBeNamedCorrectly() {
        classes()
                .that().resideInAPackage("..application.dto..")
                .and().areNotInterfaces()
                .should().haveSimpleNameEndingWith("DTO")
                .orShould().haveSimpleNameEndingWith("Request")
                .orShould().haveSimpleNameEndingWith("Command")
                .orShould().haveSimpleNameEndingWith("Response")
                .allowEmptyShould(true)
                .check(allClasses);
    }

    @Test
    @DisplayName("Domain services should be named *Service")
    void domainServicesShouldBeNamedCorrectly() {
        classes()
                .that().resideInAPackage("..domain.service..")
                .and().areNotInterfaces()
                .should().haveSimpleNameEndingWith("Service")
                .allowEmptyShould(true)
                .check(allClasses);
    }

    @Test
    @DisplayName("Value objects should not have *Entity suffix")
    void valueObjectsShouldNotHaveEntitySuffix() {
        classes()
                .that().resideInAPackage("..domain.model.valueobjects..")
                .should().haveSimpleNameNotEndingWith("Entity")
                .allowEmptyShould(true)
                .check(allClasses);
    }

    @Test
    @DisplayName("Domain entities should not have Jpa prefix or suffix")
    void domainEntitiesShouldNotHaveJpaInName() {
        classes()
                .that().resideInAPackage("..domain.model.aggregates..")
                .should().haveSimpleNameNotContaining("Jpa")
                .allowEmptyShould(true)
                .check(allClasses);
    }
}
