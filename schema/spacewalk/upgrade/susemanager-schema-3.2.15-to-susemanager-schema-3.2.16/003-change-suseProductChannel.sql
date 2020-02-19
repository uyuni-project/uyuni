
DELETE FROM suseProductChannel WHERE channel_id IS NULL;

ALTER TABLE suseProductChannel ALTER COLUMN channel_id SET NOT NULL;

alter table suseProductChannel drop column if exists channel_label;
alter table suseProductChannel drop column if exists parent_channel_label;

alter table suseProductChannel add column if not exists
    mandatory  CHAR(1) DEFAULT ('N') NOT NULL;

alter table suseProductChannel drop CONSTRAINT if exists spc_mand_ck;
alter table suseProductChannel add CONSTRAINT spc_mand_ck CHECK (mandatory in ('Y', 'N'));

drop index if exists suse_prd_chan_label_uq;
drop index if exists suse_prd_chan_pcl_idx;
drop index if exists suse_prd_chan_chan_idx;
