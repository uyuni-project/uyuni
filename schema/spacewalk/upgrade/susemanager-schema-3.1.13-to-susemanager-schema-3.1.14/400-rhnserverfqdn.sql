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

CREATE TABLE rhnServerFQDN
(
    id         NUMERIC NOT NULL
                   CONSTRAINT rhn_serverfqdn_id_pk PRIMARY KEY
                   ,
    name       VARCHAR(253) NOT NULL,
    server_id  NUMERIC NOT NULL
                   CONSTRAINT rhn_serverfqdn_sid_fk
                       REFERENCES rhnServer (id),
    created    TIMESTAMPTZ
                   DEFAULT (current_timestamp) NOT NULL,
    modified   TIMESTAMPTZ
                   DEFAULT (current_timestamp) NOT NULL
)

;

CREATE SEQUENCE rhn_serverfqdn_id_seq;

CREATE UNIQUE INDEX rhn_server_fqdn_name_id_idx
    ON rhnServerFQDN (name, server_id)
    
    ;

