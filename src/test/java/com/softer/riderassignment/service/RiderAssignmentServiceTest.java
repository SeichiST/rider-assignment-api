
package com.softer.riderassignment.service;

import com.softer.riderassignment.exception.BusinessRuleException;
import com.softer.riderassignment.exception.ResourceNotFoundException;
import com.softer.riderassignment.model.AssignmentStatus;
import com.softer.riderassignment.model.RiderAssignment;
import com.softer.riderassignment.repository.RiderAssignmentRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RiderAssignmentServiceTest {

    @Mock
    private RiderAssignmentRepository repository;

    @InjectMocks
    private RiderAssignmentService service;

    private RiderAssignment assignment;

    @BeforeEach
    void setUp() {
        assignment = new RiderAssignment(
                1L, 1001L, 501L,
                AssignmentStatus.ASSIGNED,
                LocalDateTime.now()
        );
    }

    @Test
    void shouldReturnAllAssignments() {
        when(repository.findAll()).thenReturn(List.of(assignment));

        List<RiderAssignment> result = service.findAll();

        assertEquals(1, result.size());
        assertEquals(1001L, result.getFirst().getDeliveryId());
        verify(repository).findAll();
    }

    @Test
    void shouldReturnAssignmentWhenIdExists() {
        when(repository.findById(1L))
                .thenReturn(Optional.of(assignment));

        RiderAssignment result = service.findById(1L);

        assertEquals(501L, result.getRiderId());
    }

    @Test
    void shouldThrowExceptionWhenAssignmentDoesNotExist() {
        when(repository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.findById(99L));
    }

    @Test
    void shouldCreateAssignmentWithValidData() {
        when(repository.existsByDeliveryId(1001L))
                .thenReturn(false);

        when(repository.save(any(RiderAssignment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RiderAssignment result = service.create(assignment);

        assertEquals(1001L, result.getDeliveryId());
        assertEquals(501L, result.getRiderId());
        assertEquals(AssignmentStatus.ASSIGNED, result.getStatus());

        verify(repository).save(assignment);
    }

    @Test
    void shouldRejectDuplicateDeliveryAssignment() {
        when(repository.existsByDeliveryId(1001L))
                .thenReturn(true);

        assertThrows(BusinessRuleException.class,
                () -> service.create(assignment));

        verify(repository, never()).save(any());
    }

    @Test
    void shouldAssignDefaultStatusWhenStatusIsNull() {
        assignment.setStatus(null);

        when(repository.existsByDeliveryId(1001L))
                .thenReturn(false);

        when(repository.save(any(RiderAssignment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RiderAssignment result = service.create(assignment);

        assertEquals(AssignmentStatus.ASSIGNED, result.getStatus());
    }

    @Test
    void shouldUpdateExistingAssignment() {
        RiderAssignment request = new RiderAssignment(
                null, 1001L, 502L,
                AssignmentStatus.ACCEPTED, null
        );

        when(repository.findById(1L))
                .thenReturn(Optional.of(assignment));

        when(repository.save(any(RiderAssignment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RiderAssignment result = service.update(1L, request);

        assertEquals(502L, result.getRiderId());
        assertEquals(AssignmentStatus.ACCEPTED, result.getStatus());
    }

    @Test
    void shouldRejectUpdateWhenDeliveryAlreadyAssigned() {
        RiderAssignment request = new RiderAssignment(
                null, 2002L, 502L,
                AssignmentStatus.ACCEPTED, null
        );

        when(repository.findById(1L))
                .thenReturn(Optional.of(assignment));

        when(repository.existsByDeliveryId(2002L))
                .thenReturn(true);

        assertThrows(BusinessRuleException.class,
                () -> service.update(1L, request));

        verify(repository, never()).save(any());
    }

    @Test
    void shouldDeleteExistingAssignment() {
        when(repository.findById(1L))
                .thenReturn(Optional.of(assignment));

        service.delete(1L);

        verify(repository).delete(assignment);
    }
}
