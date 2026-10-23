DROP INDEX IF EXISTS rhn_srv_reg_tok_ts_idx;

DELETE FROM rhnServerTokenRegs t1
USING rhnServerTokenRegs t2
WHERE t1.server_id = t2.server_id
  AND t1.token_id = t2.token_id
  AND t1.ctid > t2.ctid;

CREATE UNIQUE INDEX IF NOT EXISTS rhn_srv_reg_tok_ts_uq
    ON rhnServerTokenRegs (token_id, server_id);
