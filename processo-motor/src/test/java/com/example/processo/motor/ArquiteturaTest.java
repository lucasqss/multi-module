package com.example.processo.motor;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/** O pacote domain do motor (incluindo o SPI Segmento) só pode depender de Java puro. */
class ArquiteturaTest {

    @Test
    void dominioNaoConheceInfraestrutura() {
        var classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.example.processo.motor");

        noClasses().that().resideInAPackage("..motor.domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "jakarta..", "io.quarkus..", "org.hibernate..", "com.example.jpacore..",
                        "..motor.application..", "..motor.adapter..")
                .check(classes);
    }
}
