package com.beowulf.clinical;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import java.util.Map;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CompleteWorkflowIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    void setup() {
        restTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());
    }

    private static Long orderId;
    private static Long studyId;
    private static Long patientId;

    @Test
    @Order(1)
    public void test1_CreateOrderAndVerifyStudy() {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("mrn", "MRN12345");
        request.put("firstName", "John");
        request.put("lastName", "Doe");
        request.put("dateOfBirth", "1980-05-15");
        request.put("type", "XRAY");

        ResponseEntity<Map> response = restTemplate.postForEntity("/api/orders", request, Map.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Map body = response.getBody();
        assertNotNull(body.get("id"));
        assertNotNull(body.get("patientId"));
        assertNotNull(body.get("studyId"));

        orderId = ((Number) body.get("id")).longValue();
        patientId = ((Number) body.get("patientId")).longValue();

        ResponseEntity<Map> studyResponse = restTemplate.getForEntity("/api/orders/" + orderId + "/study", Map.class);
        assertEquals(HttpStatus.OK, studyResponse.getStatusCode());
        Map study = studyResponse.getBody();
        assertEquals("ORDERED", study.get("status"));
        assertEquals(0, ((Number) study.get("version")).intValue());

        studyId = ((Number) study.get("id")).longValue();
    }

    @Test
    @Order(2)
    public void test2_UpdateReportText() {
        Map<String, Object> updateRequest = new LinkedHashMap<>();
        updateRequest.put("reportText", "Normal cardiac findings. No abnormalities detected.");
        updateRequest.put("version", 0);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(updateRequest, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
            "/api/studies/" + studyId, HttpMethod.PATCH, entity, Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Normal cardiac findings. No abnormalities detected.", response.getBody().get("reportText"));
    }

    @Test
    @Order(3)
    public void test3_FinalizeStudy() {
        Map<String, Object> finalizeRequest = new LinkedHashMap<>();
        finalizeRequest.put("status", "FINALIZED");
        finalizeRequest.put("version", 1);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(finalizeRequest, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
            "/api/studies/" + studyId, HttpMethod.PATCH, entity, Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("FINALIZED", response.getBody().get("status"));
    }

    @Test
    @Order(4)
    public void test4_VerifyOrderResult() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
            "/api/orders/" + orderId + "/results", Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map result = response.getBody();
        assertEquals(1, ((Number) result.get("version")).intValue());
        assertEquals("FINALIZED", result.get("status"));
        assertTrue((Boolean) result.get("isCurrent"));
    }

    @Test
    @Order(5)
    public void test5_AmendStudy() {
        Map<String, Object> amendRequest = new LinkedHashMap<>();
        amendRequest.put("status", "AMENDED");
        amendRequest.put("reportText", "Updated findings: Minor calcification noted in the left ventricle.");
        amendRequest.put("version", 2);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(amendRequest, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
            "/api/studies/" + studyId, HttpMethod.PATCH, entity, Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("AMENDED", response.getBody().get("status"));
    }

    @Test
    @Order(6)
    public void test6_VerifyResultHistory() {
        ResponseEntity<Map[]> response = restTemplate.getForEntity(
            "/api/orders/" + orderId + "/results/history", Map[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map[] results = response.getBody();
        assertEquals(2, results.length);

        Map firstResult = results[0];
        assertEquals(1, ((Number) firstResult.get("version")).intValue());
        assertEquals("FINALIZED", firstResult.get("status"));
        assertFalse((Boolean) firstResult.get("isCurrent"));
        assertEquals("Normal cardiac findings. No abnormalities detected.", firstResult.get("report"));
        assertNotNull(firstResult.get("supersededById"));

        Map amendedResult = results[1];
        assertEquals(2, ((Number) amendedResult.get("version")).intValue());
        assertEquals("AMENDED", amendedResult.get("status"));
        assertTrue((Boolean) amendedResult.get("isCurrent"));
        assertEquals("Updated findings: Minor calcification noted in the left ventricle.", amendedResult.get("report"));
        assertNull(amendedResult.get("supersededById"));
    }

    @Test
    @Order(7)
    public void test7_VersionConflict() {
        Map<String, Object> conflictRequest = new LinkedHashMap<>();
        conflictRequest.put("reportText", "Should fail");
        conflictRequest.put("version", 0);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(conflictRequest, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
            "/api/studies/" + studyId, HttpMethod.PATCH, entity, Map.class);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    @Order(8)
    public void test8_PatientUpdate() {
        Map<String, Object> mismatchedOrder = new LinkedHashMap<>();
        mismatchedOrder.put("mrn", "MRN12345");
        mismatchedOrder.put("firstName", "Jonathan");
        mismatchedOrder.put("lastName", "Doe");
        mismatchedOrder.put("dateOfBirth", "1980-05-15");
        mismatchedOrder.put("type", "LAB");

        ResponseEntity<Map> conflictResponse = restTemplate.postForEntity("/api/orders", mismatchedOrder, Map.class);
        assertEquals(HttpStatus.CONFLICT, conflictResponse.getStatusCode());

        Map<String, Object> matchingOrder = new LinkedHashMap<>();
        matchingOrder.put("mrn", "MRN12345");
        matchingOrder.put("firstName", "John");
        matchingOrder.put("lastName", "Doe");
        matchingOrder.put("dateOfBirth", "1980-05-15");
        matchingOrder.put("type", "LAB");

        ResponseEntity<Map> successResponse = restTemplate.postForEntity("/api/orders", matchingOrder, Map.class);
        assertEquals(HttpStatus.CREATED, successResponse.getStatusCode());
        assertEquals(patientId.intValue(), ((Number) successResponse.getBody().get("patientId")).intValue());

        Map<String, Object> updateRequest = new LinkedHashMap<>();
        updateRequest.put("firstName", "Jonathan");
        updateRequest.put("lastName", "Doe");
        updateRequest.put("dateOfBirth", "1980-05-15");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(updateRequest, headers);

        ResponseEntity<Map> updateResponse = restTemplate.exchange(
            "/api/patients/" + patientId, HttpMethod.PUT, entity, Map.class);
        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());

        ResponseEntity<Map> patientResponse = restTemplate.getForEntity("/api/patients/" + patientId, Map.class);
        assertEquals("Jonathan", patientResponse.getBody().get("firstName"));
    }

    @Test
    @Order(9)
    public void test9_CannotDeleteFinalizedStudy() {
        ResponseEntity<Map> response = restTemplate.exchange(
            "/api/studies/" + studyId, HttpMethod.DELETE, null, Map.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @Order(10)
    public void test10_LupusEasterEgg() {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("mrn", "LUPUS");
        request.put("firstName", "Gregory");
        request.put("lastName", "House");
        request.put("dateOfBirth", "1959-06-11");
        request.put("type", "LAB");

        ResponseEntity<Map> response = restTemplate.postForEntity("/api/orders", request, Map.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Map body = response.getBody();
        assertNotNull(body.get("easterEgg"));
        assertTrue(body.get("easterEgg").toString().contains("not lupus"));
        assertEquals("LUPUS", body.get("mrn"));
    }

    @Test
    @Order(11)
    public void test11_NoEasterEggForNormalMRN() {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("mrn", "MRN88888");
        request.put("firstName", "Normal");
        request.put("lastName", "Patient");
        request.put("dateOfBirth", "1990-01-01");
        request.put("type", "XRAY");

        ResponseEntity<Map> response = restTemplate.postForEntity("/api/orders", request, Map.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNull(response.getBody().get("easterEgg"));
    }

    @Test
    @Order(12)
    public void test12_CancelOrderedStudy() {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("mrn", "MRN99999");
        request.put("firstName", "Jane");
        request.put("lastName", "Smith");
        request.put("dateOfBirth", "1990-01-01");
        request.put("type", "MRI");

        ResponseEntity<Map> orderResponse = restTemplate.postForEntity("/api/orders", request, Map.class);
        Long newStudyId = ((Number) orderResponse.getBody().get("studyId")).longValue();

        Map<String, Object> cancelRequest = new LinkedHashMap<>();
        cancelRequest.put("status", "CANCELED");
        cancelRequest.put("version", 0);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(cancelRequest, headers);

        ResponseEntity<Map> cancelResponse = restTemplate.exchange(
            "/api/studies/" + newStudyId, HttpMethod.PATCH, entity, Map.class);

        assertEquals(HttpStatus.OK, cancelResponse.getStatusCode());
        assertEquals("CANCELED", cancelResponse.getBody().get("status"));
    }
}
