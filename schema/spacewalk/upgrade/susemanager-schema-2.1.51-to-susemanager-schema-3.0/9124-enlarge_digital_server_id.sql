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
-- Red Hat trademarks are not licensed under GPLv2. No permission is
-- granted to use or replicate Red Hat trademarks that are incorporated
-- in this software or its documentation.
--
--

-- Before altering the size of the column, we must drop views referring to this
-- column. Then we need to recreate them again.
DROP VIEW rhnentitledservers;

-- Enlarge the size of digital_server_id to 1024.
ALTER TABLE rhnServer
ALTER COLUMN digital_server_id type varchar(1024);

-- Re-create rhnentitledservers view
create or replace view
rhnEntitledServers
as
select distinct
    S.id,
    S.org_id,
    S.digital_server_id,
    S.server_arch_id,
    S.os,
    S.release,
    S.name,
    S.description,
    S.info,
    S.secret
from
    rhnServerGroup SG,
    rhnServerGroupType SGT,
    rhnServerGroupMembers SGM,
    rhnServer S
where
    S.id = SGM.server_id
and SG.id = SGM.server_group_id
and SGT.label IN ('enterprise_entitled', 'bootstrap_entitled',
                  'salt_entitled', 'foreign_entitled')
and SG.group_type = SGT.id
and SG.org_id = S.org_id
;

