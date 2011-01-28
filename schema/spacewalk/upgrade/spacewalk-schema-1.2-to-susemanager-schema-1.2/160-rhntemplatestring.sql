--
-- Copyright (c) 2010 Novell
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--
--

update rhntemplatestring set value = '--the SUSE Manager Team', description = 'Footer for SUSE Manager e-mail'  where label = 'email_footer';
update rhntemplatestring set value = 'Account Information:
Your SUSE Manager login:         <login />
Your SUSE Manager email address: <email-address />', description = 'Account info lines for SUSE Manager e-mail' where label = 'email_account_info';


commit;


