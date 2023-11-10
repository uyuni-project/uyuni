ALTER TABLE susePaygSshData
  ALTER COLUMN password type VARCHAR(4096),
  ALTER COLUMN key_password type  VARCHAR(4096),
  ALTER COLUMN bastion_password type VARCHAR(4096),
  ALTER COLUMN bastion_key_password type VARCHAR(4096);
