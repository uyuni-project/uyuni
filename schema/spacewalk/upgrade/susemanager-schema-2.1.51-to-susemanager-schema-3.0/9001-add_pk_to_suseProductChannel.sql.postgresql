-- oracle equivalent source sha1 c4463ae934e5bc0421e3ad8f883581b90fc08537

CREATE SEQUENCE suse_product_channel_id_seq;

ALTER TABLE suseProductChannel ADD id NUMERIC;

UPDATE suseProductChannel SET id = sequence_nextval('suse_product_channel_id_seq');

ALTER TABLE suseProductChannel ADD CONSTRAINT suse_product_channel_id_pk PRIMARY KEY (id);

ALTER TABLE suseProductChannel ALTER id SET NOT NULL;
