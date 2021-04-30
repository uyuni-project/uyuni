--
-- Copyright (c) 2008--2013 Red Hat, Inc.
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
-- data for rhnServerGroupType

-- enterprise_entitled type --------------------------------------------------

insert into rhnServerGroupType (id, label, name, permanent, is_base)
        values (sequence_nextval('rhn_servergroup_type_seq'),
                'enterprise_entitled', 'Spacewalk Management Entitled Servers', 
                'Y', 'Y'
        );

-- virtualization_host type ----------------------------------------------------

insert into rhnServerGroupType ( id, label, name, permanent, is_base)
   values ( sequence_nextval('rhn_servergroup_type_seq'),
      'virtualization_host', 'Virtualization Host Entitled Servers',
      'N', 'N'
   );

--  bootstrap_entitled type ----------------------------------------------------

insert into rhnServerGroupType ( id, label, name, permanent, is_base)
   values ( sequence_nextval('rhn_servergroup_type_seq'),
      'bootstrap_entitled', 'Bootstrap Entitled Servers',
      'Y', 'Y'
   );

--  salt_entitled type ---------------------------------------------------------

insert into rhnServerGroupType (id, label, name, permanent, is_base)
   values (sequence_nextval('rhn_servergroup_type_seq'),
      'salt_entitled', 'Salt Management Entitled Servers',
      'Y', 'Y'
   );

-- foreign_entitled type ---------------------------------------------------------

insert into rhnServerGroupType ( id, label, name, permanent, is_base)
   values ( sequence_nextval('rhn_servergroup_type_seq'),
      'foreign_entitled', 'Foreign Entitled Servers',
      'Y', 'Y'
   );

-- container_build_host type ----------------------------------------------------

insert into rhnServerGroupType ( id, label, name, permanent, is_base)
   values ( sequence_nextval('rhn_servergroup_type_seq'),
      'container_build_host', 'Container Build Host',
      'N', 'N'
   );

-- osimage_build_host type ----------------------------------------------------

insert into rhnServerGroupType ( id, label, name, permanent, is_base)
   values ( sequence_nextval('rhn_servergroup_type_seq'),
      'osimage_build_host', 'OS Image Build Host',
      'N', 'N'
   );

-- monitoring_entitled type ----------------------------------------------------

insert into rhnServerGroupType (id, label, name, permanent, is_base)
   values (sequence_nextval('rhn_servergroup_type_seq'),
      'monitoring_entitled', 'Monitoring',
      'N', 'N'
   );

-- ansible_control_node type ---------------------------------------------------

insert into rhnServerGroupType ( id, label, name, permanent, is_base)
   values ( sequence_nextval('rhn_servergroup_type_seq'),
      'ansible_control_node', 'Ansible Control Node',
      'N', 'N'
   );

commit;
