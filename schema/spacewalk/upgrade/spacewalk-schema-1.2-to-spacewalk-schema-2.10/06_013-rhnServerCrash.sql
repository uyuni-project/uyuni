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

create table rhnServerCrash
(
    id              NUMERIC not null
                    constraint rhn_server_crash_id_pk primary key,
    server_id       NUMERIC not null
                    constraint rhn_server_crash_sid_fk
                        references rhnServer(id)
                        on delete cascade,
    crash           VARCHAR(512) not null,
    path            VARCHAR(1024) not null,
    count           NUMERIC not null,
    analyzer        VARCHAR(128),
    architecture    VARCHAR(16),
    cmdline         VARCHAR(2048),
    component       VARCHAR(256),
    executable      VARCHAR(512),
    kernel          VARCHAR(128),
    reason          VARCHAR(512),
    username        VARCHAR(256),
    package_name_id NUMERIC
                    constraint rhn_server_crash_pname_id_fk
                        references rhnPackageName(id),
    package_evr_id  NUMERIC
                    constraint rhn_server_crash_evr_id_fk
                        references rhnPackageEVR(id),
    package_arch_id NUMERIC
                    constraint rhn_server_crash_arch_id_fk
                        references rhnPackageArch(id),
    storage_path    VARCHAR(1024),
    created         TIMESTAMPTZ
                        default (current_timestamp) not null,
    modified        TIMESTAMPTZ
                        default (current_timestamp) not null
)

;

create sequence rhn_server_crash_id_seq start with 1;

create unique index rhn_scr_sid_crash
    on rhnServerCrash (server_id, crash)
    ;
