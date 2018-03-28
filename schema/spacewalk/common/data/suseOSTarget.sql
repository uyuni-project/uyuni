--
-- Copyright (c) 2011 Novell
-- Copyright (c) 2011 SUSE Linux Products GmbH, Nuremberg, Germany
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
(sequence_nextval('suse_ostarget_id_seq'), 'sle-11-i586', 'sle-11-i586', LOOKUP_CHANNEL_ARCH('channel-ia32'));

insert into suseOSTarget (id, os, target, channel_arch_id) values
(sequence_nextval('suse_ostarget_id_seq'), 'sle-11-ia64', 'sle-11-ia64', LOOKUP_CHANNEL_ARCH('channel-ia64'));

insert into suseOSTarget (id, os, target, channel_arch_id) values
(sequence_nextval('suse_ostarget_id_seq'), 'sle-11-ppc64', 'sle-11-ppc64', LOOKUP_CHANNEL_ARCH('channel-ppc'));

insert into suseOSTarget (id, os, target, channel_arch_id) values
(sequence_nextval('suse_ostarget_id_seq'), 'sle-11-s390x', 'sle-11-s390x', LOOKUP_CHANNEL_ARCH('channel-s390x'));

insert into suseOSTarget (id, os, target, channel_arch_id) values
(sequence_nextval('suse_ostarget_id_seq'), 'sle-11-x86_64', 'sle-11-x86_64', LOOKUP_CHANNEL_ARCH('channel-x86_64'));

insert into suseOSTarget (id, os, target, channel_arch_id) values
(sequence_nextval('suse_ostarget_id_seq'), 'i386', 'i386', LOOKUP_CHANNEL_ARCH('channel-ia32'));

insert into suseOSTarget (id, os, target, channel_arch_id) values
(sequence_nextval('suse_ostarget_id_seq'), 'x86_64', 'x86_64', LOOKUP_CHANNEL_ARCH('channel-x86_64'));

insert into suseOSTarget (id, os, target, channel_arch_id) values
(sequence_nextval('suse_ostarget_id_seq'), 'sled-10-i586', 'sled-10-i586', LOOKUP_CHANNEL_ARCH('channel-ia32'));

insert into suseOSTarget (id, os, target, channel_arch_id) values
(sequence_nextval('suse_ostarget_id_seq'), 'sled-10-x86_64', 'sled-10-x86_64', LOOKUP_CHANNEL_ARCH('channel-x86_64'));

insert into suseOSTarget (id, os, target, channel_arch_id) values
(sequence_nextval('suse_ostarget_id_seq'), 'sles-10-i586', 'sles-10-i586', LOOKUP_CHANNEL_ARCH('channel-ia32'));

insert into suseOSTarget (id, os, target, channel_arch_id) values
(sequence_nextval('suse_ostarget_id_seq'), 'sles-10-ia64', 'sles-10-ia64', LOOKUP_CHANNEL_ARCH('channel-ia64'));

insert into suseOSTarget (id, os, target, channel_arch_id) values
(sequence_nextval('suse_ostarget_id_seq'), 'sles-10-ppc', 'sles-10-ppc', LOOKUP_CHANNEL_ARCH('channel-ppc'));

insert into suseOSTarget (id, os, target, channel_arch_id) values
(sequence_nextval('suse_ostarget_id_seq'), 'sles-10-s390x', 'sles-10-s390x', LOOKUP_CHANNEL_ARCH('channel-s390x'));

insert into suseOSTarget (id, os, target, channel_arch_id) values
(sequence_nextval('suse_ostarget_id_seq'), 'sles-10-s390', 'sles-10-s390', LOOKUP_CHANNEL_ARCH('channel-s390'));

insert into suseOSTarget (id, os, target, channel_arch_id) values
(sequence_nextval('suse_ostarget_id_seq'), 'sles-10-x86_64', 'sles-10-x86_64', LOOKUP_CHANNEL_ARCH('channel-x86_64'));

insert into suseOSTarget (id, os, target, channel_arch_id) values
(sequence_nextval('suse_ostarget_id_seq'), 'sled-10-i686', 'sled-10-i586', LOOKUP_CHANNEL_ARCH('channel-ia32'));

insert into suseOSTarget (id, os, target, channel_arch_id) values
(sequence_nextval('suse_ostarget_id_seq'), 'sles-10-i686', 'sles-10-i586', LOOKUP_CHANNEL_ARCH('channel-ia32'));

insert into suseOSTarget (id, os, target, channel_arch_id) values
(sequence_nextval('suse_ostarget_id_seq'), 'sle-12-ppc64le', 'sle-12-ppc64le', LOOKUP_CHANNEL_ARCH('channel-ppc64le'));

insert into suseOSTarget (id, os, target, channel_arch_id) values
(sequence_nextval('suse_ostarget_id_seq'), 'sle-12-s390x', 'sle-12-s390x', LOOKUP_CHANNEL_ARCH('channel-s390x'));

insert into suseOSTarget (id, os, target, channel_arch_id) values
(sequence_nextval('suse_ostarget_id_seq'), 'sle-12-x86_64', 'sle-12-x86_64', LOOKUP_CHANNEL_ARCH('channel-x86_64'));

insert into suseOSTarget (id, os, target, channel_arch_id) values
(sequence_nextval('suse_ostarget_id_seq'), 'sle-15-x86_64', 'sle-15-x86_64', LOOKUP_CHANNEL_ARCH('channel-x86_64'));

insert into suseOSTarget (id, os, target, channel_arch_id) values
(sequence_nextval('suse_ostarget_id_seq'), 'sle-15-s390x', 'sle-15-s390x', LOOKUP_CHANNEL_ARCH('channel-s390x'));

insert into suseOSTarget (id, os, target, channel_arch_id) values
(sequence_nextval('suse_ostarget_id_seq'), 'sle-15-ppc64le', 'sle-15-ppc64le', LOOKUP_CHANNEL_ARCH('channel-ppc64le'));

commit;

--
-- Revision 1.1  2008/07/02 23:42:28  jsherrill
-- Sequence; data to populate stuff
--

