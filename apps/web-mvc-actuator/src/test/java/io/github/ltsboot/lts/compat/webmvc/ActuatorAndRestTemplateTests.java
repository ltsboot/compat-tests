package io.github.ltsboot.lts.compat.webmvc;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@SuppressWarnings("unchecked")
class ActuatorAndRestTemplateTests {

    @Autowired
    private TestRestTemplate rest;

    @Test
    void healthEndpointReportsUp() {
        ResponseEntity<Map<String, Object>> response = rest.getForEntity("/actuator/health", (Class<Map<String, Object>>) (Class<?>) Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("status", "UP");
    }

    @Test
    void infoEndpointExposesConfiguredAppName() {
        Map<String, Object> info = rest.getForObject("/actuator/info", Map.class);

        assertThat(info).containsKey("app");
        assertThat((Map<String, Object>) info.get("app")).containsEntry("name", "web-mvc-actuator");
    }

    @Test
    void endpointsNotInExposureListAreHidden() {
        ResponseEntity<String> response = rest.getForEntity("/actuator/env", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void createItemOverRealHttpStack() {
        ItemRequest request = new ItemRequest();
        request.setName("nut");
        request.setQuantity(5);

        ResponseEntity<Map> response = rest.postForEntity("/api/items", request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).containsEntry("name", "nut").containsEntry("quantity", 5);
    }
}
