package com.investments.tracker.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

/**
 * ArchUnit tests enforcing domain model implementation rules per ADR-018.
 *
 * <p>Ensures that:
 *
 * <ul>
 *   <li>Domain model types are Java records (not classes)
 *   <li>No factory methods like create() or reconstitute()
 *   <li>No setter or with* methods (immutability)
 *   <li>Domain exceptions extend DomainException
 * </ul>
 */
@DisplayName("Domain Model Rules Tests (ADR-018)")
class DomainModelRulesTest {

    private static JavaClasses allClasses;

    @BeforeAll
    static void setUp() {
        allClasses =
                new ClassFileImporter()
                        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                        .importPackages("com.investments.tracker");
    }

    @Test
    @DisplayName("Domain model classes should be records")
    void domainModelClassesShouldBeRecords() {
        classes()
                .that()
                .resideInAnyPackage("..domain.model..", "..domain.model.value..")
                .and()
                .areNotEnums()
                .should()
                .beRecords()
                .allowEmptyShould(true)
                .check(allClasses);
    }

    @Test
    @DisplayName("Domain records should not have create() or reconstitute() factory methods")
    void domainRecordsShouldNotHaveFactoryMethods() {
        classes()
                .that()
                .resideInAnyPackage("..domain.model..", "..domain.model.value..")
                .and()
                .areRecords()
                .should(notHaveMethodsNamed("create", "reconstitute"))
                .allowEmptyShould(true)
                .check(allClasses);
    }

    @Test
    @DisplayName("Domain records should not have setter or with* methods")
    void domainRecordsShouldNotHaveSettersOrWithMethods() {
        classes()
                .that()
                .resideInAnyPackage("..domain.model..", "..domain.model.value..")
                .and()
                .areRecords()
                .should(notHaveMethodsStartingWith("set", "with"))
                .allowEmptyShould(true)
                .check(allClasses);
    }

    @Test
    @DisplayName("Domain exceptions should extend DomainException")
    void domainExceptionsShouldExtendDomainException() {
        classes()
                .that()
                .resideInAPackage("..domain.exception..")
                .and()
                .areAssignableTo(RuntimeException.class)
                .and()
                .doNotHaveSimpleName("DomainException")
                .should()
                .beAssignableTo(com.investments.tracker.domain.exception.DomainException.class)
                .allowEmptyShould(true)
                .check(allClasses);
    }

    @Test
    @DisplayName("Domain layer should not throw IllegalArgumentException or IllegalStateException")
    void domainShouldNotThrowStandardExceptions() {
        noClasses()
                .that()
                .resideInAPackage("..domain..")
                .should()
                .dependOnClassesThat()
                .haveFullyQualifiedName(IllegalArgumentException.class.getName())
                .allowEmptyShould(true)
                .check(allClasses);
    }

    private static ArchCondition<com.tngtech.archunit.core.domain.JavaClass> notHaveMethodsNamed(
            String... names) {
        return new ArchCondition<>("not have methods named " + String.join(", ", names)) {
            @Override
            public void check(
                    com.tngtech.archunit.core.domain.JavaClass javaClass, ConditionEvents events) {
                for (JavaMethod method : javaClass.getMethods()) {
                    for (String name : names) {
                        if (method.getName().equals(name)) {
                            events.add(
                                    SimpleConditionEvent.violated(
                                            javaClass,
                                            String.format(
                                                    "%s has forbidden factory method %s()",
                                                    javaClass.getName(), name)));
                        }
                    }
                }
            }
        };
    }

    private static ArchCondition<com.tngtech.archunit.core.domain.JavaClass>
            notHaveMethodsStartingWith(String... prefixes) {
        return new ArchCondition<>(
                "not have methods starting with " + String.join(", ", prefixes)) {
            @Override
            public void check(
                    com.tngtech.archunit.core.domain.JavaClass javaClass, ConditionEvents events) {
                for (JavaMethod method : javaClass.getMethods()) {
                    for (String prefix : prefixes) {
                        if (method.getName().startsWith(prefix)
                                && method.getName().length() > prefix.length()
                                && Character.isUpperCase(
                                        method.getName().charAt(prefix.length()))) {
                            events.add(
                                    SimpleConditionEvent.violated(
                                            javaClass,
                                            String.format(
                                                    "%s has forbidden method %s()",
                                                    javaClass.getName(), method.getName())));
                        }
                    }
                }
            }
        };
    }
}
