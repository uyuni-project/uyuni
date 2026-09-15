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
-- SPDX-License-Identifier: GPL-2.0-only
--
--

create table
suseMdKeyword
(
    id                NUMERIC PRIMARY KEY,
    label             varchar(128) NOT NULL,
    created   TIMESTAMPTZ
                  DEFAULT (current_timestamp) NOT NULL,
    modified  TIMESTAMPTZ
                  DEFAULT (current_timestamp) NOT NULL
)

;

CREATE SEQUENCE suse_mdkeyword_id_seq;

CREATE UNIQUE INDEX susemdkeyword_label_uq
ON suseMdKeyword (label);
