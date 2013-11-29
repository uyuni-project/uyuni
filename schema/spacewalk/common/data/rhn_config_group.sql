--
-- Copyright (c) 2008 Red Hat, Inc.
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
--
--

--data for rhn_config_group

insert into rhn_config_group(name,description) 
    values ( 'general','General configuration parameters');
insert into rhn_config_group(name,description) 
    values ( 'timesync','SUSE Manager time synchronization');
insert into rhn_config_group(name,description) 
    values ( 'oracle','General Oracle database configuration');
insert into rhn_config_group(name,description) 
    values ( 'mail','General mail configuration');
insert into rhn_config_group(name,description) 
    values ( 'innovate','Innovate (trouble ticket DB) configuration');
insert into rhn_config_group(name,description) 
    values ( 'cf_db','Configuration database configuration');
insert into rhn_config_group(name,description) 
    values ( 'cs_db','Current state database configuration');
insert into rhn_config_group(name,description) 
    values ( 'release_db','Release database configuration');
insert into rhn_config_group(name,description) 
    values ( 'notif','Notification system configuration');
insert into rhn_config_group(name,description) 
    values ( 'ts_db','Time series database configuration');
insert into rhn_config_group(name,description) 
    values ( 'TSDBLocalQueue','Time series database local queue configuration');
insert into rhn_config_group(name,description) 
    values ( 'sc_db','State change database configuration');
insert into rhn_config_group(name,description) 
    values ( 'satellite','General SUSE Manager configuration');
insert into rhn_config_group(name,description) 
    values ( 'netsaint','Netsaint configuration (mostly defunct)');
insert into rhn_config_group(name,description) 
    values ( 'PlugFrame','Plugin framework configuration');
insert into rhn_config_group(name,description) 
    values ( 'current_state','Current state database configuration');
insert into rhn_config_group(name,description) 
    values ( 'queues','SUSE Manager dequeuer configuration');
insert into rhn_config_group(name,description) 
    values ( 'ConfigPusher','SUSE Manager configurator configuration');
insert into rhn_config_group(name,description) 
    values ( 'CommandQueue','SputLite configuration');
insert into rhn_config_group(name,description) 
    values ( 'gritch','Gritch (throttled OOB notifications) configuration');
insert into rhn_config_group(name,description) 
    values ( 'trapReceiver','SNMP Trap Receiver configuration');
insert into rhn_config_group(name,description) 
    values ( 'ssl_bridge','SSL bridge configuration');
insert into rhn_config_group(name,description) 
    values ( 'SpreadBridge','Spread bridge configuration');
insert into rhn_config_group(name,description) 
    values ( 'SuperSput','SuperSput configuration');
insert into rhn_config_group(name,description) 
    values ( 'Discoverer','Discoverer configuration');
insert into rhn_config_group(name,description) 
    values ( 'notification','Notification system configuration');
insert into rhn_config_group(name,description) 
    values ( 'hosts','Host information');
insert into rhn_config_group(name,description) 
    values ( 'ProbeFramework','Probe framework configuration');
commit;

