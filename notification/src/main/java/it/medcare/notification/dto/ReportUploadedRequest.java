package it.medcare.notification.dto;

import lombok.Data;

@Data
public class ReportUploadedRequest {
	
    private String reportId;
    private Long prenotationId;
    private String visitType;
    private String date;
    private String slotTime;

    private String patientEmail;
    private String patientName;
    private String doctorName;
}
