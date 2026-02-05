ALTER TABLE public.users DROP CONSTRAINT IF EXISTS fk_users__auth_user_id;
DROP INDEX IF EXISTS ux_users__auth_user_id;
ALTER TABLE public.users DROP COLUMN IF EXISTS auth_user_id;

ALTER TABLE public.users
    ADD CONSTRAINT fk_public_users_auth_users
        FOREIGN KEY (id)
            REFERENCES auth.users(id)
            ON DELETE CASCADE;

CREATE OR REPLACE FUNCTION public.handle_new_auth_user()
RETURNS trigger
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
BEGIN
INSERT INTO public.users (id, email)
VALUES (new.id, new.email)
    ON CONFLICT (id) DO NOTHING;
RETURN new;
END;
$$;

DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE PROCEDURE public.handle_new_auth_user();