package it.medcare.billing.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.medcare.billing.common.rest.Headers;
import it.medcare.billing.common.rest.MediaType;
import it.medcare.billing.config.MedcareSecurityConfig;
import it.medcare.billing.dto.OverviewDTO;
import it.medcare.billing.dto.PaymentDTO;
import it.medcare.billing.dto.PaymentSummaryDTO;
import it.medcare.billing.dto.PaymentUpdateRequest;
import it.medcare.billing.dto.RevenueByDoctorDTO;
import it.medcare.billing.dto.RevenueBySpecializationDTO;
import it.medcare.billing.dto.RevenuePointDTO;
import it.medcare.billing.enums.RoleType;
import it.medcare.billing.service.BillingService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/billing")
@RequiredArgsConstructor
@MedcareSecurityConfig(allowedRoles = {RoleType.ADMIN})
public class BillingController {

    private final BillingService billingService;

    @GetMapping(value = "/overview", produces = MediaType.BILLING_V1)
    public ResponseEntity<OverviewDTO> getOverview(
            @RequestHeader(name = Headers.ACCEPT, required = true) String accept,
            @RequestHeader(name = Headers.KEY_LOGIC, required = true) String keyLogic,
            @RequestHeader(name = Headers.TRANSACTION_ID, required = false) String transId,
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        return ResponseEntity.ok(billingService.getOverview(from, to, authorization, accept, keyLogic, transId));
    }

    @GetMapping(value = "/revenue", produces = MediaType.BILLING_V1)
    public ResponseEntity<List<RevenuePointDTO>> getRevenue(
            @RequestHeader(name = Headers.ACCEPT, required = true) String accept,
            @RequestHeader(name = Headers.KEY_LOGIC, required = true) String keyLogic,
            @RequestHeader(name = Headers.TRANSACTION_ID, required = false) String transId,
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false, defaultValue = "day") String group) {

        return ResponseEntity.ok(billingService.getRevenue(from, to, group, authorization, accept, keyLogic, transId));
    }

    @GetMapping(value = "/revenue/by-specialization", produces = MediaType.BILLING_V1)
    public ResponseEntity<List<RevenueBySpecializationDTO>> getRevenueBySpecialization(
            @RequestHeader(name = Headers.ACCEPT, required = true) String accept,
            @RequestHeader(name = Headers.KEY_LOGIC, required = true) String keyLogic,
            @RequestHeader(name = Headers.TRANSACTION_ID, required = false) String transId,
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        return ResponseEntity.ok(billingService.getRevenueBySpecialization(from, to, authorization, accept, keyLogic, transId));
    }

    @GetMapping(value = "/revenue/by-doctor", produces = MediaType.BILLING_V1)
    public ResponseEntity<List<RevenueByDoctorDTO>> getRevenueByDoctor(
            @RequestHeader(name = Headers.ACCEPT, required = true) String accept,
            @RequestHeader(name = Headers.KEY_LOGIC, required = true) String keyLogic,
            @RequestHeader(name = Headers.TRANSACTION_ID, required = false) String transId,
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        return ResponseEntity.ok(billingService.getRevenueByDoctor(from, to, authorization, accept, keyLogic, transId));
    }

    @GetMapping(value = "/payments/summary", produces = MediaType.BILLING_V1)
    public ResponseEntity<PaymentSummaryDTO> getPaymentSummary(
            @RequestHeader(name = Headers.ACCEPT, required = true) String accept,
            @RequestHeader(name = Headers.KEY_LOGIC, required = true) String keyLogic,
            @RequestHeader(name = Headers.TRANSACTION_ID, required = false) String transId,
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        return ResponseEntity.ok(billingService.getPaymentSummary(from, to, authorization, accept, keyLogic, transId));
    }

    @GetMapping(value = "/payments", produces = MediaType.BILLING_V1)
    public ResponseEntity<List<PaymentDTO>> getPayments(
            @RequestHeader(name = Headers.ACCEPT, required = true) String accept,
            @RequestHeader(name = Headers.KEY_LOGIC, required = true) String keyLogic,
            @RequestHeader(name = Headers.TRANSACTION_ID, required = false) String transId,
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String status) {

        return ResponseEntity.ok(billingService.getPayments(from, to, status, authorization, accept, keyLogic, transId));
    }

    @PatchMapping(value = "/payments/{prenotationId}/status", produces = MediaType.BILLING_V1)
    public ResponseEntity<PaymentDTO> updatePaymentStatus(
            @RequestHeader(name = Headers.ACCEPT, required = true) String accept,
            @RequestHeader(name = Headers.KEY_LOGIC, required = true) String keyLogic,
            @RequestHeader(name = Headers.TRANSACTION_ID, required = false) String transId,
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @PathVariable Long prenotationId,
            @RequestBody PaymentUpdateRequest request) {

        return ResponseEntity.ok(billingService.updatePaymentStatus(prenotationId, request, authorization, accept, keyLogic, transId));
    }
}
