--
-- Copyright (c) 2014 Red Hat, Inc.
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


CREATE TABLE rhnChildChannelArchCompat
(
    parent_arch_id  NUMERIC NOT NULL
                         CONSTRAINT rhn_ccac_paid_fk
                             REFERENCES rhnChannelArch (id),
    child_arch_id  NUMERIC NOT NULL
                         CONSTRAINT rhn_ccac_caid_fk
                             REFERENCES rhnChannelArch (id),
    created          TIMESTAMPTZ
                         DEFAULT (current_timestamp) NOT NULL,
    modified         TIMESTAMPTZ
                         DEFAULT (current_timestamp) NOT NULL
)

;

CREATE INDEX rhn_ccac_paid_caid
    ON rhnChildChannelArchCompat (parent_arch_id, child_arch_id)
    ;

ALTER TABLE rhnChildChannelArchCompat
    ADD CONSTRAINT rhn_ccac_paid_caid_uq UNIQUE (parent_arch_id, child_arch_id);

