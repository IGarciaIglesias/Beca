package com.example.StudentsApplication.external;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

/*
 * Clase base que arranca un WireMockServer en puerto dinámico para cada test.
 * Expone baseUrl para construir las peticiones HTTP reales contra el mock.
 */
public abstract class WireMockSupport {

    protected WireMockServer wm;
    protected String baseUrl;
    protected final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void startServer() {
        wm = new WireMockServer(wireMockConfig().dynamicPort());
        wm.start();
        baseUrl = "http://localhost:" + wm.port();
    }

    @AfterEach
    void stopServer() {
        if (wm != null) wm.stop();
    }
}