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
-- SPDX-License-Identifier: GPL-2.0-only
--
-- Red Hat trademarks are not licensed under GPLv2. No permission is
-- granted to use or replicate Red Hat trademarks that are incorporated
-- in this software or its documentation.
--


CREATE TABLE rhnConfigContent
(
    id         NUMERIC NOT NULL
                   CONSTRAINT rhn_confcontent_id_pk PRIMARY KEY
                   ,
    contents   BYTEA,
    file_size  NUMERIC,
    checksum_id NUMERIC NOT NULL
                  CONSTRAINT rhn_confcontent_chsum_fk
                  REFERENCES rhnChecksum (id),
    is_binary  CHAR(1)
                   DEFAULT ('N') NOT NULL
                   CONSTRAINT rhn_confcontent_isbin_ck
                       CHECK (is_binary in ('Y','N')),
    delim_start          VARCHAR(16) NOT NULL,
    delim_end            VARCHAR(16) NOT NULL,
    created    TIMESTAMPTZ
                   DEFAULT (current_timestamp) NOT NULL,
    modified   TIMESTAMPTZ
                   DEFAULT (current_timestamp) NOT NULL
)


;

CREATE SEQUENCE rhn_confcontent_id_seq;

