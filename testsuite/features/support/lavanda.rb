# Copyright (c) 2016-2022 SUSE LLC.
# Licensed under the terms of the MIT license.

require 'twopence'
require 'timeout'

# Extend the objects node VMs with useful methods needed for testsuite.
# All function added here will be available like $server.run
#  or $minion.run_until_ok etc.
module LavandaBasic
  # init the hostnames, only one time
  def init_hostname(hostname)
    @in_hostname = hostname.strip
  end

  def init_full_hostname(fqdn)
    @in_full_hostname = fqdn.strip
  end

  def init_private_ip(private_ip)
    @in_private_ip = private_ip
  end

  def init_public_ip(public_ip)
    @in_public_ip = public_ip
  end

  def init_private_interface(private_interface)
    @in_private_interface = private_interface
  end

  def init_public_interface(public_interface)
    @in_public_interface = public_interface
  end

  # getter functions, executed on testsuite
  def hostname
    raise 'empty hostname, something wrong' if @in_hostname.empty?
    @in_hostname
  end

  def full_hostname
    raise 'empty hostname, something wrong' if @in_full_hostname.empty?
    @in_full_hostname
  end

  def private_ip
    raise 'empty private_ip, something wrong' if @in_private_ip.empty?
    @in_private_ip
  end

  def public_ip
    raise 'empty public_ip, something wrong' if @in_public_ip.empty?
    @in_public_ip
  end

  def private_interface
    raise 'empty private_interface, something wrong' if @in_private_interface.empty?
    @in_private_interface
  end

  def public_interface
    raise 'empty public_interface, something wrong' if @in_public_interface.empty?
    @in_public_interface
  end

  # run functions
  def run(cmd, separated_results: false, check_errors: true, timeout: DEFAULT_TIMEOUT, user: 'root', successcodes: [0], buffer_size: 65536)
    if separated_results
      out, err, _lo, _rem, code = test_and_store_results_separately(cmd, user, timeout, buffer_size)
    else
      out, _lo, _rem, code = test_and_store_results_together(cmd, user, timeout, buffer_size)
    end
    if check_errors
      raise "FAIL: #{cmd} returned #{code}. output : #{out}" unless successcodes.include?(code)
    end
    if separated_results
      [out, err, code]
    else
      [out, code]
    end
  end

  def run_until_ok(cmd)
    result = nil
    repeat_until_timeout(report_result: true) do
      result, code = run(cmd, check_errors: false)
      break if code.zero?
      sleep 2
      result
    end
  end

  def run_until_fail(cmd)
    result = nil
    repeat_until_timeout(report_result: true) do
      result, code = run(cmd, check_errors: false)
      break if code.nonzero?
      sleep 2
      result
    end
  end

  def wait_while_process_running(process)
    result = nil
    repeat_until_timeout(report_result: true) do
      result, code = run("pgrep -x #{process} >/dev/null", check_errors: false)
      break if code.nonzero?
      sleep 2
      result
    end
  end
end
