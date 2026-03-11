package com.example.StudentsApplication.serverTEST;

import org.junit.jupiter.api.Test;
import java.net.URI;
import java.net.http.*;

import static org.junit.jupiter.api.Assertions.*;

class RealStudentsCacheIT {

    private final String baseUrl = "http://localhost:8080";
    private final HttpClient client = HttpClient.newHttpClient();

    @Test
    void refresh_cache_endpoint_works() throws Exception {

        var res = client.send(
                HttpRequest.newBuilder(URI.create(baseUrl + "/students/refresh"))
                        .GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(200, res.statusCode());
        assertTrue(res.body().contains("\"status\""));
        assertTrue(res.body().contains("students"));
    }
}