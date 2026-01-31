package it.medcare.prenotation.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;

@OpenAPIDefinition(
    info = @Info(
        title = "MedCare Prenotation API",
        version = "v1",
        description = "Gestione visite, slot e prenotazioni MedCare.",
        contact = @Contact(name = "MedCare API", email = "supporto@medcare.it"),
        license = @License(name = "Proprietary")
    )
)
public class OpenApiConfig {}
