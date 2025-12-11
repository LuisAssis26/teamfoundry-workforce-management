-- Força a coluna de username de admin para texto, convertendo de bytea se necessário.
ALTER TABLE admin_account
    ALTER COLUMN username TYPE TEXT
    USING (
        CASE
            WHEN pg_typeof(username) = 'bytea'::regtype THEN convert_from(username, 'UTF8')
            ELSE username::text
        END
    );
