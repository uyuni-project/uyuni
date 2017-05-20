ALTER TABLE suseProducts ADD channel_family_id NUMBER
    CONSTRAINT suse_products_cfid_fk
    REFERENCES rhnChannelFamily (id)
    ON DELETE SET NULL;
