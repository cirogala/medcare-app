package it.medcare.billing.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import it.medcare.billing.entity.Payment;
import it.medcare.billing.enums.PaymentStatus;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByPrenotationId(Long prenotationId);

    List<Payment> findByPrenotationIdIn(Collection<Long> prenotationIds);

    List<Payment> findByStatus(PaymentStatus status);
}
