--
-- Copyright (c) 2016 SUSE LLC
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

CREATE TABLE suseCustomState
(
    id               NUMERIC NOT NULL
                         CONSTRAINT suse_custom_state_id_pk PRIMARY KEY,
    org_id           NUMERIC NOT NULL
                         CONSTRAINT suse_custom_state_org_id_fk
                            REFERENCES web_customer (id)
                            ON DELETE CASCADE,
    state_name       VARCHAR(256) NOT NULL,
    state_deleted    char(1)
                           default ('N') not null
                           constraint suse_custom_state_deleted_chk
                           check (state_deleted in ('Y', 'N'))
)

;
CREATE UNIQUE INDEX suse_custom_state_name_org_uq
ON suseCustomState (org_id, state_name)
;

CREATE SEQUENCE suse_custom_state_id_seq;
