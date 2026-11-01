--
-- Copyright (c) 2008--2010 Red Hat, Inc.
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--
-- SPDX-License-Identifier: GPL-2.0-only
-- 
-- Red Hat trademarks are not licensed under GPLv2. No permission is
-- granted to use or replicate Red Hat trademarks that are incorporated
-- in this software or its documentation. 
--
-- data for rhnVirtualInstanceType


insert into rhnVirtualInstanceType (id, name, label)
     values (sequence_nextval('rhn_vit_id_seq'), 'Fully Virtualized', 'fully_virtualized');

insert into rhnVirtualInstanceType (id, name, label)
     values (sequence_nextval('rhn_vit_id_seq'), 'Para-Virtualized', 'para_virtualized');

insert into rhnVirtualInstanceType (id, name, label)
      values (sequence_nextval('rhn_vit_id_seq'), 'KVM/QEMU', 'qemu');

insert into rhnVirtualInstanceType (id, name, label) 
    values (sequence_nextval('rhn_vit_id_seq'), 'VMware', 'vmware');

insert into rhnVirtualInstanceType (id, name, label) 
    values (sequence_nextval('rhn_vit_id_seq'), 'Hyper-V', 'hyperv');

insert into rhnVirtualInstanceType (id, name, label) 
    values (sequence_nextval('rhn_vit_id_seq'), 'Virtage', 'virtage');

insert into rhnVirtualInstanceType (id, name, label)
    values (sequence_nextval('rhn_vit_id_seq'), 'VirtualBox', 'virtualbox');

insert into rhnVirtualInstanceType (id, name, label)
    values (sequence_nextval('rhn_vit_id_seq'), 'Azure', 'azure');

insert into rhnVirtualInstanceType (id, name, label)
    values (sequence_nextval('rhn_vit_id_seq'), 'Amazon EC2', 'aws');

insert into rhnVirtualInstanceType (id, name, label)
    values (sequence_nextval('rhn_vit_id_seq'), 'Amazon EC2/Nitro', 'aws_nitro');

insert into rhnVirtualInstanceType (id, name, label)
    values (sequence_nextval('rhn_vit_id_seq'), 'Amazon EC2/Xen', 'aws_xen');

insert into rhnVirtualInstanceType (id, name, label)
    values (sequence_nextval('rhn_vit_id_seq'), 'Google CE', 'gce');

insert into rhnVirtualInstanceType (id, name, label)
    values (sequence_nextval('rhn_vit_id_seq'), 'Nutanix AHV', 'nutanix');

insert into rhnVirtualInstanceType (id, name, label)
    values (sequence_nextval('rhn_vit_id_seq'), 'VirtualPC', 'virtualpc');
