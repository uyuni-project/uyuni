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
--
--
-- 
--

--data for rhn_quanta

insert into rhn_quanta(quantum_id,basic_unit_id,description,last_update_user,
last_update_date) 
    values ( 'none','-','No physical quantity',
    'system',current_timestamp);
insert into rhn_quanta(quantum_id,basic_unit_id,description,last_update_user,
last_update_date) 
    values ( 'time','secs','Measure of time',
    'system',current_timestamp);
insert into rhn_quanta(quantum_id,basic_unit_id,description,last_update_user,
last_update_date) 
    values ( 'data','bytes','Measure of information',
    'system',current_timestamp);
insert into rhn_quanta(quantum_id,basic_unit_id,description,last_update_user,
last_update_date) 
    values ( 'datarate','Bps','Measure of information flow',
    'system',current_timestamp);
insert into rhn_quanta(quantum_id,basic_unit_id,description,last_update_user,
last_update_date) 
    values ( 'frequency','hertz','Measure of freqency',
    'system',current_timestamp);
insert into rhn_quanta(quantum_id,basic_unit_id,description,last_update_user,
last_update_date) 
    values ( 'temp','kelvins','Measure of temperature',
    'system',current_timestamp);
insert into rhn_quanta(quantum_id,basic_unit_id,description,last_update_user,
last_update_date) 
    values ( 'length','metres','Measure of length',
    'system',current_timestamp);
insert into rhn_quanta(quantum_id,basic_unit_id,description,last_update_user,
last_update_date) 
    values ( 'mass','kilograms','Measure of mass',
    'system',current_timestamp);
insert into rhn_quanta(quantum_id,basic_unit_id,description,last_update_user,
last_update_date) 
    values ( 'current','amperes','Measure of electric current',
    'system',current_timestamp);
insert into rhn_quanta(quantum_id,basic_unit_id,description,last_update_user,
last_update_date) 
    values ( 'force','newtons','Measure of force/weight',
    'system',current_timestamp);
insert into rhn_quanta(quantum_id,basic_unit_id,description,last_update_user,
last_update_date) 
    values ( 'power','watts','Measure of power',
    'system',current_timestamp);
insert into rhn_quanta(quantum_id,basic_unit_id,description,last_update_user,
last_update_date) 
    values ( 'charge','coulombs','Measure of charge',
    'system',current_timestamp);
insert into rhn_quanta(quantum_id,basic_unit_id,description,last_update_user,
last_update_date) 
    values ( 'voltage','volts','Measure of electric potential difference',
    'system',current_timestamp);
commit;

