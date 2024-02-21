
CREATE OR REPLACE FUNCTION suse_srvcocoatt_rep_mod_trig_fun() RETURNS TRIGGER AS
$$
BEGIN
	new.modified = current_timestamp;
 	RETURN new;
END;
$$ language plpgsql;

DROP TRIGGER IF EXISTS suse_srvcocoatt_rep_mod_trig ON suseServerCoCoAttestationReport;

CREATE TRIGGER
suse_srvcocoatt_rep_mod_trig
BEFORE INSERT OR UPDATE ON suseServerCoCoAttestationReport
FOR EACH ROW
EXECUTE PROCEDURE suse_srvcocoatt_rep_mod_trig_fun();
