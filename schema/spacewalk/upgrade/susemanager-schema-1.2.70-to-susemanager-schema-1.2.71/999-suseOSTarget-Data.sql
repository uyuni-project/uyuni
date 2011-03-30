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
(sequence_nextval('suse_ostarget_id_seq'), 'i386', 'i386', LOOKUP_CHANNEL_ARCH('channel-ia32'));

insert into suseOSTarget (id, os, target, channel_arch_id) values
(sequence_nextval('suse_ostarget_id_seq'), 'x86_64', 'x86_64', LOOKUP_CHANNEL_ARCH('channel-x86_64'));

commit;


