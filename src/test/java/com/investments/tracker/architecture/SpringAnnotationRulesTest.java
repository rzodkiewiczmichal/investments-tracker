package com.investments.tracker.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;

/**
 * ArchUnit tests enforcing correct placement of Spring annotations.
 *
 * <p>Ensures that framework annotations are only used in the correct architectural layers, per
 * ADR-003 and project conventions.
 */
@DisplayName("Spring Annotation Placement Rules")
class SpringAnnotationRulesTest {

    private static JavaClasses allClasses;

    @BeforeAll
    static void setUp() {
        allClasses =
                new ClassFileImporter()
                        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                        .importPackages("com.investments.tracker");
    }

    @Test
    @DisplayName("@Service should only be used in application.usecase package")
    void serviceAnnotationOnlyOnUseCases() {
        classes()
                .that()
                .areAnnotatedWith(Service.class)
                .should()
                .resideInAPackage("..application.usecase..")
                .allowEmptyShould(true)
                .check(allClasses);
    }

    @Test
    @DisplayName("@RestController should only be used in infrastructure.web.controller package")
    void restControllerOnlyInWebController() {
        classes()
                .that()
                .areAnnotatedWith(RestController.class)
                .should()
                .resideInAPackage("..infrastructure.web.controller..")
                .allowEmptyShould(true)
                .check(allClasses);
    }

    @Test
    @DisplayName("@Transactional should only be used in application.usecase package")
    void transactionalOnlyOnUseCases() {
        classes()
                .that()
                .areAnnotatedWith(Transactional.class)
                .should()
                .resideInAPackage("..application.usecase..")
                .allowEmptyShould(true)
                .check(allClasses);
    }

    @Test
    @DisplayName("@Repository should only be used in infrastructure.persistence package")
    void repositoryAnnotationOnlyOnAdapters() {
        classes()
                .that()
                .areAnnotatedWith(Repository.class)
                .should()
                .resideInAPackage("..infrastructure.persistence..")
                .allowEmptyShould(true)
                .check(allClasses);
    }

    @Test
    @DisplayName("Domain services should not have Spring @Service annotation")
    void domainServicesShouldNotHaveServiceAnnotation() {
        noClasses()
                .that()
                .resideInAPackage("..domain.service..")
                .should()
                .beAnnotatedWith(Service.class)
                .allowEmptyShould(true)
                .check(allClasses);
    }
}
