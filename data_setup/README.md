# Data setup (seed)

Questi script permettono di inserire dati demo nei DB per navigare il progetto subito dopo l’avvio.

## Credenziali di test

Password per tutti: `Medcare123!`

- Admin interno
  - Username: `ADM1001`
  - Ruolo: ADMIN
- Utente interno
  - Username: `INT2001`
  - Ruolo: ADMIN
- Medico (Cardiologia)
  - Username: `MED1001`
  - Ruolo: MEDICO
- Medico (Oculistica)
  - Username: `MED1002`
  - Ruolo: MEDICO
- Paziente
  - Username: `PAT1001`
  - Ruolo: PAZIENTE

## SQL Server

Esegui i file nell’ordine:

1) `profiling_seed.sql`
2) `prenotation_seed.sql`
3) `billing_seed.sql`

Esempio con sqlcmd (se configurato):

```bash
sqlcmd -S 127.0.0.1 -U sa -P MedCare@2025! -i data_setup/profiling_seed.sql
sqlcmd -S 127.0.0.1 -U sa -P MedCare@2025! -i data_setup/prenotation_seed.sql
sqlcmd -S 127.0.0.1 -U sa -P MedCare@2025! -i data_setup/billing_seed.sql
```

## MongoDB (opzionale)

```bash
mongosh < data_setup/mongo_seed.js
```

## Note

- Gli script sono idempotenti (non duplicano dati già presenti).
- Le prenotazioni demo sono collegate ai medici e al paziente creati in profiling.
