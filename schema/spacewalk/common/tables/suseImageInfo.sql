--
-- Copyright (c) 2017 SUSE LLC
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

CREATE TABLE suseImageInfo
(
    id             NUMERIC NOT NULL
                     CONSTRAINT suse_imginfo_imgid_pk PRIMARY KEY,
    name           VARCHAR(128) NOT NULL,
    version        VARCHAR(128),
    image_type     VARCHAR(32) NOT NULL,
    checksum_id    NUMERIC
                     CONSTRAINT suse_imginfo_chsum_fk
                       REFERENCES rhnChecksum (id),
    image_arch_id  NUMERIC NOT NULL
                       CONSTRAINT suse_imginfo_said_fk
                           REFERENCES rhnServerArch (id),
    curr_revision_num   NUMERIC,
    org_id         NUMERIC NOT NULL
                     CONSTRAINT suse_imginfo_oid_fk
                       REFERENCES web_customer (id)
                       ON DELETE CASCADE,
    build_action_id     NUMERIC,
    inspect_action_id   NUMERIC,
    profile_id     NUMERIC
                     CONSTRAINT suse_imginfo_pid_fk
                       REFERENCES suseImageProfile (profile_id)
                       ON DELETE SET NULL,
    store_id       NUMERIC
                      CONSTRAINT suse_imginfo_sid_fk
                         REFERENCES suseImageStore (id)
                         ON DELETE SET NULL,
    build_server_id  NUMERIC
                      CONSTRAINT suse_imginfo_bsid_fk
                         REFERENCES suseMinionInfo (server_id)
                         ON DELETE SET NULL,
    external_image CHAR(1) DEFAULT ('N') NOT NULL,

    obsolete       CHAR(1) DEFAULT ('N') NOT NULL,

    built          CHAR(1) DEFAULT ('N') NOT NULL,

    pillar_id      NUMERIC
                     CONSTRAINT suse_imginfo_pillar_fk
                       REFERENCES suseSaltPillar (id)
                       ON DELETE SET NULL,
    log            TEXT,

    created        TIMESTAMPTZ
                     DEFAULT (current_timestamp) NOT NULL,
    modified       TIMESTAMPTZ
                     DEFAULT (current_timestamp) NOT NULL,
    CONSTRAINT suse_imginfo_bldaid_fk FOREIGN KEY (build_action_id)
        REFERENCES rhnAction (id) ON DELETE SET NULL,
    CONSTRAINT suse_imginfo_insaid_fk FOREIGN KEY (inspect_action_id)
        REFERENCES rhnAction (id) ON DELETE SET NULL
)

;

CREATE SEQUENCE suse_imginfo_imgid_seq;
