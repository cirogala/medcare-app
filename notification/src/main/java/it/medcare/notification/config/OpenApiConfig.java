package it.medcare.notification.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;

@OpenAPIDefinition(
    info = @Info(
        title = "MedCare Notification API",
        version = "v1",
        description = "Gestione notifiche e invio email MedCare.",
        contact = @Contact(name = "MedCare API", email = "supporto@medcare.it"),
        license = @License(name = "Proprietary")
    )
)
public class OpenApiConfig {}
