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


CREATE TABLE rhnPackageArch
(
    id            NUMERIC NOT NULL,
    label         VARCHAR(64) NOT NULL,
    name          VARCHAR(64) NOT NULL,
    arch_type_id  NUMERIC NOT NULL
                      CONSTRAINT rhn_parch_atid_fk
                          REFERENCES rhnArchType (id),
    created       TIMESTAMPTZ
                      DEFAULT (current_timestamp) NOT NULL,
    modified      TIMESTAMPTZ
                      DEFAULT (current_timestamp) NOT NULL
)

;

CREATE INDEX rhn_parch_id_l_n_idx
    ON rhnPackageArch (id, label, name)
    ;

CREATE SEQUENCE rhn_package_arch_id_seq START WITH 100;

ALTER TABLE rhnPackageArch
    ADD CONSTRAINT rhn_parch_id_pk PRIMARY KEY (id);

ALTER TABLE rhnPackageArch
    ADD CONSTRAINT rhn_parch_label_uq UNIQUE (label);

