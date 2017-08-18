-- oracle equivalent source sha1 37ce11f19633471ed28413c0b1a36c2c791497cf
drop index suse_vhmc_id_para_uq;
alter table suseVHMConfig add constraint suse_vhmc_id_para_uq unique (virtual_host_manager_id, parameter) DEFERRABLE;

