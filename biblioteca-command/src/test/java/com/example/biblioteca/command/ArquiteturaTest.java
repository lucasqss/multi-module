package com.example.biblioteca.command;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * O domínio vive no mesmo módulo que os adapters, então o Maven não impede que ele importe JPA/Quarkus.
 * Esta regra faz esse papel: o pacote domain só pode depender de Java puro.
 */
class ArquiteturaTest {

    @Test
    void dominioNaoConheceInfraestrutura() {
        var classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.example.biblioteca.command");

        noClasses().that().resideInAPackage("..command.domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "jakarta..", "io.quarkus..", "org.hibernate..", "com.example.jpacore..",
                        "..command.application..", "..command.adapter..")
                .check(classes);
    }
}
