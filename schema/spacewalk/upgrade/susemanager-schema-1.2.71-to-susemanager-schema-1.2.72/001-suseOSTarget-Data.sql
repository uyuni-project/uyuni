--
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

commit;


