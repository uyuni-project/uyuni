-- oracle equivalent source sha1 fb504b4d8fc73f5e380abf9859f2b50dbc55deef
-- retrieved from schema/spacewalk/oracle/triggers/suseNotificationMessage.sql
--
-- Copyright (c) 2010 Novell
-- Copyright (c) 2011 SUSE Linux Products GmbH
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--
--
create or replace function suse_notification_message_mod_trig_fun() returns trigger
as
$$
begin
            new.modified := current_timestamp;

            return new;
end;
$$
language plpgsql;


create trigger
susenotificationmessage_mod_trig
before insert or update on suseNotificationMessage
for each row
execute procedure suse_notification_message_mod_trig_fun();
