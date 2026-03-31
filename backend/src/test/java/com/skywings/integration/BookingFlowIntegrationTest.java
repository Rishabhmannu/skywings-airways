package com.skywings.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skywings.dto.request.LoginRequest;
import com.skywings.dto.request.SignupRequest;
import com.skywings.dto.response.AuthResponse;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BookingFlowIntegrationTest {

    @LocalServerPort private int port;
    @Autowired private TestRestTemplate restTemplate;
    @Autowired private ObjectMapper objectMapper;

    private static String accessToken;
    private static final String TEST_EMAIL = "inttest_" + System.currentTimeMillis() + "@test.com";
    private static final String TEST_PHONE = "+91" + (9000000000L + System.currentTimeMillis() % 1000000000L);

    private String url(String path) {
        return "http://localhost:" + port + "/api" + path;
    }

    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (accessToken != null) {
            headers.setBearerAuth(accessToken);
        }
        return headers;
    }

    @Test
    @Order(1)
    void step1_signup_shouldCreateUserAndReturnToken() {
        SignupRequest signup = new SignupRequest("IntTest User", TEST_EMAIL, "Pass@123", TEST_PHONE);

        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                url("/auth/signup"), signup, AuthResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getAccessToken()).isNotBlank();
        assertThat(response.getBody().getRole()).isEqualTo("PASSENGER");
        accessToken = response.getBody().getAccessToken();
    }

    @Test
    @Order(2)
    void step2_searchFlights_shouldReturnOkStatus() {
        // Search is public, should always return 200 even if no flights match
        ResponseEntity<String> response = restTemplate.getForEntity(
                url("/flights/search?origin=DEL&dest=BOM&date=2030-01-01"), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @Order(3)
    void step3_adminEndpoint_shouldBeForbiddenForPassenger() {
        HttpEntity<Void> entity = new HttpEntity<>(authHeaders());

        ResponseEntity<String> response = restTemplate.exchange(
                url("/admin/flights"), HttpMethod.GET, entity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Order(4)
    void step4_loginAsAdmin_shouldReturnAdminRole() {
        LoginRequest login = new LoginRequest("admin@skywings.com", "Admin@123");

        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                url("/auth/login"), login, AuthResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getRole()).isEqualTo("ADMIN");
    }

    @Test
    @Order(5)
    void step5_getProfile_shouldReturnUserDetails() {
        Assumptions.assumeTrue(accessToken != null, "Skipped — signup didn't set token");

        HttpEntity<Void> entity = new HttpEntity<>(authHeaders());

        ResponseEntity<String> response = restTemplate.exchange(
                url("/users/profile"), HttpMethod.GET, entity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains(TEST_EMAIL);
    }

    @Test
    @Order(6)
    void step6_duplicateSignup_shouldReturn409() {
        Assumptions.assumeTrue(accessToken != null, "Skipped — original signup didn't succeed");

        SignupRequest signup = new SignupRequest("Dupe", TEST_EMAIL, "Pass@123", "+918888888888");

        ResponseEntity<String> response = restTemplate.postForEntity(
                url("/auth/signup"), signup, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @Order(7)
    void step7_weakPassword_shouldReturn400() {
        SignupRequest signup = new SignupRequest("Test", "weakpwd@test.com", "123", "+917777777777");

        ResponseEntity<String> response = restTemplate.postForEntity(
                url("/auth/signup"), signup, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Order(8)
    void step8_unauthenticatedBookingsAccess_shouldBeRejected() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                url("/bookings"), String.class);

        assertThat(response.getStatusCode().value()).isIn(401, 403);
    }
}
