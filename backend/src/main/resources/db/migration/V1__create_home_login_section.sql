-- Creates the authenticated home section table used by HomeLogin content.
CREATE TABLE IF NOT EXISTS site_home_login_section (
    id SERIAL PRIMARY KEY,
    type VARCHAR(40) UNIQUE NOT NULL,
    display_order INTEGER NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    title VARCHAR(120) NOT NULL,
    subtitle VARCHAR(500),
    content VARCHAR(2000),
    primary_cta_label VARCHAR(80),
    primary_cta_url VARCHAR(300)
);
