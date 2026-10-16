package com.example.biblioteca.command;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * O domínio vive no mesmo módulo que os adapters, então o Maven não impede que ele importe JPA/Quarkus.
 * Esta regra faz esse papel: o pacote domain só pode depender de Java puro.
 */
class ArquiteturaTest {

    @Test
    void dominioNaoConheceInfraestrutura() {
        // O DO_NOT_INCLUDE_TESTS do ArchUnit procura target/test-classes e não reconhece o layout
        // target/<artifactId>/test-classes deste build: as classes de teste são filtradas pelo caminho
        var classes = new ClassFileImporter()
                .withImportOption(local -> !local.contains("/test-classes/"))
                .importPackages("com.example.biblioteca.command");

        noClasses().that().resideInAPackage("..command.domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "jakarta..", "io.quarkus..", "org.hibernate..", "com.example.jpacore..",
                        "..command.application..", "..command.adapter..")
                .check(classes);
    }
}
