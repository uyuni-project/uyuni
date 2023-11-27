
ALTER TABLE suseVHMConfig DROP CONSTRAINT suse_vhmc_id_para_uq;

ALTER TABLE suseVHMConfig ADD CONSTRAINT suse_vhmc_id_para_uq UNIQUE (virtual_host_manager_id, parameter)
        DEFERRABLE INITIALLY DEFERRED;
