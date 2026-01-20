ALTER TABLE credit_card_invoices DROP COLUMN reference_month;
ALTER TABLE credit_card_invoices ADD COLUMN reference_month INTEGER NOT NULL;