
ALTER TABLE public.users
    ADD COLUMN auth_user_id UUID;

ALTER TABLE public.users
    ADD CONSTRAINT fk_users__auth_user_id
        FOREIGN KEY (auth_user_id)
            REFERENCES auth.users(id)
            ON DELETE CASCADE;

CREATE UNIQUE INDEX IF NOT EXISTS ux_users__auth_user_id
    ON public.users(auth_user_id);