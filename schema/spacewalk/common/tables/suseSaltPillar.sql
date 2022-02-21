--
-- Copyright (c) 2021 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--
-- Red Hat trademarks are not licensed under GPLv2. No permission is
-- granted to use or replicate Red Hat trademarks that are incorporated
-- in this software or its documentation.
--

CREATE TABLE suseSaltPillar
(
    id NUMERIC NOT NULL
        CONSTRAINT suse_salt_pillar_id_pk PRIMARY KEY,
    server_id NUMERIC
              CONSTRAINT suse_salt_pillar_sid_fk
              REFERENCES rhnServer (id)
              ON DELETE CASCADE,
    group_id NUMERIC
             CONSTRAINT suse_salt_pillar_gid_fk
             REFERENCES rhnServerGroup (id)
             ON DELETE CASCADE,
    org_id NUMERIC
           CONSTRAINT suse_salt_pillar_oid_fk
           REFERENCES web_customer (id)
           ON DELETE CASCADE,
    category VARCHAR NOT NULL,
    pillar JSONB NOT NULL,
    UNIQUE (server_id, category),
    UNIQUE (group_id, category),
    UNIQUE (org_id, category),
    CONSTRAINT suse_salt_pillar_only_one_target CHECK (
      ( server_id is not null and group_id is null and org_id is null ) or
      ( server_id is null and group_id is not null and org_id is null ) or
      ( server_id is null and group_id is null and org_id is not null ) or
      ( server_id is null and group_id is null and org_id is null)
    )
);

CREATE SEQUENCE suse_salt_pillar_id_seq;

CREATE INDEX suse_salt_pillar_server_id_idx ON suseSaltPillar (server_id);

CREATE INDEX suse_salt_pillar_group_id_idx ON suseSaltPillar (group_id);

CREATE INDEX suse_salt_pillar_org_id_idx ON suseSaltPillar (org_id);

CREATE INDEX suse_salt_pillar_category_idx ON suseSaltPillar (category);
