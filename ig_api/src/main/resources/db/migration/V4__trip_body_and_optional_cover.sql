ALTER TABLE trip ADD COLUMN body_json jsonb;
ALTER TABLE trip ALTER COLUMN cover_image_key DROP NOT NULL;
