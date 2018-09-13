CREATE INDEX rhn_srv_net_iface_hw_addr_idx
    ON rhnServerNetInterface (hw_addr)
    TABLESPACE [[8m_tbs]];

