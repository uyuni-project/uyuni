
ALTER TABLE suseContentEnvironmentTarget ADD COLUMN IF NOT EXISTS status VARCHAR(32) NOT NULL DEFAULT 'NEW';
UPDATE suseContentEnvironmentTarget SET status = 'BUILT';

