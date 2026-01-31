# MedCare - Workspace

Questo workspace contiene il frontend `portal` e tutti i microservizi BE.

## Struttura

- `portal` (Angular)
- `profiling` (Spring Boot, SQL Server)
- `prenotation` (Spring Boot, SQL Server)
- `doc-repo` (Spring Boot, MongoDB)
- `notification` (Spring Boot, MongoDB + SMTP)
- `billing` (Spring Boot, SQL Server)
- `data_setup` (script di seed demo)

## Requisiti

- Java 17
- Maven o `./mvnw`
- Node.js LTS (consigliato 20.x)
- SQL Server su `127.0.0.1:1433`
- MongoDB su `127.0.0.1:27017`
- Docker (opzionale, per MailHog)

## Porte

- Frontend: `http://localhost:4200`
- Profiling: `http://localhost:8080`
- Prenotation: `http://localhost:8081`
- Doc-repo: `http://localhost:8082`
- Notification: `http://localhost:8083`
- Billing: `http://localhost:8084`
- SQL Server: `1433`
- MongoDB: `27017`
- MailHog UI (opzionale): `http://localhost:8025`

## Database SQL Server

Credenziali default:

- username: `sa`
- password: `MedCare@2025!`

DB richiesti:

```sql
CREATE DATABASE medcare_profiling;
CREATE DATABASE prenotation;
CREATE DATABASE billing;
```

Per Billing è disponibile lo script:
`billing/db/create_billing.sql`

> Le tabelle vengono create automaticamente da Hibernate (`ddl-auto=update`).

## MongoDB

DB usati:

- `doc_repo`
- `notification`

Nessuna creazione manuale richiesta.

## Mail (Notification)

MailHog per test locale:

```bash
docker run -p 1025:1025 -p 8025:8025 mailhog/mailhog
```

L’app `notification` invia verso `localhost:1025`.

## Avvio Backend

Apri un terminale per ciascun microservizio:

```bash
# Profiling
cd profiling
./mvnw spring-boot:run

# Prenotation
cd ../prenotation
./mvnw spring-boot:run

# Doc-repo
cd ../doc-repo
./mvnw spring-boot:run

# Notification
cd ../notification
./mvnw spring-boot:run

# Billing
cd ../billing
./mvnw spring-boot:run
```

## Avvio Frontend

Da `portal/`:

```bash
npm install
npm run start -- --proxy-config proxy.conf.json
```

oppure:

```bash
ng serve --proxy-config proxy.conf.json
```

## Swagger UI

- Profiling: `http://localhost:8080/swagger-ui.html`
- Prenotation: `http://localhost:8081/swagger-ui.html`
- Doc-repo: `http://localhost:8082/swagger-ui.html`
- Notification: `http://localhost:8083/swagger-ui.html`
- Billing: `http://localhost:8084/swagger-ui.html`

## Dati demo

Per inserire dati di test:

- `data_setup/README.md`

## Note

- Il frontend usa `portal/proxy.conf.json` per chiamare i microservizi.
- Se cambi porte/host aggiorna `application.properties` e il `proxy.conf.json`.
