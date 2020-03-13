--
-- Copyright (c) 2008--2017 Red Hat, Inc.
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
rhnContentSource
(
        id		NUMERIC NOT NULL
			constraint rhn_cs_id_pk primary key,
        org_id		NUMERIC
			constraint rhn_cs_org_fk
                                references web_customer (id),
        type_id         NUMERIC NOT NULL
                        constraint rhn_cs_type_fk
                                references rhnContentSourceType(id),
        source_url      VARCHAR(2048) NOT NULL,
        label           VARCHAR(128) NOT NULL,
        metadata_signed CHAR(1)
                            DEFAULT ('Y') NOT NULL
                            CONSTRAINT rhn_cs_ms_ck
                                CHECK (metadata_signed in ( 'Y' , 'N' )),
        created         TIMESTAMPTZ default(current_timestamp) NOT NULL,
        modified        TIMESTAMPTZ default(current_timestamp) NOT NULL
)

  ;


create sequence rhn_chan_content_src_id_seq start with 500;

CREATE UNIQUE INDEX rhn_cs_label_uq
    ON rhnContentSource(COALESCE(org_id, 0), label)
    ;
CREATE UNIQUE INDEX rhn_cs_repo_uq
    ON rhnContentSource(COALESCE(org_id, 0), type_id, source_url,
                        (case when label like 'manifest_%' then 1 else 0 end))
    ;

