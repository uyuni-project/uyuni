--
-- Copyright (c) 2011 Novell
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--
--

insert into suseOSTarget (id, os, target, channel_arch_id) values
(sequence_nextval('suse_ostarget_id_seq'), 'sle-11-i586', 'sle-11-i586', LOOKUP_CHANNEL_ARCH('channel-ia32'))

insert into suseOSTarget (id, os, target, channel_arch_id) values
(sequence_nextval('suse_ostarget_id_seq'), 'sle-11-ia64', 'sle-11-ia64', LOOKUP_CHANNEL_ARCH('channel-ia64'))

insert into suseOSTarget (id, os, target, channel_arch_id) values
(sequence_nextval('suse_ostarget_id_seq'), 'sle-11-ppc64', 'sle-11-ppc64', LOOKUP_CHANNEL_ARCH('channel-ppc'))

insert into suseOSTarget (id, os, target, channel_arch_id) values
(sequence_nextval('suse_ostarget_id_seq'), 'sle-11-s390x', 'sle-11-s390x', LOOKUP_CHANNEL_ARCH('channel-s390x'))

insert into suseOSTarget (id, os, target, channel_arch_id) values
(sequence_nextval('suse_ostarget_id_seq'), 'sle-11-x86_64', 'sle-11-x86_64', LOOKUP_CHANNEL_ARCH('channel-x86_64'))

-- insert into suseOSTarget (id, os, target, channel_arch_id) values
-- (sequence_nextval('suse_ostarget_id_seq'), '', '', LOOKUP_CHANNEL_ARCH())

commit;

--
-- Revision 1.1  2008/07/02 23:42:28  jsherrill
-- Sequence; data to populate stuff
--

