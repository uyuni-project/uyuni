# Copyright (c) 2017 Suse Linux
# Licensed under the terms of the MIT license.

require "xmlrpc/client"
require 'time'
require 'date'

def retrieve_server_id(server)
  sysrpc = XMLRPCSystemTest.new(ENV['TESTHOST'])
  sysrpc.login('admin', 'admin')
  systems = sysrpc.listSystems
  refute_nil(systems)
  server_id = systems
              .select { |s| s['name'] == server }
              .map { |s| s['id'] }.first
  refute_nil(server_id, "client #{server} is not yet registered?")
  server_id
end

def waitActionComplete(actionid)
  host = $server_fullhostname
  @cli = XMLRPC::Client.new2('http://' + host + '/rpc/api')
  @sid = @cli.call('auth.login', 'admin', 'admin')
  time_out = 300
  Timeout.timeout(time_out) do
    loop do
      list = @cli.call('schedule.listCompletedActions', @sid)
      list.each do |action|
        return true if action['id'] == actionid
        sleep(2)
      end
    end
  end
rescue Timeout::Error
  raise "ERR: Action did not completed! Not found on completed actions"
end

# centos7_tradclient tests
And(/^execute some tests for centos_trad_client$/) do
  host = $server_fullhostname
  @cli = XMLRPC::Client.new2('http://' + host + '/rpc/api')
  @sid = @cli.call('auth.login', 'admin', 'admin')
  # -------------------------------
  # --1) package refresh--
  @centosid = retrieve_server_id($ceos_minion_fullhostname)
  now = DateTime.now
  date_schedule_now = XMLRPC::DateTime.new(now.year, now.month, now.day, now.hour, now.min, now.sec)
  @cli.call('system.schedulePackageRefresh', @sid, @centosid, date_schedule_now)
  # --2) run schedule script --
  script = "#! /usr/bin/bash \n uptime && ls"
  id_script = @cli.call('system.scheduleScriptRun', @sid, @centosid, 'root', 'root', 500, script, date_schedule_now)
  $ceos_minion.run("rhn_check -vvv", true, 500, 'root')
  waitActionComplete(id_script)
  # --3)  schedule reboot
  @cli.call('system.scheduleReboot', @sid, @centosid, date_schedule_now)
  $ceos_minion.run("rhn_check -vvv", true, 500, 'root')
  timeout = 400
  checkShutdown($ceos_minion_fullhostname, timeout)
  checkRestart($ceos_minion_fullhostname, $ceos_minion, timeout)
  assert_empty(@cli.call('schedule.listFailedActions', @sid))
  @cli.call("auth.logout", @sid)
end
