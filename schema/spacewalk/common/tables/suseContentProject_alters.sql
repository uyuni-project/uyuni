ALTER TABLE suseContentProject
  ADD CONSTRAINT suse_ct_project_fenvid_fk
  FOREIGN KEY (first_env_id)
  REFERENCES suseContentEnvironment(id)
  ON DELETE SET NULL;
