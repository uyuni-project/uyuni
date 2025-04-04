--
-- Copyright (c) 2009-2025 SUSE LLC
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

insert into rhnSGTypeBaseAddonCompat (base_id, addon_id)
values (lookup_sg_type('enterprise_entitled'),
        lookup_sg_type('virtualization_host'));

insert into rhnSGTypeBaseAddonCompat (base_id, addon_id)
values (lookup_sg_type('salt_entitled'),
        lookup_sg_type('virtualization_host'));

insert into rhnSGTypeBaseAddonCompat (base_id, addon_id)
values (lookup_sg_type('salt_entitled'),
        lookup_sg_type('container_build_host'));

insert into rhnSGTypeBaseAddonCompat (base_id, addon_id)
values (lookup_sg_type('salt_entitled'),
        lookup_sg_type('osimage_build_host'));

insert into rhnSGTypeBaseAddonCompat (base_id, addon_id)
values (lookup_sg_type('salt_entitled'),
        lookup_sg_type('monitoring_entitled'));

insert into rhnSGTypeBaseAddonCompat (base_id, addon_id)
values (lookup_sg_type('salt_entitled'),
        lookup_sg_type('ansible_control_node'));

insert into rhnSGTypeBaseAddonCompat (base_id, addon_id)
values (lookup_sg_type('salt_entitled'),
        lookup_sg_type('peripheral_server'));

insert into rhnSGTypeBaseAddonCompat (base_id, addon_id)
values (lookup_sg_type('foreign_entitled'),
        lookup_sg_type('peripheral_server'));

insert into rhnSGTypeBaseAddonCompat (base_id, addon_id)
values (lookup_sg_type('salt_entitled'),
        lookup_sg_type('ansible_managed'));

insert into rhnSGTypeBaseAddonCompat (base_id, addon_id)
values (lookup_sg_type('salt_entitled'),
        lookup_sg_type('proxy_entitled'));

insert into rhnSGTypeBaseAddonCompat (base_id, addon_id)
values (lookup_sg_type('foreign_entitled'),
        lookup_sg_type('proxy_entitled'));

commit;
