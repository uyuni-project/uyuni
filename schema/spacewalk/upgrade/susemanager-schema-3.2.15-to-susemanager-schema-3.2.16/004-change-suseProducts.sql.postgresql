-- oracle equivalent source sha1 5f5f67f470cbd485de8cfb181fcb42f165dcfa1f

alter table suseProducts add column if not exists release_stage VARCHAR(10) DEFAULT ('released') NOT NULL;
alter table suseProducts add column if not exists description VARCHAR(4000);

CREATE UNIQUE INDEX if not exists suseprod_pdid_uq
ON suseProducts (product_id);
