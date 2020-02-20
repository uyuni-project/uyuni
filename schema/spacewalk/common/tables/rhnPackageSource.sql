--
-- Copyright (c) 2008--2012 Red Hat, Inc.
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


CREATE TABLE rhnPackageSource
(
    id             NUMERIC NOT NULL
                       CONSTRAINT rhn_pkgsrc_id_pk PRIMARY KEY
                       ,
    org_id         NUMERIC
                       CONSTRAINT rhn_pkgsrc_oid_fk
                           REFERENCES web_customer (id)
                           ON DELETE CASCADE,
    source_rpm_id  NUMERIC NOT NULL
                       CONSTRAINT rhn_pkgsrc_srid_fk
                           REFERENCES rhnSourceRPM (id),
    package_group  NUMERIC NOT NULL
                       CONSTRAINT rhn_pkgsrc_group_fk
                           REFERENCES rhnPackageGroup (id),
    rpm_version    VARCHAR(16) NOT NULL,
    payload_size   NUMERIC NOT NULL,
    build_host     VARCHAR(256) NOT NULL,
    build_time     TIMESTAMPTZ NOT NULL,
    sigchecksum_id NUMERIC NOT NULL
                      CONSTRAINT rhn_pkgsrc_sigchsum_fk
                          REFERENCES rhnChecksum (id),
    vendor         VARCHAR(64) NOT NULL,
    cookie         VARCHAR(128) NOT NULL,
    path           VARCHAR(1000),
    checksum_id    NUMERIC NOT NULL
                      CONSTRAINT rhn_pkgsrc_chsum_fk
                          REFERENCES rhnChecksum (id),
    package_size   NUMERIC NOT NULL,
    last_modified  TIMESTAMPTZ
                       DEFAULT (current_timestamp) NOT NULL,
    created        TIMESTAMPTZ
                       DEFAULT (current_timestamp) NOT NULL,
    modified       TIMESTAMPTZ
                       DEFAULT (current_timestamp) NOT NULL
)

;

CREATE UNIQUE INDEX rhn_pkgsrc_srid_oid_uq
    ON rhnPackageSource (source_rpm_id, org_id)
    ;

CREATE SEQUENCE rhn_package_source_id_seq;

