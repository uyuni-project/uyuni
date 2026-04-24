--
-- Performance indexes for OVAL-based CVE audit queries
--

CREATE INDEX IF NOT EXISTS rhn_server_cpe_idx ON rhnserver(cpe);
CREATE INDEX IF NOT EXISTS rhn_sp_nid_sid_idx ON rhnserverpackage(name_id, server_id);
