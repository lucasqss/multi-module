package com.example.processo.core;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * Motor e segmentos vivem no mesmo módulo, então o Maven não impede que um importe o outro, nem que uma camada
 * pule outra. Estas regras fazem esse papel, em cada contexto (motor, segmento.*):
 *
 * <pre>
 * adapter / config  →  application (port.out, port.spi, service)  →  domain (model, state, rule, ...)
 * </pre>
 *
 * Interface só onde há costura real (port.out e port.spi): dentro do contexto, injeta-se a classe.
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
    void dominioNaoConheceApplicationNemInfraestrutura() {
        // O domain de um segmento pode usar o domain do motor (modelo, regras), nada além disso
        noClasses().that().resideInAPackage("..core..domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "jakarta..", "io.quarkus..", "org.hibernate..", "com.example.jpacore..",
                        "..core..application..", "..core..adapter..", "..core..config..")
                .check(CLASSES);
    }

    @Test
    void applicationNaoConheceAdapters() {
        // Os adapters implementam os ports; a application só conhece as interfaces
        noClasses().that().resideInAPackage("..core..application..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..core..adapter..", "..core..config..", "com.example.jpacore..")
                .check(CLASSES);
    }
}
