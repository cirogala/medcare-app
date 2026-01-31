USE billing;
GO

IF NOT EXISTS (SELECT 1 FROM payment WHERE prenotation_id = 2001)
BEGIN
  INSERT INTO payment (prenotation_id, amount, status, paid_at, created_at, updated_at)
  VALUES (2001, 90.00, 'PENDING', NULL, GETDATE(), GETDATE());
END

IF NOT EXISTS (SELECT 1 FROM payment WHERE prenotation_id = 2002)
BEGIN
  INSERT INTO payment (prenotation_id, amount, status, paid_at, created_at, updated_at)
  VALUES (2002, 90.00, 'PAID', DATEADD(day, -2, GETDATE()), GETDATE(), GETDATE());
END
GO
