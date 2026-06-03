
-- coco database modifications

-- CONFIGURATION TABLE

-- configuration table: add in_data column
ALTER TABLE suseServerCocoAttestationConfig ADD COLUMN IF NOT EXISTS
    in_data jsonb NOT NULL default '{}';

-- RESULT TABLE

-- result table: add new states
ALTER TABLE suseCoCoAttestationResult
DROP CONSTRAINT suse_cocoatt_res_st_ck,
ADD  CONSTRAINT suse_cocoatt_res_st_ck CHECK(status IN ('PENDING', 'SUCCEEDED', 'FAILED', 'REQUESTED', 'QUEUED', 'SUBMITTED'));


-- result table: add env_type column
ALTER TABLE suseCoCoAttestationResult ADD COLUMN IF NOT EXISTS
    env_type NUMERIC NOT NULL default 0;

-- result table: update env_type column with corresponding report values
UPDATE suseCoCoAttestationResult res
SET env_type = rpt.env_type
FROM suseServerCocoAttestationReport rpt
WHERE rpt.id = res.report_id;

-- result table: change trigger to set report status depending on result ones
CREATE OR REPLACE FUNCTION suse_cocoatt_res_up_trig_fun() RETURNS TRIGGER AS
$$
BEGIN
	IF EXISTS(SELECT FROM suseCoCoAttestationResult WHERE status NOT IN ('FAILED', 'SUCCEEDED') AND report_id = NEW.report_id) THEN
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



-- result table: add notification trigger on REQUESTED status
CREATE OR REPLACE FUNCTION suse_cocoatt_notify_requested_trig_fun() RETURNS TRIGGER AS
$$
BEGIN
    NOTIFY pendingAttestationResult;
    RETURN NEW;
END;
$$ language plpgsql;


DROP TRIGGER IF EXISTS suse_cocoatt_notify_requested_trig ON suseCoCoAttestationResult;


CREATE TRIGGER
suse_cocoatt_notify_requested_trig
AFTER INSERT OR UPDATE OF status ON suseCoCoAttestationResult
FOR EACH ROW
WHEN (NEW.status = 'REQUESTED')
EXECUTE PROCEDURE suse_cocoatt_notify_requested_trig_fun();


-- result table: add in_data column
ALTER TABLE suseCoCoAttestationResult ADD COLUMN IF NOT EXISTS
    in_data jsonb NOT NULL default '{}';


-- REPORT TABLE

-- report table: add config_data column
ALTER TABLE suseServerCoCoAttestationReport ADD COLUMN IF NOT EXISTS
    config_data jsonb NOT NULL default '{}';

