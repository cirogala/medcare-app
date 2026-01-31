# MedCare - Documentazione Funzionale (Microservizi)

## Scopo
Il sistema MedCare gestisce registrazione utenti, prenotazioni visite, referti/anamnesi, notifiche email e dashboard economica. L’obiettivo è permettere a pazienti, medici e utenti interni di lavorare sulla piattaforma in modo semplice.

---

## Ruoli

- **Admin/Utente interno**: gestisce utenti, medici, prenotazioni, pagamenti.
- **Medico**: consulta agenda, prenotazioni, carica referti, compila anamnesi.
- **Paziente**: prenota visite, consulta le proprie prenotazioni e referti.

---

## Microservizi e funzionalità

### profiling
- Autenticazione login (username/password).
- Recupero profilo utente (nome, cognome, ruolo).
- Gestione utenti interni/esterni.
- Elenco medici con specializzazione.
- Reset password con invio email.

### prenotation
- Catalogo tipologie di visita (durata, prezzo, specializzazione).
- Prenotazione visita con scelta medico/slot.
- Storico prenotazioni utente/medico.
- Stato prenotazione: **Attiva**, **Completata**, **Annullata**.
- Slot prenotazioni e disponibilità per data/ora.

### doc-repo
- Upload referti (PDF o altri file).
- Upload anamnesi (JSON compilato dal medico).
- Download e consultazione documenti.

### notification
- Invio email per:
  - Creazione utente
  - Nuova prenotazione (medico + paziente)
  - Referto caricato
  - Reset password

### billing
- Dashboard economica (ricavi, ticket medio, visite).
- Pagamenti per prenotazione con stato:
  - **In attesa**
  - **Pagato**
  - **Rimborsato**
- Gestione manuale dei pagamenti da parte dell’admin.

---

## Flussi principali

### 1) Login
- Utente inserisce username/password.
- `profiling` valida e restituisce JWT.
- Frontend mostra la dashboard in base al ruolo.

### 2) Prenotazione visita (Paziente)
- Selezione tipologia visita e data/slot.
- Vengono mostrati solo i medici compatibili con la specializzazione.
- Creazione prenotazione in `prenotation`.
- Notifica inviata a medico e paziente.

### 3) Gestione visita (Medico)
- Visualizza prenotazioni.
- Carica referto su `doc-repo`.
- Compila anamnesi (questionario), caricato su `doc-repo`.
- Quando la data è trascorsa, la visita è conteggiata per KPI.

### 4) Pagamenti (Utente interno)
- L’admin visualizza prenotazioni e stati pagamento.
- Può impostare **Pagato** o **Rimborsato**.
- Dashboard economica aggiornata automaticamente.

---

## Dati demo
Nella cartella `data_setup` sono presenti script per creare utenti e prenotazioni demo, con credenziali valide per tutti i ruoli.

