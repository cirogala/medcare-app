# MedCare - Documentazione Tecnica (Microservizi)

## Panoramica
Microservizi Spring Boot (Java 17) con autenticazione JWT e header custom MedCare.

### Stack comune
- Spring Boot 3.5.x
- Maven
- DB: SQL Server (profiling, prenotation, billing) + MongoDB (doc-repo, notification)
- Swagger UI attivo su tutti i microservizi
- Header richiesti:
  - `X-Medcare-Accept`
  - `X-Medcare-KeyLogic`
  - `X-Medcare-TransactionID` (opzionale)
  - `Authorization: Bearer <JWT>` (quando richiesto)

### Porte
- profiling: 8080
- prenotation: 8081
- doc-repo: 8082
- notification: 8083
- billing: 8084

---

## profiling
**Responsabilità**: gestione utenti, login, ruoli, profilo.

**DB**: SQL Server `medcare_profiling`.

**Principali entity**:
- `User`
- `MdtRole`

**Endpoint principali**:
- `POST /profiling/login`
- `POST /profiling/retrieveMyInfo`
- `POST /profiling/doctors?isMed=true`
- `GET /profiling/users`
- `GET /profiling/users/internal`
- `PUT /profiling/users/internal/{userId}`
- `PUT /profiling/users/external/{userId}`
- `POST /profiling/users/reset-password`

**Swagger**: `http://localhost:8080/swagger-ui.html`

---

## prenotation
**Responsabilità**: visite, prenotazioni, slot, stato visita.

**DB**: SQL Server `prenotation`.

**Entity**:
- `Visit`
- `VisitSlot`
- `Prenotation`

**Endpoint principali**:
- `GET /visits/all`
- `GET /visits/available`
- `GET /visits/available-doctors`
- `POST /visits/prenotations`
- `GET /visits/my`
- `GET /visits/doctor/my`
- `POST /visits/{id}/complete`
- `DELETE /visits/delete/{id}`
- `GET /visits/admin/prenotations?from=YYYY-MM-DD&to=YYYY-MM-DD`
- `GET /visits/admin/prenotations/{id}`

**Swagger**: `http://localhost:8081/swagger-ui.html`

---

## doc-repo
**Responsabilità**: upload/download referti e anamnesi (file). Metadata su Mongo.

**DB**: MongoDB `doc_repo` (collection `doc_report`).

**Endpoint principali**:
- `POST /doc-repo/upload`
- `GET /doc-repo/download/{fileId}`
- `GET /doc-repo/search?doctorId=...&patientId=...&prenotationId=...`

**Swagger**: `http://localhost:8082/swagger-ui.html`

---

## notification
**Responsabilità**: invio email e log eventi.

**DB**: MongoDB `notification` (collection `notifications`).

**Endpoint principali**:
- `POST /notification/users/created`
- `POST /notification/prenotations/created`
- `POST /notification/reports/uploaded`
- `POST /notification/users/reset-password`

**SMTP**: default MailHog `localhost:1025`.

**Swagger**: `http://localhost:8083/swagger-ui.html`

---

## billing
**Responsabilità**: stato pagamenti e KPI economici (ricavi/insoluti).

**DB**: SQL Server `billing`.

**Entity**:
- `Payment` (status: `PENDING|PAID|REFUNDED`)

**Endpoint principali**:
- `GET /billing/overview?from=...&to=...`
- `GET /billing/revenue?from=...&to=...&group=day|week|month`
- `GET /billing/revenue/by-specialization?from=...&to=...`
- `GET /billing/revenue/by-doctor?from=...&to=...`
- `GET /billing/payments/summary?from=...&to=...`
- `GET /billing/payments?from=...&to=...&status=PAID|PENDING|REFUNDED`
- `PATCH /billing/payments/{prenotationId}/status`

**Swagger**: `http://localhost:8084/swagger-ui.html`

---

## Integrazioni tra servizi
- `prenotation` chiama `profiling` per dati medici/pazienti.
- `prenotation` notifica `notification` alla creazione prenotazione.
- `doc-repo` notifica `notification` su upload referto.
- `billing` legge da `prenotation` (prenotazioni) e `profiling` (medici).

---

## Configurazioni principali
- `jwt.secret` e `jwt.expiration` in tutti i servizi che usano JWT.
- `profiling.doctors-url`, `profiling.users-url` in `prenotation`.
- `prenotation.base-url` in `billing`.
- `notification.base-url` in `profiling` e `prenotation`.

