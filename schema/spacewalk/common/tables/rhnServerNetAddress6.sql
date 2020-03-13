--
-- Copyright (c) 2011--2012 Red Hat, Inc.
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


CREATE TABLE rhnServerNetAddress6
(
    interface_id  NUMERIC NOT NULL
                   CONSTRAINT rhn_srv_net_iaddress6_iid_fk
                       REFERENCES rhnServerNetInterface (id)
                       ON DELETE CASCADE,
    address    VARCHAR(45),
    netmask    VARCHAR(49),
    scope      VARCHAR(64),
    created    TIMESTAMPTZ
                   DEFAULT (current_timestamp) NOT NULL,
    modified   TIMESTAMPTZ
                   DEFAULT (current_timestamp) NOT NULL
)

;

CREATE INDEX rhn_srv_net_ad6_iid_sc_ad_idx
    ON rhnServerNetAddress6 (interface_id, scope, address)
    ;

ALTER TABLE rhnServerNetAddress6
    ADD CONSTRAINT rhn_srv_net_ad6_iid_sc_ad_uq UNIQUE (interface_id, scope, address);
