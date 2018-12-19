--
-- Copyright (c) 2010-2012 Novell
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--
--

create table
suseProductChannel
(
    id NUMBER NOT NULL
        CONSTRAINT suse_product_channel_id_pk PRIMARY KEY,
    product_id number        not null
                             CONSTRAINT spc_pid_fk
                             REFERENCES suseProducts (id)
                             ON DELETE CASCADE,
    channel_id number        not null
                             CONSTRAINT spc_rhn_cid_fk
                             REFERENCES rhnChannel (id)
                             ON DELETE CASCADE,
    mandatory  CHAR(1)       DEFAULT ('N') NOT NULL
                             CONSTRAINT spc_mand_ck
                             CHECK (mandatory in ('Y', 'N')),
    created   timestamp with local time zone
                  DEFAULT (current_timestamp) NOT NULL,
    modified  timestamp with local time zone
                  DEFAULT (current_timestamp) NOT NULL
);

CREATE SEQUENCE suse_product_channel_id_seq;

