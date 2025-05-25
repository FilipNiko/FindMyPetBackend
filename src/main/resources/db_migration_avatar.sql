-- Dodavanje avatar_id kolone u users tabelu
ALTER TABLE users
ADD COLUMN avatar_id VARCHAR(20) NOT NULL DEFAULT 'INITIALS'; 