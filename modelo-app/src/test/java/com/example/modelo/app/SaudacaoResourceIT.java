package com.example.modelo.app;

import io.quarkus.test.junit.QuarkusIntegrationTest;

/** Os mesmos cenários contra o jar empacotado (fase verify, via failsafe). */
@QuarkusIntegrationTest
class SaudacaoResourceIT extends SaudacaoResourceTest {
}
