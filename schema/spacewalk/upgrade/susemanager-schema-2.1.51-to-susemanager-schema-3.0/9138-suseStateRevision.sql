--
-- Copyright (c) 2015 SUSE LLC
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

CREATE TABLE suseStateRevision
(
    id               NUMERIC NOT NULL
                         CONSTRAINT suse_state_revision_id_pk PRIMARY KEY,
    creator_id       NUMERIC
                         CONSTRAINT suse_state_revision_cid_fk
                             REFERENCES web_contact (id)
                             ON DELETE SET NULL,
    created          TIMESTAMPTZ
                         DEFAULT (current_timestamp) NOT NULL
)

;

CREATE SEQUENCE suse_state_revision_id_seq;
