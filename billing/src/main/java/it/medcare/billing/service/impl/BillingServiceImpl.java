package it.medcare.billing.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import it.medcare.billing.client.PrenotationClient;
import it.medcare.billing.client.ProfilingClient;
import it.medcare.billing.dto.OverviewDTO;
import it.medcare.billing.dto.PaymentDTO;
import it.medcare.billing.dto.PaymentSummaryDTO;
import it.medcare.billing.dto.PaymentUpdateRequest;
import it.medcare.billing.dto.PrenotationDTO;
import it.medcare.billing.dto.RevenueByDoctorDTO;
import it.medcare.billing.dto.RevenueBySpecializationDTO;
import it.medcare.billing.dto.RevenuePointDTO;
import it.medcare.billing.dto.UserProfileDTO;
import it.medcare.billing.dto.VisitDTO;
import it.medcare.billing.entity.Payment;
import it.medcare.billing.enums.PaymentStatus;
import it.medcare.billing.repository.PaymentRepository;
import it.medcare.billing.service.BillingService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BillingServiceImpl implements BillingService {

    private final PaymentRepository paymentRepository;
    private final PrenotationClient prenotationClient;
    private final ProfilingClient profilingClient;

    @Override
    public OverviewDTO getOverview(LocalDate from, LocalDate to, String authHeader, String accept, String keyLogic, String transId) {
    	
        List<PrenotationDTO> prenotations = fetchPrenotations(from, to, authHeader, accept, keyLogic, transId);
        Map<Long, VisitDTO> visitMap = fetchVisitMap(authHeader, accept, keyLogic, transId);
        Map<Long, Payment> paymentMap = fetchPaymentMap(prenotations);

        BigDecimal totalExpected = BigDecimal.ZERO;
        BigDecimal totalPaid = BigDecimal.ZERO;
        int paidCount = 0;
        int refundedCount = 0;
        int pendingCount = 0;

        for (PrenotationDTO prenotation : prenotations) {
        	
            BigDecimal price = getPriceForPrenotation(prenotation, visitMap);
            totalExpected = totalExpected.add(price);

            Payment payment = paymentMap.get(prenotation.getPrenotationId());
            
            if (payment == null || payment.getStatus() == null) {
            	
                pendingCount++;
                
                continue;
            }

            if (payment.getStatus() == PaymentStatus.PAID) {
            	
                paidCount++;
                
                totalPaid = totalPaid.add(defaultAmount(payment.getAmount(), price));
                
            } else if (payment.getStatus() == PaymentStatus.REFUNDED) {
            	
                refundedCount++;
                
            } else {
            	
                pendingCount++;
            }
        }

        OverviewDTO overview = new OverviewDTO();
        overview.setFrom(from);
        overview.setTo(to);
        overview.setTotalVisits(prenotations.size());
        overview.setPaidCount(paidCount);
        overview.setRefundedCount(refundedCount);
        overview.setPendingCount(pendingCount);
        overview.setTotalExpectedAmount(totalExpected);
        overview.setTotalPaidAmount(totalPaid);
        overview.setAverageTicket(calculateAverage(totalExpected, prenotations.size()));
        
        return overview;
    }

    @Override
    public List<RevenuePointDTO> getRevenue(LocalDate from, LocalDate to, String group, String authHeader, String accept, String keyLogic, String transId) {
    	
        List<PrenotationDTO> prenotations = fetchPrenotations(from, to, authHeader, accept, keyLogic, transId);
        Map<Long, VisitDTO> visitMap = fetchVisitMap(authHeader, accept, keyLogic, transId);
        Map<Long, Payment> paymentMap = fetchPaymentMap(prenotations);

        Map<String, BigDecimal> grouped = new TreeMap<>();
        String normalizedGroup = StringUtils.hasText(group) ? group.toLowerCase(Locale.ROOT) : "day";

        for (PrenotationDTO prenotation : prenotations) {
        	
            Payment payment = paymentMap.get(prenotation.getPrenotationId());
            
            if (payment == null || payment.getStatus() != PaymentStatus.PAID) {
            	
                continue;
            }

            String label = buildGroupLabel(prenotation.getDate(), normalizedGroup);
            BigDecimal price = defaultAmount(payment.getAmount(), getPriceForPrenotation(prenotation, visitMap));
            grouped.merge(label, price, BigDecimal::add);
        }

        return grouped.entrySet().stream()
            .map(entry -> {
                RevenuePointDTO dto = new RevenuePointDTO();
                dto.setLabel(entry.getKey());
                dto.setTotalAmount(entry.getValue());
                return dto;
            })
            .toList();
    }

    @Override
    public List<RevenueBySpecializationDTO> getRevenueBySpecialization(LocalDate from, LocalDate to, String authHeader, String accept, String keyLogic, String transId) {
    	
        List<PrenotationDTO> prenotations = fetchPrenotations(from, to, authHeader, accept, keyLogic, transId);
        Map<Long, VisitDTO> visitMap = fetchVisitMap(authHeader, accept, keyLogic, transId);
        Map<Long, Payment> paymentMap = fetchPaymentMap(prenotations);

        Map<String, BigDecimal> totals = new HashMap<>();
        
        for (PrenotationDTO prenotation : prenotations) {
        	
            Payment payment = paymentMap.get(prenotation.getPrenotationId());
            
            if (payment == null || payment.getStatus() != PaymentStatus.PAID) {
            	
                continue;
            }

            VisitDTO visit = visitMap.get(prenotation.getVisitTypeId());
            String specialization = visit != null && StringUtils.hasText(visit.getSpecialization()) ? visit.getSpecialization() : "Non specificata";
            BigDecimal price = defaultAmount(payment.getAmount(), getPriceForPrenotation(prenotation, visitMap));
            
            totals.merge(specialization, price, BigDecimal::add);
        }

        return totals.entrySet().stream()
            .map(entry -> {
                RevenueBySpecializationDTO dto = new RevenueBySpecializationDTO();
                dto.setSpecialization(entry.getKey());
                dto.setTotalAmount(entry.getValue());
                return dto;
            })
            .sorted(Comparator.comparing(RevenueBySpecializationDTO::getTotalAmount).reversed())
            .toList();
    }

    @Override
    public List<RevenueByDoctorDTO> getRevenueByDoctor(LocalDate from, LocalDate to, String authHeader, String accept, String keyLogic, String transId) {
    	
        List<PrenotationDTO> prenotations = fetchPrenotations(from, to, authHeader, accept, keyLogic, transId);
        Map<Long, VisitDTO> visitMap = fetchVisitMap(authHeader, accept, keyLogic, transId);
        Map<Long, Payment> paymentMap = fetchPaymentMap(prenotations);
        Map<Long, String> doctorNames = profilingClient.fetchDoctors().stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toMap(UserProfileDTO::getUserId,
                doctor -> (doctor.getNome() + " " + doctor.getCognome()).trim(),
                (a, b) -> a));

        Map<Long, BigDecimal> totals = new HashMap<>();
        
        for (PrenotationDTO prenotation : prenotations) {
        	
            Payment payment = paymentMap.get(prenotation.getPrenotationId());
            
            if (payment == null || payment.getStatus() != PaymentStatus.PAID) {
            	
                continue;
            }
            
            BigDecimal price = defaultAmount(payment.getAmount(), getPriceForPrenotation(prenotation, visitMap));
            totals.merge(prenotation.getDoctorId(), price, BigDecimal::add);
        }

        return totals.entrySet().stream()
            .map(entry -> {
                RevenueByDoctorDTO dto = new RevenueByDoctorDTO();
                dto.setDoctorId(entry.getKey());
                dto.setDoctorName(doctorNames.get(entry.getKey()));
                dto.setTotalAmount(entry.getValue());
                return dto;
            })
            .sorted(Comparator.comparing(RevenueByDoctorDTO::getTotalAmount).reversed())
            .toList();
    }

    @Override
    public PaymentSummaryDTO getPaymentSummary(LocalDate from, LocalDate to, String authHeader, String accept, String keyLogic, String transId) {
    	
        List<PrenotationDTO> prenotations = fetchPrenotations(from, to, authHeader, accept, keyLogic, transId);
        Map<Long, VisitDTO> visitMap = fetchVisitMap(authHeader, accept, keyLogic, transId);
        Map<Long, Payment> paymentMap = fetchPaymentMap(prenotations);

        int paidCount = 0;
        int refundedCount = 0;
        int pendingCount = 0;
        BigDecimal totalPaid = BigDecimal.ZERO;
        BigDecimal totalPending = BigDecimal.ZERO;

        for (PrenotationDTO prenotation : prenotations) {
        	
            BigDecimal price = getPriceForPrenotation(prenotation, visitMap);
            Payment payment = paymentMap.get(prenotation.getPrenotationId());
            
            if (payment == null || payment.getStatus() == null) {
            	
                pendingCount++;
                totalPending = totalPending.add(price);
                continue;
            }

            if (payment.getStatus() == PaymentStatus.PAID) {
            	
                paidCount++;
                totalPaid = totalPaid.add(defaultAmount(payment.getAmount(), price));
                
            } else if (payment.getStatus() == PaymentStatus.REFUNDED) {
            	
                refundedCount++;
                
            } else {
            	
                pendingCount++;
                totalPending = totalPending.add(price);
            }
        }

        PaymentSummaryDTO dto = new PaymentSummaryDTO();
        dto.setPaidCount(paidCount);
        dto.setPendingCount(pendingCount);
        dto.setRefundedCount(refundedCount);
        dto.setTotalPaidAmount(totalPaid);
        dto.setTotalPendingAmount(totalPending);
        
        return dto;
    }

    @Override
    public List<PaymentDTO> getPayments(LocalDate from, LocalDate to, String status, String authHeader, String accept, String keyLogic, String transId) {
    	
        List<PrenotationDTO> prenotations = fetchPrenotations(from, to, authHeader, accept, keyLogic, transId);
        Map<Long, VisitDTO> visitMap = fetchVisitMap(authHeader, accept, keyLogic, transId);
        Map<Long, Payment> paymentMap = fetchPaymentMap(prenotations);

        PaymentStatus statusFilter = parseStatus(status);

        return prenotations.stream()
            .map(prenotation -> mapPayment(prenotation, visitMap, paymentMap.get(prenotation.getPrenotationId())))
            .filter(dto -> statusFilter == null || dto.getStatus() == statusFilter)
            .toList();
    }

    @Override
    public PaymentDTO updatePaymentStatus(Long prenotationId, PaymentUpdateRequest request, String authHeader, String accept, String keyLogic, String transId) {
    	
        if (request == null || request.getStatus() == null) {
        	
            throw new IllegalArgumentException("Status pagamento obbligatorio");
        }

        PrenotationDTO prenotation = prenotationClient.fetchPrenotation(prenotationId, authHeader, accept, keyLogic, transId);
        
        if (prenotation == null) {
        	
            throw new IllegalArgumentException("Prenotazione non trovata");
        }

        Map<Long, VisitDTO> visitMap = fetchVisitMap(authHeader, accept, keyLogic, transId);
        BigDecimal price = getPriceForPrenotation(prenotation, visitMap);

        Payment payment = paymentRepository.findByPrenotationId(prenotationId).orElseGet(() -> {
            Payment created = new Payment();
            created.setPrenotationId(prenotationId);
            created.setCreatedAt(LocalDateTime.now());
            return created;
        });

        payment.setStatus(request.getStatus());
        payment.setAmount(price);
        payment.setUpdatedAt(LocalDateTime.now());
        
        if (request.getStatus() == PaymentStatus.PAID) {
        	
            payment.setPaidAt(LocalDateTime.now());
            
        } else {
        	
            payment.setPaidAt(null);
        }

        Payment saved = paymentRepository.save(payment);
        
        return mapPayment(prenotation, visitMap, saved);
    }

    private List<PrenotationDTO> fetchPrenotations(LocalDate from, LocalDate to, String authHeader, String accept, String keyLogic, String transId) {
    	
        return prenotationClient.fetchPrenotations(from, to, authHeader, accept, keyLogic, transId);
    }

    private Map<Long, VisitDTO> fetchVisitMap(String authHeader, String accept, String keyLogic, String transId) {
    	
        return prenotationClient.fetchVisits(authHeader, accept, keyLogic, transId).stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toMap(VisitDTO::getVisitId, visit -> visit, (a, b) -> a));
    }

    private Map<Long, Payment> fetchPaymentMap(List<PrenotationDTO> prenotations) {
    	
        List<Long> ids = prenotations.stream()
            .map(PrenotationDTO::getPrenotationId)
            .filter(Objects::nonNull)
            .toList();

        if (ids.isEmpty()) {
        	
            return Map.of();
        }

        return paymentRepository.findByPrenotationIdIn(ids).stream().collect(Collectors.toMap(Payment::getPrenotationId, payment -> payment, (a, b) -> a));
    }

    private BigDecimal getPriceForPrenotation(PrenotationDTO prenotation, Map<Long, VisitDTO> visitMap) {
    	
        if (prenotation == null || prenotation.getVisitTypeId() == null) {
        	
            return BigDecimal.ZERO;
        }
        
        VisitDTO visit = visitMap.get(prenotation.getVisitTypeId());
        
        return visit != null && visit.getPrice() != null ? visit.getPrice() : BigDecimal.ZERO;
    }

    private BigDecimal calculateAverage(BigDecimal total, int count) {
    	
        if (count == 0) {
        	
            return BigDecimal.ZERO;
        }
        
        return total.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
    }

    private String buildGroupLabel(LocalDate date, String group) {
    	
        if (date == null) {
        	
            return "N/A";
        }
        
        return switch (group) {
        
            case "week" -> {
                WeekFields wf = WeekFields.ISO;
                int week = date.get(wf.weekOfWeekBasedYear());
                int year = date.get(wf.weekBasedYear());
                yield year + "-W" + String.format("%02d", week);
            }
            
            case "month" -> date.getYear() + "-" + String.format("%02d", date.getMonthValue());
            
            default -> date.toString();
        };
    }

    private PaymentDTO mapPayment(PrenotationDTO prenotation, Map<Long, VisitDTO> visitMap, Payment payment) {
    	
        PaymentDTO dto = new PaymentDTO();
        
        if (payment != null) {
        	
            dto.setPaymentId(payment.getPaymentId());
            dto.setPrenotationId(payment.getPrenotationId());
            dto.setAmount(payment.getAmount());
            dto.setStatus(payment.getStatus());
            dto.setPaidAt(payment.getPaidAt());
            dto.setCreatedAt(payment.getCreatedAt());
            dto.setUpdatedAt(payment.getUpdatedAt());
        } else {
            dto.setPrenotationId(prenotation.getPrenotationId());
            dto.setAmount(getPriceForPrenotation(prenotation, visitMap));
            dto.setStatus(PaymentStatus.PENDING);
        }
        
        return dto;
    }

    private BigDecimal defaultAmount(BigDecimal candidate, BigDecimal fallback) {
    	
        if (candidate == null || candidate.compareTo(BigDecimal.ZERO) == 0) {
        	
            return fallback != null ? fallback : BigDecimal.ZERO;
        }
        
        return candidate;
    }

    private PaymentStatus parseStatus(String status) {
    	
        if (!StringUtils.hasText(status)) {
        	
            return null;
        }
        
        try {
        	
            return PaymentStatus.valueOf(status.toUpperCase(Locale.ROOT));
            
        } catch (IllegalArgumentException ex) {
        	
            return null;
        }
    }
}
