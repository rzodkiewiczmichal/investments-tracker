package com.investments.tracker.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;

/**
 * ArchUnit tests enforcing naming conventions across the codebase.
 *
 * <p>Ensures consistent naming for JDBC entities, repositories, use cases, controllers, DTOs,
 * mappers, and domain services.
 *
 * <p>Note: Tests will pass if packages are empty (no classes exist yet). They will fail only when
 * code is added that violates the rules.
 */
@DisplayName("Naming Convention Tests")
class NamingConventionTest {

    private static JavaClasses allClasses;

    @BeforeAll
    static void setUp() {
        allClasses =
                new ClassFileImporter()
                        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                        .importPackages("com.investments.tracker");
    }

    @Test
    @DisplayName("JDBC entities should be named *JdbcEntity")
    void jdbcEntitiesShouldBeNamedCorrectly() {
        classes()
                .that()
                .resideInAPackage("..infrastructure.persistence.entity..")
                .and()
                .areAnnotatedWith("org.springframework.data.relational.core.mapping.Table")
                .should()
                .haveSimpleNameEndingWith("JdbcEntity")
                .allowEmptyShould(true)
                .check(allClasses);
    }

    @Test
    @DisplayName("Domain repository interfaces should be named *Repository, *Provider, or *Cache")
    void repositoriesShouldBeNamedCorrectly() {
        classes()
                .that()
                .resideInAPackage("..domain.repository..")
                .and()
                .areInterfaces()
                .should()
                .haveSimpleNameEndingWith("Repository")
                .orShould()
                .haveSimpleNameEndingWith("Provider")
                .orShould()
                .haveSimpleNameEndingWith("Cache")
                .allowEmptyShould(true)
                .check(allClasses);
    }

    @Test
    @DisplayName("Use case interfaces should be named *UseCase")
    void useCaseInterfacesShouldBeNamedCorrectly() {
        classes()
                .that()
                .resideInAPackage("..application.usecase..")
                .and()
                .areInterfaces()
                .should()
                .haveSimpleNameEndingWith("UseCase")
                .allowEmptyShould(true)
                .check(allClasses);
    }

    @Test
    @DisplayName("Use case implementations should be named *UseCaseService")
    void useCaseServicesShouldBeNamedCorrectly() {
        classes()
                .that()
                .resideInAPackage("..application.usecase..")
                .and()
                .areNotInterfaces()
                .should()
                .haveSimpleNameEndingWith("UseCaseService")
                .allowEmptyShould(true)
                .check(allClasses);
    }

    @Test
    @DisplayName("REST controllers should be named *Controller")
    void controllersShouldBeNamedCorrectly() {
        classes()
                .that()
                .resideInAPackage("..infrastructure.web.controller..")
                .and()
                .areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
                .should()
                .haveSimpleNameEndingWith("Controller")
                .allowEmptyShould(true)
                .check(allClasses);
    }

    @Test
    @DisplayName("DTOs should be named *DTO, *Request, *Response, or *Command")
    void dtosShouldBeNamedCorrectly() {
        classes()
                .that()
                .resideInAPackage("..application.dto..")
                .and()
                .resideOutsideOfPackage("..application.dto.mapper..")
                .and()
                .areNotInterfaces()
                .should()
                .haveSimpleNameEndingWith("DTO")
                .orShould()
                .haveSimpleNameEndingWith("Request")
                .orShould()
                .haveSimpleNameEndingWith("Command")
                .orShould()
                .haveSimpleNameEndingWith("Response")
                .allowEmptyShould(true)
                .check(allClasses);
    }

    @Test
    @DisplayName("Mappers should be named *Mapper")
    void mappersShouldBeNamedCorrectly() {
        classes()
                .that()
                .resideInAPackage("..application.dto.mapper..")
                .and()
                .areNotInterfaces()
                .and()
                .areTopLevelClasses()
                .should()
                .haveSimpleNameEndingWith("Mapper")
                .allowEmptyShould(true)
                .check(allClasses);
    }

    @Test
    @DisplayName("Domain services should be named *Service")
    void domainServicesShouldBeNamedCorrectly() {
        classes()
                .that()
                .resideInAPackage("..domain.service..")
                .and()
                .areNotInterfaces()
                .should()
                .haveSimpleNameEndingWith("Service")
                .allowEmptyShould(true)
                .check(allClasses);
    }

    @Test
    @DisplayName("Value objects should not have *Entity suffix")
    void valueObjectsShouldNotHaveEntitySuffix() {
        classes()
                .that()
                .resideInAPackage("..domain.model.value..")
                .should()
                .haveSimpleNameNotEndingWith("Entity")
                .allowEmptyShould(true)
                .check(allClasses);
    }

    @Test
    @DisplayName("Domain model classes should not have Jpa or Jdbc prefix/suffix")
    void domainModelShouldNotHavePersistenceInName() {
        classes()
                .that()
                .resideInAPackage("..domain.model..")
                .should()
                .haveSimpleNameNotContaining("Jpa")
                .andShould()
                .haveSimpleNameNotContaining("Jdbc")
                .allowEmptyShould(true)
                .check(allClasses);
    }
}
