ALTER TABLE public.users
    ALTER COLUMN name DROP NOT NULL,
    ALTER COLUMN payment_frequency DROP NOT NULL;