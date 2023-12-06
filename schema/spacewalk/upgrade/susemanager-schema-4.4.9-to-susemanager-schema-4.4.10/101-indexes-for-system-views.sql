CREATE INDEX IF NOT EXISTS rhn_errkw_keyword_idx ON rhnErrataKeyword USING btree (keyword);
CREATE INDEX IF NOT EXISTS rhn_package_nid_evrid_idx ON rhnPackage (name_id, evr_id);
CREATE INDEX IF NOT EXISTS rhn_server_package_nid_evrid_idx ON rhnServerPackage (name_id, evr_id);
CREATE INDEX IF NOT EXISTS rhn_sp_sien_idx ON rhnServerPackage USING btree (server_id, installtime, evr_id, name_id);
