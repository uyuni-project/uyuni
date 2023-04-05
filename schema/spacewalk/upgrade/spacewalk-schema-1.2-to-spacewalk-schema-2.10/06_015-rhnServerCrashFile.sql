--
-- Copyright (c) 2013 Red Hat, Inc.
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

create table rhnServerCrashFile
(
    id          NUMERIC not null
                constraint rhn_server_crash_file_id_pk primary key,
    crash_id    NUMERIC not null
                constraint rhn_server_crash_file_cis_fk
                    references rhnServerCrash(id)
                    on delete cascade,
    filename    VARCHAR(512) not null,
    path        VARCHAR(1024) not null,
    filesize    NUMERIC not null,
    is_uploaded char(1)
                default ('N') not null
                constraint rhn_server_crash_file_upl_ck
                check (is_uploaded in ('Y', 'N')),
    created     TIMESTAMPTZ
                    default (current_timestamp) not null,
    modified    TIMESTAMPTZ
                    default (current_timestamp) not null
)

;

create sequence rhn_server_crash_file_id_seq start with 1;

create unique index rhn_scrf_cid_fn
    on rhnServerCrashFile (crash_id, filename)
    ;
