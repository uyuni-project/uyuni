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
--data for rhn_metrics
--metrics for linux and scout commands only

insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'accesses','eps','Request Rate','system',current_timestamp,'Request rate','Apache::Traffic');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'traffic','Kbps','Traffic','system',current_timestamp,'Traffic','Apache::Traffic');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'load1','count','CPU Load 1-Minute Average','system',current_timestamp,'CPU load 1-min ave','Unix::Load');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'open','count','Open Objects','system',current_timestamp,'Open objects','MySQL::Open');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'pingtime','millisecs','Round-trip Average','system',current_timestamp,'Round-trip avg','NetworkService::Ping');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'nusers','count','Users','system',current_timestamp,'Users','Unix::Users');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'nprocs','count','Processes','system',current_timestamp,'Processes','Unix::ProcessCountTotal');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'probes','count','Probes','system',current_timestamp,'Probes','Satellite::ProbeCount');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'probelatnc','secs','Probe Latency Average','system',current_timestamp,'Probe latency ave','Satellite::ProbeLatency');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'free','Mb','RAM Free','system',current_timestamp,'RAM free','Unix::MemoryFree');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'load5','count','CPU Load 5-Minute Average','system',current_timestamp,'CPU load 5-min ave','Unix::Load');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'load15','count','CPU Load 15-Minute Average','system',current_timestamp,'CPU load 15-min ave','Unix::Load');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'opened','count','Opened Objects','system',current_timestamp,'Opened objects','MySQL::Opened');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'probextm','secs','Probe Execution Time Average','system',current_timestamp,'Probe exec time ave','Satellite::ProbeExecTime');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'regmatches','count','Regular Expression Matches','system',current_timestamp,'Pattern matches','LogAgent::PatternMatch');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'regrate','epm','Regular Expression Match Rate','system',current_timestamp,'Pattern match rate','LogAgent::PatternMatch');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'bytes','bytes','Size','system',current_timestamp,'Size','LogAgent::Size');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'byterate','bpm','Output Rate','system',current_timestamp,'Output rate','LogAgent::Size');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'lines','count','Lines','system',current_timestamp,'Lines','LogAgent::Size');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'linerate','lpm','Line Rate','system',current_timestamp,'Line rate','LogAgent::Size');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'reqs','count','Current Requests','system',current_timestamp,'Current requests','Apache::Traffic');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'nblocked','count','Blocked Processes','system',current_timestamp,'Blocked processes','Unix::ProcessStateCounts');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'nchildren','count','Child Process Groups','system',current_timestamp,'Child process groups','Unix::Process');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'ndefunct','count','Defunct Processes','system',current_timestamp,'Defunct processes','Unix::ProcessStateCounts');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'nstopped','count','Stopped Processes','system',current_timestamp,'Stopped processes','Unix::ProcessStateCounts');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'nswapped','count','Sleeping Processes','system',current_timestamp,'Sleeping processes','Unix::ProcessStateCounts');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'nthreads','count','Threads','system',current_timestamp,'Threads','Unix::Process');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'vsz','count','Virtual Memory Used','system',current_timestamp,'Virtual memory used','Unix::Process');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'kbrps','Kbps','Read Rate','system',current_timestamp,'Read rate','Unix::DiskIOThroughput');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'kbwps','Kbps','Write Rate','system',current_timestamp,'Write rate','Unix::DiskIOThroughput');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'pctused','percent','Filesystem Used','system',current_timestamp,'Filesystem pct used','Unix::Disk');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'pctfree','percent','Virtual Memory Free','system',current_timestamp,'Virtual mem pct free','Unix::VirtualMemory');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'pctfree','percent','Swap Space Free','system',current_timestamp,'Swap pct free','Unix::Swap');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'pctused','percent','CPU Used','system',current_timestamp,'CPU pct used','Unix::CPU');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'pctlost','percent','Packet Loss','system',current_timestamp,'Packet loss pct','NetworkService::Ping');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'pctiused','percent','Inodes Used','system',current_timestamp,'Inodes pct used','Unix::Inodes');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'childmb','Mb','Max Data Transferred Per Child','system',current_timestamp,'Max transferred per child','Apache::Processes');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'slotmb','Mb','Max Data Transferred Per Slot','system',current_timestamp,'Max transferred per slot','Apache::Processes');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'actsession','count','Active Sessions','system',current_timestamp,'Active sessions','Oracle::ActiveSessions');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'blksession','count','Blocking Sessions','system',current_timestamp,'Blocking sessions','Oracle::BlockingSessions');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'idlsession','count','Idle Sessions','system',current_timestamp,'Idle sessions','Oracle::IdleSessions');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'latency','secs','Remote Service Latency','system',current_timestamp,'Latency','NetworkService::FTP');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'latency','secs','Remote Service Latency','system',current_timestamp,'Latency','NetworkService::HTTP');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'latency','secs','Remote Service Latency','system',current_timestamp,'Latency','NetworkService::HTTPS');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'latency','secs','Remote Service Latency','system',current_timestamp,'Latency','NetworkService::IMAP');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'qps','eps','Query Rate','system',current_timestamp,'Query rate','MySQL::Queries');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'threads','count','Threads Running','system',current_timestamp,'Threads running','MySQL::Threads');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'value','count','Value','system',current_timestamp,'Value','General::SNMPCheck');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'latency','secs','Remote Service Latency','system',current_timestamp,'Latency','NetworkService::POP');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'latency','secs','Remote Service Latency','system',current_timestamp,'Latency','NetworkService::RPC');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'latency','secs','Remote Service Latency','system',current_timestamp,'Latency','NetworkService::SMTP');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'latency','secs','Remote Service Latency','system',current_timestamp,'Latency','NetworkService::SSH');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'latency','secs','Remote Service Latency','system',current_timestamp,'Latency','General::TCP');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'nconns','count','Total Connections','system',current_timestamp,'Total conns','Unix::TCPConnectionsByState');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'latency','secs','Remote Service Latency','system',current_timestamp,'Latency','Oracle::TNSping');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'value','count','Value','system',current_timestamp,'value','General::RemoteProgramWithData');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'value','bytes','Value','system',current_timestamp,'Value','Weblogic::HeapFree');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'hit_ratio','percent','Buffer Cache Hit Ratio','system',current_timestamp,'Buffer cache hit ratio','Oracle::BufferCache');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'db_block_gets','epm','DB Block Get Rate','system',current_timestamp,'DB block get rate','Oracle::BufferCache');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'consistent_gets','epm','Consistent Get Rate','system',current_timestamp,'Consistent get rate','Oracle::BufferCache');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'physical_reads','epm','Physical Read Rate','system',current_timestamp,'Physical read rate','Oracle::BufferCache');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'hit_ratio','percent','Data Dictionary Hit Ratio','system',current_timestamp,'Data dict hit ratio','Oracle::DataDictionaryCache');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'gets','epm','Get Rate','system',current_timestamp,'Get rate','Oracle::DataDictionaryCache');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'get_misses','epm','Cache Miss Rate','system',current_timestamp,'Cache miss rate','Oracle::DataDictionaryCache');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'miss_ratio','percent','Library Cache Miss Ratio','system',current_timestamp,'Library cache miss ratio','Oracle::LibraryCache');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'executions','epm','Execution Rate','system',current_timestamp,'Execution rate','Oracle::LibraryCache');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'misses','epm','Cache Miss Rate','system',current_timestamp,'Cache miss rate','Oracle::LibraryCache');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'usedsesspct','percent','Used Sessions','system',current_timestamp,'Used Sessions','Oracle::ActiveSessions');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'locks','count','Active Locks','system',current_timestamp,'Active locks','Oracle::Locks');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'requests','epm','Redo Log Space Request Rate','system',current_timestamp,'Redo log space request rate','Oracle::RedoLog');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'retries','epm','Redo Buffer Allocation Retry Rate','system',current_timestamp,'Redo buffer allocation retry rate','Oracle::RedoLog');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'ratio','percent','Disk Sort Ratio','system',current_timestamp,'Disk sort ratio','Oracle::DiskSort');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'dsk_sorts','epm','Disk Sort Rate','system',current_timestamp,'Disk sort rate','Oracle::DiskSort');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'mem_sorts','epm','Memory Sort Rate','system',current_timestamp,'Memory sort rate','Oracle::DiskSort');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'connections','count','Connections','system',current_timestamp,'Connections','Weblogic::JDBCConnectionPool');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'waiters','count','Waiters','system',current_timestamp,'Waiters','Weblogic::JDBCConnectionPool');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'conn_rate','eps','Connection Rate','system',current_timestamp,'Conn rate','Weblogic::JDBCConnectionPool');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'idle_threads','count','Idle Execute Threads','system',current_timestamp,'Idle threads','Weblogic::ExecuteQueue');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'queue_length','count','Queue Length','system',current_timestamp,'Queue length','Weblogic::ExecuteQueue');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'request_rate','eps','Request Rate','system',current_timestamp,'Request rate','Weblogic::ExecuteQueue');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'exec_move_ave','count','Execution Time Moving Average','system',current_timestamp,'Exec time moving ave','Weblogic::Servlet');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'avg_exec_time','count','Execution Time Ave','system',current_timestamp,'Execution time ave','Weblogic::Servlet');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'reload_rate','epm','Reload Rate','system',current_timestamp,'Reload rate','Weblogic::Servlet');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'invocation_rate','epm','Invocation Rate','system',current_timestamp,'Invocation rate','Weblogic::Servlet');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'high_exec_time','count','High Execution Time','system',current_timestamp,'High exec time','Weblogic::Servlet');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'low_exec_time','count','Low Execution Time','system',current_timestamp,'Low exec time','Weblogic::Servlet');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'space_used','Mb','Space Used','system',current_timestamp,'Space used','Unix::Disk');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'space_avail','Mb','Space Available','system',current_timestamp,'Space available','Unix::Disk');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'out_byte_rt','bps','Output Rate','system',current_timestamp,'Output rate','Unix::InterfaceTraffic');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'in_byte_rt','bps','Input Rate','system',current_timestamp,'Input rate','Unix::InterfaceTraffic');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'load1','percent','CPU Load 1-Minute Average','system',current_timestamp,'CPU load 1-min avg','Windows::LoadAverage');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'load5','percent','CPU Load 5-Minute Average','system',current_timestamp,'CPU load 5-min avg','Windows::LoadAverage');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'load15','percent','CPU Load 15-Minute Average','system',current_timestamp,'CPU load 15-min avg','Windows::LoadAverage');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'physical_mem_used','count','Physical Memory Used','system',current_timestamp,'Physical memory used','Unix::Process');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'cpu_time_rt','msps','CPU Usage','system',current_timestamp,'CPU usage','Unix::Process');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'time_wait_conn','count','TIME_WAIT Connections','system',current_timestamp,'TIME_WAIT conns','Unix::TCPConnectionsByState');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'close_wait_conn','count','CLOSE_WAIT Connections','system',current_timestamp,'CLOSE_WAIT conns','Unix::TCPConnectionsByState');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'fin_wait_conn','count','FIN_WAIT Connections','system',current_timestamp,'FIN_WAIT conns','Unix::TCPConnectionsByState');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'established_conn','count','ESTABLISHED Connections','system',current_timestamp,'ESTABLISHED conns','Unix::TCPConnectionsByState');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'syn_rcvd_conn','count','SYN_RCVD Connections','system',current_timestamp,'SYN_RCVD conns','Unix::TCPConnectionsByState');
insert into rhn_metrics(metric_id,storage_unit_id,description,last_update_user,last_update_date,label,command_class)     values ( 'query_time','millisecs','Query Time','system',current_timestamp,'Query time','Unix::Dig');

commit;

