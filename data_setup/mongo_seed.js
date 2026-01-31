// Esegui con: mongosh < data_setup/mongo_seed.js

// DOC-REPO
use doc_repo;

db.doc_report.insertOne({
  prenotationId: 2002,
  patientId: 100,
  doctorId: 10,
  visitTypeId: 1,
  fileId: "demo-file-2002",
  filename: "referto-demo-2002.pdf",
  contentType: "application/pdf",
  size: 123456,
  createdAt: new Date()
});

// NOTIFICATION
use notification;

db.notifications.insertOne({
  eventType: "PRENOTATION_CREATED",
  recipientEmail: "marco.esposito@medcare.local",
  subject: "Prenotazione creata",
  body: "Demo notifica prenotazione.",
  status: "SENT",
  createdAt: new Date(),
  sentAt: new Date(),
  payload: {
    prenotationId: 2001
  }
});
