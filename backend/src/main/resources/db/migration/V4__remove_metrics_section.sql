-- Removes legacy METRICS sections from authenticated home since the feature was deprecated.
DELETE FROM site_home_login_section WHERE type = 'METRICS';
