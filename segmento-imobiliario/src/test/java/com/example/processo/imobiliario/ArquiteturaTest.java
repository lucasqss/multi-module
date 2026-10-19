package com.example.processo.imobiliario;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/** O domain do segmento só conhece Java puro e o domain do motor. */
class ArquiteturaTest {

    @Test
    void dominioNaoConheceInfraestrutura() {
        var classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.example.processo.imobiliario");

        noClasses().that().resideInAPackage("..imobiliario.domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "jakarta..", "io.quarkus..", "org.hibernate..", "com.example.jpacore..",
                        "..imobiliario.application..", "..imobiliario.adapter..",
                        "..motor.application..", "..motor.adapter..")
                .check(classes);
    }
}
