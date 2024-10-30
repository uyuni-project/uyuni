--
-- Copyright (c) 2024 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--

CREATE TABLE IF NOT EXISTS suseChannelAttributes
(
    id                     BIGINT CONSTRAINT suse_chanatt_id_pk PRIMARY KEY
                                  GENERATED ALWAYS AS IDENTITY,
    product_id             NUMERIC NOT NULL
                                  CONSTRAINT suse_chanatt_pid_fk
                                  REFERENCES suseProducts (id)
                                  ON DELETE CASCADE,
    root_product_id        NUMERIC NOT NULL
                                  CONSTRAINT suse_chanatt_rpid_fk
                                  REFERENCES suseProducts (id)
                                  ON DELETE CASCADE,
    channel_label          VARCHAR(128) not null,
    parent_channel_label   VARCHAR(128),
    channel_name           VARCHAR(256) not null,
    mandatory              BOOLEAN DEFAULT FALSE NOT NULL,
    update_tag             VARCHAR(128),
    gpg_key_url            VARCHAR(256),
    gpg_key_id             VARCHAR(14),
    gpg_key_fp             VARCHAR(50),
    created                TIMESTAMPTZ
                           DEFAULT (current_timestamp) NOT NULL,
    modified               TIMESTAMPTZ
                           DEFAULT (current_timestamp) NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS suse_chanatt_pid_rpid_chl_uq
ON suseChannelAttributes (product_id, root_product_id, channel_label);

CREATE INDEX IF NOT EXISTS suse_chanatt_rpid_idx
ON suseChannelAttributes (root_product_id);

CREATE INDEX IF NOT EXISTS suse_chanatt_chl_idx
ON suseChannelAttributes (channel_label);

------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS suseChannelRepository
(
    sccchannel_id  BIGINT NOT NULL
                     CONSTRAINT suse_chanrepo_cid_fk
                         REFERENCES suseChannelAttributes (id)
                         ON DELETE CASCADE,
    sccrepo_id     NUMERIC NOT NULL
                     CONSTRAINT suse_chanrepo_rid_fk
                         REFERENCES suseSCCRepository (id)
                         ON DELETE CASCADE
);

CREATE UNIQUE INDEX IF NOT EXISTS suse_chanrepo_cid_rid_uq
    ON suseChannelRepository (sccchannel_id, sccrepo_id);

CREATE INDEX IF NOT EXISTS suse_chanrepo_rid_idx
    ON suseChannelRepository(sccrepo_id);

------------------------------------------------------------------

ALTER TABLE suseSCCRepository
  ADD COLUMN IF NOT EXISTS
    nonoss CHAR(1) DEFAULT ('N') NOT NULL CONSTRAINT suse_sccrepo_nonoss_ck CHECK (nonoss in ('Y', 'N'));

DO $$
  BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'suseproductsccrepository') THEN

      INSERT INTO suseChannelAttributes (product_id, root_product_id, channel_label, parent_channel_label, channel_name, mandatory, update_tag, gpg_key_url, gpg_key_id, gpg_key_fp)
        SELECT product_id, root_product_id, channel_label, parent_channel_label, channel_name, CASE mandatory WHEN 'Y' THEN true ELSE false END AS mandatory, update_tag, gpg_key_url, gpg_key_id, gpg_key_fp
          FROM suseProductSCCRepository
      ORDER BY id;

      INSERT INTO suseChannelRepository
        SELECT ca.id, spcr.repo_id
          FROM suseProductSCCRepository spcr
          JOIN suseChannelAttributes ca ON ca.channel_label = spcr.channel_label AND ca.product_id = spcr.product_id AND ca.root_product_id = spcr.root_product_id;

    ELSE
      RAISE NOTICE 'suseProductSCCRepository does not exists';
    END IF;
  END;
$$;


DROP INDEX IF EXISTS suse_prdrepo_pid_rpid_rid_uq;
DROP INDEX IF EXISTS suse_prdrepo_chl_idx;
DROP SEQUENCE IF EXISTS suse_prdrepo_id_seq;
DROP TABLE IF EXISTS suseProductSCCRepository CASCADE;
