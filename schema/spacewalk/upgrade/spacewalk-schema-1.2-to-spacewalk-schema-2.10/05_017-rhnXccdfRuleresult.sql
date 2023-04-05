
ALTER TABLE rhnXccdfRuleresult ADD id NUMERIC;

CREATE SEQUENCE rhn_xccdf_rresult_id_seq;

UPDATE rhnXccdfRuleresult SET id = nextval('rhn_xccdf_rresult_id_seq');

ALTER TABLE rhnXccdfRuleresult ALTER COLUMN id SET NOT NULL;

ALTER TABLE rhnXccdfRuleresult
    ADD CONSTRAINT rhn_xccdf_rresult_id_pk PRIMARY KEY (id);
