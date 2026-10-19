package com.example.processo.core;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * Motor e segmentos vivem no mesmo módulo, então o Maven não impede que um importe o outro. Estas regras fazem
 * esse papel: o motor continua único (não conhece segmento nenhum) e os domínios continuam Java puro.
 */
class ArquiteturaTest {

    private static final JavaClasses CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.example.processo.core");

    @Test
    void motorNaoConheceSegmentos() {
        noClasses().that().resideInAPackage("..core.motor..")
                .should().dependOnClassesThat().resideInAPackage("..core.segmento..")
                .check(CLASSES);
    }

    @Test
    void segmentosNaoSeEnxergam() {
        // Cada segmento é uma fatia (core.segmento.<nome>): fatias não podem depender umas das outras
        slices().matching("com.example.processo.core.segmento.(*)..").should().notDependOnEachOther()
                .check(CLASSES);
    }

    @Test
    void dominiosNaoConhecemInfraestrutura() {
        // O domain de um segmento pode usar o domain do motor (SPI, Especificacao), nada além disso
        noClasses().that().resideInAnyPackage("..motor.domain..", "..segmento.*.domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "jakarta..", "io.quarkus..", "org.hibernate..", "com.example.jpacore..",
                        "..application..", "..adapter..")
                .check(CLASSES);
    }
}
