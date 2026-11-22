package com.softer.riderassignment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softer.riderassignment.exception.GlobalExceptionHandler;
import com.softer.riderassignment.exception.ResourceNotFoundException;
import com.softer.riderassignment.model.AssignmentStatus;
import com.softer.riderassignment.model.RiderAssignment;
import com.softer.riderassignment.service.RiderAssignmentService;
import com.softer.riderassignment.exception.BusinessRuleException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RiderAssignmentController.class)
@Import(GlobalExceptionHandler.class)
class RiderAssignmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RiderAssignmentService service;

    @Test
    void shouldReturnAllAssignments() throws Exception {
        when(service.findAll()).thenReturn(List.of(sample()));

        mockMvc.perform(get("/api/assignments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].deliveryId").value(1001))
                .andExpect(jsonPath("$[0].riderId").value(501));
    }

    @Test
    void shouldReturnAssignmentById() throws Exception {
        when(service.findById(1L)).thenReturn(sample());

        mockMvc.perform(get("/api/assignments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("ASSIGNED"));
    }

    @Test
    void shouldCreateAssignment() throws Exception {
        when(service.create(any(RiderAssignment.class))).thenReturn(sample());

        mockMvc.perform(post("/api/assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sample())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.deliveryId").value(1001));
    }

    @Test
    void shouldRejectInvalidAssignment() throws Exception {
        RiderAssignment invalid = new RiderAssignment(
                null, 0L, 501L, AssignmentStatus.ASSIGNED, null);

        mockMvc.perform(post("/api/assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());

        verify(service, never()).create(any());
    }


    @Test
    void shouldUpdateAssignment() throws Exception {
        RiderAssignment updated = sample();
        updated.setRiderId(502L);
        updated.setStatus(AssignmentStatus.ACCEPTED);

        when(service.update(eq(1L), any(RiderAssignment.class)))
                .thenReturn(updated);

        mockMvc.perform(put("/api/assignments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.riderId").value(502))
                .andExpect(jsonPath("$.status").value("ACCEPTED"));

        verify(service).update(eq(1L), any(RiderAssignment.class));
    }


    @Test
    void shouldRejectDuplicateDeliveryAssignment() throws Exception {
        when(service.create(any(RiderAssignment.class)))
                .thenThrow(new BusinessRuleException(
                        "El delivery 1001 ya tiene una asignación registrada"));

        mockMvc.perform(post("/api/assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sample())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(
                        "El delivery 1001 ya tiene una asignación registrada"));
    }


    @Test
    void shouldDeleteAssignment() throws Exception {
        doNothing().when(service).delete(1L);

        mockMvc.perform(delete("/api/assignments/1"))
                .andExpect(status().isNoContent());

        verify(service).delete(1L);
    }

    private RiderAssignment sample() {
        return new RiderAssignment(
                1L, 1001L, 501L, AssignmentStatus.ASSIGNED, LocalDateTime.of(2026, 11, 18, 10, 0));
    }
}
