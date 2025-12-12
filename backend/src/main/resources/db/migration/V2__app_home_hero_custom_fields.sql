-- Adds customizable hero labels and visibility toggles for the authenticated home.
ALTER TABLE site_home_login_section
    ADD COLUMN IF NOT EXISTS greeting_prefix VARCHAR(80) DEFAULT 'Ola',
    ADD COLUMN IF NOT EXISTS profile_bar_visible BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS label_current_company VARCHAR(120) DEFAULT 'Empresa atual',
    ADD COLUMN IF NOT EXISTS label_offers VARCHAR(120) DEFAULT 'Ofertas disponiveis';
