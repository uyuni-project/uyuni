--
-- Copyright (c) 2009--2010 Red Hat, Inc.
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
-- The checksum associated with a package/file/etc...

create table
rhnChecksum
(
        id              number not null
                        constraint rhnChecksum_pk primary key,
        checksum_type_id        number not null
                        constraint rhnChecksum_typeid_fk
                        references rhnChecksumType(id),
        checksum        varchar2(128) not null
)
        enable row movement
;

alter table rhnChecksum add constraint rhnChecksum_chsum_uq
        unique (checksum, checksum_type_id)
        using index tablespace [[32m_tbs]];

CREATE SEQUENCE rhnChecksum_seq;
