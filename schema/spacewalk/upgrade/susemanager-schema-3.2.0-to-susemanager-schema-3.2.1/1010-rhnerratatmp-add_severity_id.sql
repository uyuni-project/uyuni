-- oracle equivalent source sha1 b98ff4d1f19446def7f4ebaa245092bd0af9a7f6
-- 
-- Red Hat trademarks are not licensed under GPLv2. No permission is
-- granted to use or replicate Red Hat trademarks that are incorporated
-- in this software or its documentation. 
--
--
-- ALTER TABLE rhnErrataTmp
--  ADD severity_id NUMERIC;

ALTER TABLE rhnErrataTmp
 ADD CONSTRAINT rhn_erratatmp_sevid_fk
 FOREIGN KEY (severity_id) REFERENCES rhnErrataSeverity(id);
