IF DB_ID('billing') IS NULL
BEGIN
    CREATE DATABASE billing;
END
GO

USE billing;
GO

IF OBJECT_ID('dbo.payment', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.payment (
        payment_id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        prenotation_id BIGINT NOT NULL,
        amount DECIMAL(10,2) NOT NULL,
        status VARCHAR(20) NOT NULL,
        paid_at DATETIME2 NULL,
        created_at DATETIME2 NOT NULL,
        updated_at DATETIME2 NULL
    );

    CREATE UNIQUE INDEX UX_payment_prenotation
        ON dbo.payment (prenotation_id);
END
GO
