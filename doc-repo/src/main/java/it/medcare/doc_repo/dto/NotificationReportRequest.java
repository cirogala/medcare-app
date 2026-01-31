package it.medcare.doc_repo.dto;

import lombok.Data;

@Data
public class NotificationReportRequest {
	
    private String reportId;
    private Long prenotationId;
    private String visitType;
    private String date;
    private String slotTime;

    private String patientEmail;
    private String patientName;
    private String doctorName;
}
