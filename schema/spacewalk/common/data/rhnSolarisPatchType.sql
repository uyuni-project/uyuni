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

insert into rhnSolarisPatchType (id, name, label)
   values (sequence_nextval('rhn_solaris_pt_seq'), 'Generic Patch', 'generic');
insert into rhnSolarisPatchType (id, name, label)
   values (sequence_nextval('rhn_solaris_pt_seq'), 'Kernel Update Patch', 'kernel');
insert into rhnSolarisPatchType (id, name, label)
   values (sequence_nextval('rhn_solaris_pt_seq'), 'Restricted Patch', 'restricted');
insert into rhnSolarisPatchType (id, name, label)
   values (sequence_nextval('rhn_solaris_pt_seq'), 'Point Patch', 'point');
insert into rhnSolarisPatchType (id, name, label)
   values (sequence_nextval('rhn_solaris_pt_seq'), 'Temporary Patch', 'temporary');
insert into rhnSolarisPatchType (id, name, label)
   values (sequence_nextval('rhn_solaris_pt_seq'), 'Nonstandard Patch', 'nonstandard');

