package it.medcare.notification.controller;

import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.medcare.notification.common.rest.Headers;
import it.medcare.notification.common.rest.MediaType;
import it.medcare.notification.dto.NotificationResponse;
import it.medcare.notification.dto.PasswordResetRequest;
import it.medcare.notification.dto.PrenotationCreatedRequest;
import it.medcare.notification.dto.ReportUploadedRequest;
import it.medcare.notification.dto.UserCreatedRequest;
import it.medcare.notification.service.NotificationService;

@RestController
@RequestMapping("/notification")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping(path = "/users/created", produces = MediaType.APP_JSON)
    public NotificationResponse userCreated(
        @RequestHeader(name = Headers.ACCEPT, required = true) String accept,
        @RequestHeader(name = Headers.KEY_LOGIC, required = true) String keyLogic,
        @RequestHeader(name = Headers.TRANSACTION_ID, required = false) String transId,
        @RequestBody UserCreatedRequest request
    ) {
    	
        return notificationService.notifyUserCreated(request);
    }

    @PostMapping(path = "/prenotations/created", produces = MediaType.APP_JSON)
    public List<NotificationResponse> prenotationCreated(
        @RequestHeader(name = Headers.ACCEPT, required = true) String accept,
        @RequestHeader(name = Headers.KEY_LOGIC, required = true) String keyLogic,
        @RequestHeader(name = Headers.TRANSACTION_ID, required = false) String transId,
        @RequestBody PrenotationCreatedRequest request
    ) {
    	
        return notificationService.notifyPrenotationCreated(request);
    }

    @PostMapping(path = "/reports/uploaded", produces = MediaType.APP_JSON)
    public NotificationResponse reportUploaded(
        @RequestHeader(name = Headers.ACCEPT, required = true) String accept,
        @RequestHeader(name = Headers.KEY_LOGIC, required = true) String keyLogic,
        @RequestHeader(name = Headers.TRANSACTION_ID, required = false) String transId,
        @RequestBody ReportUploadedRequest request
    ) {
    	
        return notificationService.notifyReportUploaded(request);
    }

    @PostMapping(path = "/users/reset-password", produces = MediaType.APP_JSON)
    public NotificationResponse resetPassword(
        @RequestHeader(name = Headers.ACCEPT, required = true) String accept,
        @RequestHeader(name = Headers.KEY_LOGIC, required = true) String keyLogic,
        @RequestHeader(name = Headers.TRANSACTION_ID, required = false) String transId,
        @RequestBody PasswordResetRequest request
    ) {
    	
        return notificationService.notifyPasswordReset(request);
    }
}
