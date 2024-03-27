CREATE OR REPLACE FUNCTION suse_cocoatt_res_up_trig_fun() RETURNS TRIGGER AS
$$
BEGIN
	IF EXISTS(SELECT FROM suseCoCoAttestationResult WHERE status = 'PENDING' AND report_id = NEW.report_id) THEN
		return new;
	ELSIF EXISTS(SELECT FROM suseCoCoAttestationResult WHERE status = 'FAILED' AND report_id = NEW.report_id) THEN
		UPDATE suseServerCoCoAttestationReport
		SET status = 'FAILED'
		WHERE id = NEW.report_id;
	ELSE
		UPDATE suseServerCoCoAttestationReport
		SET status = 'SUCCEEDED'
		WHERE id = NEW.report_id;
	END IF;
	return new;
END;
$$ language plpgsql;

DROP TRIGGER IF EXISTS suse_cocoatt_res_up_trig ON suseCoCoAttestationResult;

CREATE TRIGGER
suse_cocoatt_res_up_trig
AFTER UPDATE ON suseCoCoAttestationResult
FOR EACH ROW
WHEN (OLD.status IS DISTINCT FROM NEW.status)
EXECUTE PROCEDURE suse_cocoatt_res_up_trig_fun();

CREATE OR REPLACE FUNCTION suse_cocoatt_notify_pending_trig_fun() RETURNS TRIGGER AS
$$
BEGIN
    NOTIFY pendingAttestationResult;
    RETURN NEW;
END;
$$ language plpgsql;

DROP TRIGGER IF EXISTS suse_cocoatt_notify_pending_trig ON suseCoCoAttestationResult;

CREATE TRIGGER
suse_cocoatt_notify_pending_trig
AFTER INSERT OR UPDATE OF status ON suseCoCoAttestationResult
FOR EACH ROW
WHEN (NEW.status = 'PENDING')
EXECUTE PROCEDURE suse_cocoatt_notify_pending_trig_fun();
