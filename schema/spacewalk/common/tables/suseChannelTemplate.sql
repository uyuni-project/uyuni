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
-- SPDX-License-Identifier: GPL-2.0-only
--

CREATE TABLE
suseChannelTemplate
(
    id                     BIGINT CONSTRAINT suse_chantpl_id_pk PRIMARY KEY
                                  GENERATED ALWAYS AS IDENTITY,
    product_id             NUMERIC NOT NULL
                                  CONSTRAINT suse_chantpl_pid_fk
                                  REFERENCES suseProducts (id)
                                  ON DELETE CASCADE,
    root_product_id        NUMERIC NOT NULL
                                  CONSTRAINT suse_chantpl_rpid_fk
                                  REFERENCES suseProducts (id)
                                  ON DELETE CASCADE,
    repo_id                NUMERIC NOT NULL
                                  CONSTRAINT suse_chantpl_rid_fk
                                  REFERENCES suseSCCRepository (id)
                                  ON DELETE CASCADE,
    channel_label          VARCHAR(128) NOT NULL,
    parent_channel_label   VARCHAR(128),
    channel_name           VARCHAR(256) NOT NULL,
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

CREATE UNIQUE INDEX suse_chantpl_pid_rpid_rid_uq
ON suseChannelTemplate (product_id, root_product_id, repo_id);

CREATE INDEX suse_chantpl_rpid_idx
ON suseChannelTemplate (root_product_id);

CREATE INDEX suse_chantpl_chl_idx
ON suseChannelTemplate (channel_label);
