-- oracle equivalent source sha1 3e65bbed0d18f7def20d349d0b72b07a71c3051b

alter table suseVHMConfig add constraint suse_vhmc_id_para_uq unique (virtual_host_manager_id, parameter) DEFERRABLE;
