package com.lankastay.backend.service.reservation.observer;

import com.lankastay.backend.service.CustomerReservationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class ReservationEventPublisherTest {

    @AfterEach
    void clearTransactionSynchronization() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
        TransactionSynchronizationManager.setActualTransactionActive(false);
    }

    @Test
    void createdAndCancelledEventsReachMultipleObserversOnlyAfterCommit() {
        List<ReservationEvent> firstReceived = new ArrayList<>();
        List<ReservationEvent> secondReceived = new ArrayList<>();
        ReservationEventPublisher publisher = new ReservationEventPublisher(List.of(
                firstReceived::add,
                secondReceived::add));
        ReservationEvent created = event(ReservationEventType.CREATED);
        ReservationEvent cancelled = event(ReservationEventType.CANCELLED);

        beginSynchronization();
        publisher.publishAfterCommit(created);
        publisher.publishAfterCommit(cancelled);

        assertThat(firstReceived).isEmpty();
        assertThat(secondReceived).isEmpty();
        runAfterCommitCallbacks();
        assertThat(firstReceived).containsExactly(created, cancelled);
        assertThat(secondReceived).containsExactly(created, cancelled);
    }

    @Test
    void rolledBackTransactionDoesNotNotifyObservers() {
        List<ReservationEvent> received = new ArrayList<>();
        ReservationEventPublisher publisher = new ReservationEventPublisher(List.of(received::add));

        beginSynchronization();
        publisher.publishAfterCommit(event(ReservationEventType.CREATED));
        TransactionSynchronizationManager.getSynchronizations()
                .forEach(synchronization -> synchronization.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));

        assertThat(received).isEmpty();
    }

    @Test
    void failingNonCriticalObserverDoesNotStopOtherObservers() {
        List<ReservationEvent> received = new ArrayList<>();
        ReservationObserver failing = ignored -> {
            throw new IllegalStateException("simulated observer failure");
        };
        ReservationEventPublisher publisher = new ReservationEventPublisher(List.of(failing, received::add));
        ReservationEvent event = event(ReservationEventType.CREATED);

        beginSynchronization();
        publisher.publishAfterCommit(event);

        assertThatCode(this::runAfterCommitCallbacks).doesNotThrowAnyException();
        assertThat(received).containsExactly(event);
    }

    @Test
    void customerReservationServiceDependsOnPublisherAndNoConcreteObserver() {
        boolean hasPublisherDependency = false;
        boolean hasConcreteObserverDependency = false;
        for (var field : CustomerReservationService.class.getDeclaredFields()) {
            hasPublisherDependency |= field.getType() == ReservationEventPublisher.class;
            hasConcreteObserverDependency |= field.getType() == ReservationLifecycleLogObserver.class;
        }

        assertThat(hasPublisherDependency).isTrue();
        assertThat(hasConcreteObserverDependency).isFalse();
    }

    private ReservationEvent event(ReservationEventType type) {
        return new ReservationEvent(101L, UUID.randomUUID(), type);
    }

    private void beginSynchronization() {
        TransactionSynchronizationManager.setActualTransactionActive(true);
        TransactionSynchronizationManager.initSynchronization();
    }

    private void runAfterCommitCallbacks() {
        TransactionSynchronizationManager.getSynchronizations()
                .forEach(TransactionSynchronization::afterCommit);
    }
}
