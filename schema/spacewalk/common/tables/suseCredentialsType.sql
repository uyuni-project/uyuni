--
-- Copyright (c) 2012 Novell
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

CREATE TABLE suseCredentialsType
(
    id        NUMERIC NOT NULL
                  CONSTRAINT suse_credtype_id_pk PRIMARY KEY
                  ,
    label     VARCHAR(64) NOT NULL,
    name      VARCHAR(128) NOT NULL,
    created   TIMESTAMPTZ
                  DEFAULT (current_timestamp) NOT NULL,
    modified  TIMESTAMPTZ
                  DEFAULT (current_timestamp) NOT NULL
)

;

CREATE INDEX suse_credtype_label_id_idx
    ON suseCredentialsType (label, id)
    ;

CREATE SEQUENCE suse_credtype_id_seq;

ALTER TABLE suseCredentialsType
    ADD CONSTRAINT suse_credtype_label_uq UNIQUE (label);
