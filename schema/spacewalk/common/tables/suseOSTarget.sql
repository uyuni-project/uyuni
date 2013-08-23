--
-- Copyright (c) 2011 Novell
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
suseOSTarget
(
    id            number        not null PRIMARY KEY,
    os            varchar2(200) not null
                  CONSTRAINT suseostarget_os_uq UNIQUE,
    target        varchar2(100) not null,
    channel_arch_id  NUMBER
                  CONSTRAINT suse_ostarget_caid_fk
                  REFERENCES rhnChannelArch (id),
    created   timestamp with local time zone
                  DEFAULT (current_timestamp) NOT NULL,
    modified  timestamp with local time zone
                  DEFAULT (current_timestamp) NOT NULL
);

CREATE SEQUENCE suse_ostarget_id_seq START WITH 100;

