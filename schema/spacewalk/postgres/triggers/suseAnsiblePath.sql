CREATE OR REPLACE function suse_ansible_path_mod_trig_fun() RETURNS TRIGGER AS
$$
BEGIN
        new.modified := current_timestamp;
        RETURN new;
END;
$$ language plpgsql;

CREATE TRIGGER
suse_ansible_path_mod_trig
BEFORE INSERT OR UPDATE ON suseAnsiblePath
FOR EACH ROW
EXECUTE PROCEDURE suse_ansible_path_mod_trig_fun();

