-- oracle equivalent source sha1 2abb220ec52fbe102ee7a3a8b9c8a68c6fab86a5

ALTER TABLE suseContentEnvironmentTarget ADD COLUMN IF NOT EXISTS status VARCHAR(32) NOT NULL DEFAULT 'NEW';
UPDATE suseContentEnvironmentTarget SET status = 'BUILT';

