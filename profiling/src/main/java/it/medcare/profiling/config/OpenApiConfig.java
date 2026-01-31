package it.medcare.profiling.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;

@OpenAPIDefinition(
    info = @Info(
        title = "MedCare Profiling API",
        version = "v1",
        description = "Gestione utenti, autenticazione e profili MedCare.",
        contact = @Contact(name = "MedCare API", email = "supporto@medcare.it"),
        license = @License(name = "Proprietary")
    )
)
public class OpenApiConfig {}
