--
-- Copyright (c) 2012 Novell, Inc.
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--
--
--

update rhnKickstartSessionState
   set description='Autoinstallation session created, but has not yet been used.'
 where label='created';

update rhnKickstartSessionState
   set description='Files required for autoinstall action have been installed.'
 where label='deployed';

update rhnKickstartSessionState
   set description='The system configuration has been modified to begin autoinstallation upon next boot.'
 where label='injected';

update rhnKickstartSessionState
   set description='The system has been restarted in order to begin the autoinstallation process.'
 where label='restarted';

update rhnKickstartSessionState
   set description='The system has downloaded the autoinstallation configuraton file from Spacewalk.'
 where label='configuration_accessed';

update rhnKickstartSessionState
   set description='The initial files required for autoinstallation have been downloaded.'
 where label='started';

update rhnKickstartSessionState
   set description='The system has successfully registered with Spacewalk after autoinstalling.'
 where label='registered';

update rhnKickstartSessionState
   set description='Autoinstallation complete.'
 where label='complete';

update rhnKickstartSessionState
   set description='Autoinstallation failed.'
 where label='failed';

commit;

