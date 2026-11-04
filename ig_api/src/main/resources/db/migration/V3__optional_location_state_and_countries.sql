ALTER TABLE location DROP CONSTRAINT location_country_check;
ALTER TABLE location ADD CONSTRAINT location_country_check CHECK (country BETWEEN 0 AND 6);
ALTER TABLE location ALTER COLUMN state DROP NOT NULL;
ALTER TABLE location ADD CONSTRAINT location_us_state_required CHECK (country <> 0 OR state IS NOT NULL);
