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
-- SPDX-License-Identifier: GPL-2.0-only
--
--
declare
  c_i386 number := 0;
  c_x86_64 number := 0;
begin
select count(*) into c_i386 from suseOSTarget where target = 'i386';
select count(*) into c_x86_64 from suseOSTarget where target = 'x86_64';

if c_i386 = 0 then
  insert into suseOSTarget (id, os, target, channel_arch_id) values (sequence_nextval('suse_ostarget_id_seq'), 'i386', 'i386', LOOKUP_CHANNEL_ARCH('channel-ia32'));
  commit;
end if;
if c_x86_64 = 0 then
  insert into suseOSTarget (id, os, target, channel_arch_id) values (sequence_nextval('suse_ostarget_id_seq'), 'x86_64', 'x86_64', LOOKUP_CHANNEL_ARCH('channel-x86_64'));
  commit;
end if;

end;
/

