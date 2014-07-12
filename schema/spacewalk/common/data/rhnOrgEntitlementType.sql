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
-- data for rhnOrgEntitlementType

insert into rhnOrgEntitlementType (id, label, name)
        values (sequence_nextval('rhn_org_entitlement_type_seq'),
                'sw_mgr_enterprise','Software Manager Enterprise'
        );

insert into rhnOrgEntitlementType (id, label, name)
        values (sequence_nextval('rhn_org_entitlement_type_seq'),
                'rhn_monitor','Spacewalk Monitoring'
        );

insert into rhnOrgEntitlementType (id, label, name)
        values (sequence_nextval('rhn_org_entitlement_type_seq'),
                'rhn_provisioning','Spacewalk Provisioning'
        );

insert into rhnOrgEntitlementType (id, label, name)
        values (sequence_nextval('rhn_org_entitlement_type_seq'),
                'rhn_nonlinux','Spacewalk Non-Linux'
        );

insert into rhnOrgEntitlementType (id, label, name)
        values (sequence_nextval('rhn_org_entitlement_type_seq'),
                'rhn_virtualization', 'Spacewalk Virtualization'
        );

insert into rhnOrgEntitlementType (id, label, name)
        values (sequence_nextval('rhn_org_entitlement_type_seq'),
                'rhn_virtualization_platform', 'Spacewalk Virtualization Platform'
        );


commit;

