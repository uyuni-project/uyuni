--
-- Copyright (c) 2008 Red Hat, Inc.
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
--
--
--


create table
rhnChannelContentSource
(
        id              number NOT NULL
                        constraint rhn_ccs_id_pk primary key,
        channel_id      number NOT NULL
                        constraint rhn_ccs_c_fk
                                references rhnChannel(id) on delete cascade,
        type_id         number NOT NULL
                        constraint rhn_ccs_type_fk
                                references rhnContentSourceType(id),
        source_url      varchar2(512) NOT NULL,
        label           varchar2(64) NOT NULL,
        last_synced     date,
        created         date default(sysdate) NOT NULL,
        modified        date default(sysdate) NOT NULL
)
        enable row movement
  ;



create sequence rhn_chan_content_src_id_seq start with 500;


create unique index rhn_ccs_uq
	on rhnChannelContentSource(channel_id, type_id, source_url)
	tablespace [[64k_tbs]]
  ;

show errors

