USE prenotation;
GO

-- VISIT TYPES
SET IDENTITY_INSERT visit ON;

IF NOT EXISTS (SELECT 1 FROM visit WHERE visit_id = 1)
  INSERT INTO visit (visit_id, code, description, duration_min, price, specialization, flag_deleted)
  VALUES (1, 'CARD', 'Visita cardiologica', 45, 90.00, 'CARD', 0);
IF NOT EXISTS (SELECT 1 FROM visit WHERE visit_id = 2)
  INSERT INTO visit (visit_id, code, description, duration_min, price, specialization, flag_deleted)
  VALUES (2, 'ORTO', 'Visita ortopedica', 45, 85.00, 'ORTO', 0);
IF NOT EXISTS (SELECT 1 FROM visit WHERE visit_id = 3)
  INSERT INTO visit (visit_id, code, description, duration_min, price, specialization, flag_deleted)
  VALUES (3, 'DERM', 'Visita dermatologica', 30, 70.00, 'DERM', 0);
IF NOT EXISTS (SELECT 1 FROM visit WHERE visit_id = 4)
  INSERT INTO visit (visit_id, code, description, duration_min, price, specialization, flag_deleted)
  VALUES (4, 'OCUL', 'Visita oculistica', 40, 80.00, 'OCUL', 0);
IF NOT EXISTS (SELECT 1 FROM visit WHERE visit_id = 5)
  INSERT INTO visit (visit_id, code, description, duration_min, price, specialization, flag_deleted)
  VALUES (5, 'GINE', 'Visita ginecologica', 45, 90.00, 'GINE', 0);
IF NOT EXISTS (SELECT 1 FROM visit WHERE visit_id = 6)
  INSERT INTO visit (visit_id, code, description, duration_min, price, specialization, flag_deleted)
  VALUES (6, 'ODON', 'Visita odontoiatrica', 30, 65.00, 'ODON', 0);
IF NOT EXISTS (SELECT 1 FROM visit WHERE visit_id = 7)
  INSERT INTO visit (visit_id, code, description, duration_min, price, specialization, flag_deleted)
  VALUES (7, 'RAD', 'Visita radiologica', 60, 110.00, 'RAD', 0);
IF NOT EXISTS (SELECT 1 FROM visit WHERE visit_id = 8)
  INSERT INTO visit (visit_id, code, description, duration_min, price, specialization, flag_deleted)
  VALUES (8, 'ECO', 'Ecografia', 30, 60.00, 'ECO', 0);

SET IDENTITY_INSERT visit OFF;
GO

-- VISIT SLOTS
SET IDENTITY_INSERT visit_slot ON;

IF NOT EXISTS (SELECT 1 FROM visit_slot WHERE slot_id = 1001)
  INSERT INTO visit_slot (slot_id, doctor_id, visit_id, visit_date, start_time, end_time, available)
  VALUES (1001, 10, 1, DATEADD(day, 1, CAST(GETDATE() AS date)), CAST('09:00:00' AS time), CAST('10:00:00' AS time), 0);

IF NOT EXISTS (SELECT 1 FROM visit_slot WHERE slot_id = 1002)
  INSERT INTO visit_slot (slot_id, doctor_id, visit_id, visit_date, start_time, end_time, available)
  VALUES (1002, 10, 1, DATEADD(day, 1, CAST(GETDATE() AS date)), CAST('10:00:00' AS time), CAST('11:00:00' AS time), 1);

IF NOT EXISTS (SELECT 1 FROM visit_slot WHERE slot_id = 1003)
  INSERT INTO visit_slot (slot_id, doctor_id, visit_id, visit_date, start_time, end_time, available)
  VALUES (1003, 11, 4, DATEADD(day, 1, CAST(GETDATE() AS date)), CAST('11:00:00' AS time), CAST('12:00:00' AS time), 1);

IF NOT EXISTS (SELECT 1 FROM visit_slot WHERE slot_id = 1005)
  INSERT INTO visit_slot (slot_id, doctor_id, visit_id, visit_date, start_time, end_time, available)
  VALUES (1005, 10, 1, DATEADD(day, -3, CAST(GETDATE() AS date)), CAST('11:00:00' AS time), CAST('12:00:00' AS time), 0);

SET IDENTITY_INSERT visit_slot OFF;
GO

-- PRENOTATIONS
SET IDENTITY_INSERT prenotation ON;

IF NOT EXISTS (SELECT 1 FROM prenotation WHERE prenotation_id = 2001)
  INSERT INTO prenotation (prenotation_id, user_id, doctor_id, visit_type_id, slot_id, date, slot_time, status, created_at, fl_deleted, deleted_at)
  VALUES (2001, 100, 10, 1, 1001, DATEADD(day, 1, CAST(GETDATE() AS date)), CAST('09:00:00' AS time), 'ACTIVE', GETDATE(), 0, NULL);

IF NOT EXISTS (SELECT 1 FROM prenotation WHERE prenotation_id = 2002)
  INSERT INTO prenotation (prenotation_id, user_id, doctor_id, visit_type_id, slot_id, date, slot_time, status, created_at, fl_deleted, deleted_at)
  VALUES (2002, 100, 10, 1, 1005, DATEADD(day, -3, CAST(GETDATE() AS date)), CAST('11:00:00' AS time), 'COMPLETED', GETDATE(), 0, NULL);

SET IDENTITY_INSERT prenotation OFF;
GO
