USE medcare_profiling;
GO

IF NOT EXISTS (SELECT 1 FROM mdt_role WHERE role_type = 'ADMIN')
BEGIN
  INSERT INTO mdt_role (role_type, role_description) VALUES ('ADMIN', 'Amministratore di sistema');
END
IF NOT EXISTS (SELECT 1 FROM mdt_role WHERE role_type = 'MEDICO')
BEGIN
  INSERT INTO mdt_role (role_type, role_description) VALUES ('MEDICO', 'Medico');
END
IF NOT EXISTS (SELECT 1 FROM mdt_role WHERE role_type = 'PAZIENTE')
BEGIN
  INSERT INTO mdt_role (role_type, role_description) VALUES ('PAZIENTE', 'Paziente');
END
GO

DECLARE @roleAdmin BIGINT = (SELECT role_id FROM mdt_role WHERE role_type = 'ADMIN');
DECLARE @roleMedico BIGINT = (SELECT role_id FROM mdt_role WHERE role_type = 'MEDICO');
DECLARE @rolePaziente BIGINT = (SELECT role_id FROM mdt_role WHERE role_type = 'PAZIENTE');

-- password hash (BCrypt) per: Medcare123!
DECLARE @pwd VARCHAR(200) = '$2y$10$lnvN/WBnX9mCbC1CTYXiSe47xXQe6Nx/3mWx39X2nw/u4kazal8/O';

SET IDENTITY_INSERT users ON;

IF NOT EXISTS (SELECT 1 FROM users WHERE user_id = 1)
BEGIN
  INSERT INTO users (user_id, nome, cognome, citta, indirizzo, codice_fiscale, telefono,
                     username, email, password, role_id, type_doctor, is_internal, is_med, flag_deleted)
  VALUES (1, 'Admin', 'MedCare', 'Napoli', 'Via Roma 1', 'ADM1234567890', '0810000000',
          'ADM1001', 'admin@medcare.local', @pwd, @roleAdmin, NULL, 1, 0, 0);
END

IF NOT EXISTS (SELECT 1 FROM users WHERE user_id = 2)
BEGIN
  INSERT INTO users (user_id, nome, cognome, citta, indirizzo, codice_fiscale, telefono,
                     username, email, password, role_id, type_doctor, is_internal, is_med, flag_deleted)
  VALUES (2, 'Sara', 'Verdi', 'Napoli', 'Via Torino 5', 'INT1234567890', '0810000001',
          'INT2001', 's.verdi@medcare.local', @pwd, @roleAdmin, NULL, 1, 0, 0);
END

IF NOT EXISTS (SELECT 1 FROM users WHERE user_id = 10)
BEGIN
  INSERT INTO users (user_id, nome, cognome, citta, indirizzo, codice_fiscale, telefono,
                     username, email, password, role_id, type_doctor, is_internal, is_med, flag_deleted)
  VALUES (10, 'Luca', 'Rossi', 'Napoli', 'Via Cardio 10', 'MED1234567890', '0810000010',
          'MED1001', 'l.rossi@medcare.local', @pwd, @roleMedico, 'CARD', 0, 1, 0);
END

IF NOT EXISTS (SELECT 1 FROM users WHERE user_id = 11)
BEGIN
  INSERT INTO users (user_id, nome, cognome, citta, indirizzo, codice_fiscale, telefono,
                     username, email, password, role_id, type_doctor, is_internal, is_med, flag_deleted)
  VALUES (11, 'Giulia', 'Bianchi', 'Napoli', 'Via Ocul 7', 'MED2234567890', '0810000011',
          'MED1002', 'g.bianchi@medcare.local', @pwd, @roleMedico, 'OCUL', 0, 1, 0);
END

IF NOT EXISTS (SELECT 1 FROM users WHERE user_id = 100)
BEGIN
  INSERT INTO users (user_id, nome, cognome, citta, indirizzo, codice_fiscale, telefono,
                     username, email, password, role_id, type_doctor, is_internal, is_med, flag_deleted)
  VALUES (100, 'Marco', 'Esposito', 'Napoli', 'Via Paziente 3', 'PAT1234567890', '0810000100',
          'PAT1001', 'marco.esposito@medcare.local', @pwd, @rolePaziente, NULL, 0, 0, 0);
END

IF NOT EXISTS (SELECT 1 FROM users WHERE user_id = 101)
BEGIN
  INSERT INTO users (user_id, nome, cognome, citta, indirizzo, codice_fiscale, telefono,
                     username, email, password, role_id, type_doctor, is_internal, is_med, flag_deleted)
  VALUES (101, 'Anna', 'Russo', 'Napoli', 'Via Paziente 8', 'PAT2234567890', '0810000101',
          'PAT1002', 'anna.russo@medcare.local', @pwd, @rolePaziente, NULL, 0, 0, 0);
END

SET IDENTITY_INSERT users OFF;
GO
