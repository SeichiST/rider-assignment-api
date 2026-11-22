package com.softer.riderassignment.integration;

import com.softer.riderassignment.model.AssignmentStatus;
import com.softer.riderassignment.model.RiderAssignment;
import com.softer.riderassignment.repository.RiderAssignmentRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class RiderAssignmentIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private RiderAssignmentRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void shouldCreateAndRetrieveAssignment() {

        RiderAssignment request = new RiderAssignment();
        request.setDeliveryId(2001L);
        request.setRiderId(601L);
        request.setStatus(AssignmentStatus.ASSIGNED);

        ResponseEntity<RiderAssignment> createResponse =
                restTemplate.postForEntity(
                        "/api/assignments",
                        request,
                        RiderAssignment.class
                );

        assertEquals(201, createResponse.getStatusCode().value());

        RiderAssignment created = createResponse.getBody();

        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals(2001L, created.getDeliveryId());
        assertEquals(601L, created.getRiderId());

        ResponseEntity<RiderAssignment> getResponse =
                restTemplate.getForEntity(
                        "/api/assignments/" + created.getId(),
                        RiderAssignment.class
                );

        assertEquals(200, getResponse.getStatusCode().value());

        RiderAssignment retrieved = getResponse.getBody();

        assertNotNull(retrieved);
        assertEquals(created.getId(), retrieved.getId());
        assertEquals(AssignmentStatus.ASSIGNED, retrieved.getStatus());
    }
}
