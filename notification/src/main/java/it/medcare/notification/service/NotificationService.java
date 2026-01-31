package it.medcare.notification.service;

import java.util.List;

import it.medcare.notification.dto.NotificationResponse;
import it.medcare.notification.dto.PasswordResetRequest;
import it.medcare.notification.dto.PrenotationCreatedRequest;
import it.medcare.notification.dto.ReportUploadedRequest;
import it.medcare.notification.dto.UserCreatedRequest;

public interface NotificationService {

    NotificationResponse notifyUserCreated(UserCreatedRequest request);

    List<NotificationResponse> notifyPrenotationCreated(PrenotationCreatedRequest request);

    NotificationResponse notifyReportUploaded(ReportUploadedRequest request);

    NotificationResponse notifyPasswordReset(PasswordResetRequest request);
}
