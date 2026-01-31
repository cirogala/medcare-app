package it.medcare.billing.service;

import java.time.LocalDate;
import java.util.List;

import it.medcare.billing.dto.OverviewDTO;
import it.medcare.billing.dto.PaymentDTO;
import it.medcare.billing.dto.PaymentSummaryDTO;
import it.medcare.billing.dto.PaymentUpdateRequest;
import it.medcare.billing.dto.RevenueByDoctorDTO;
import it.medcare.billing.dto.RevenueBySpecializationDTO;
import it.medcare.billing.dto.RevenuePointDTO;

public interface BillingService {

    OverviewDTO getOverview(LocalDate from, LocalDate to, String authHeader, String accept, String keyLogic, String transId);

    List<RevenuePointDTO> getRevenue(LocalDate from, LocalDate to, String group, String authHeader, String accept, String keyLogic, String transId);

    List<RevenueBySpecializationDTO> getRevenueBySpecialization(LocalDate from, LocalDate to, String authHeader, String accept, String keyLogic, String transId);

    List<RevenueByDoctorDTO> getRevenueByDoctor(LocalDate from, LocalDate to, String authHeader, String accept, String keyLogic, String transId);

    PaymentSummaryDTO getPaymentSummary(LocalDate from, LocalDate to, String authHeader, String accept, String keyLogic, String transId);

    List<PaymentDTO> getPayments(LocalDate from, LocalDate to, String status, String authHeader, String accept, String keyLogic, String transId);

    PaymentDTO updatePaymentStatus(Long prenotationId, PaymentUpdateRequest request, String authHeader, String accept, String keyLogic, String transId);
}
