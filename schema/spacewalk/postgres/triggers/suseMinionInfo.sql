CREATE OR REPLACE function suse_minion_info_mod_trig_fun() RETURNS TRIGGER AS
$$
BEGIN
        new.modified := current_timestamp;
        RETURN new;
END;
$$ language plpgsql;

CREATE TRIGGER
suse_minion_info_mod_trig
BEFORE INSERT OR UPDATE ON suseMinionInfo
FOR EACH ROW
EXECUTE PROCEDURE suse_minion_info_mod_trig_fun();
