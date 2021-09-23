# Copyright (c) 2016-2021 SUSE LLC.
# Licensed under the terms of the MIT license.
require 'twopence'
require 'timeout'

# Extend the objects node VMs with useful methods needed for testsuite.
# All function added here will be available like $server.run
#  or $minion.run_until_ok etc.
module LavandaBasic
  extend self

  # Hostname
  def init_hostname(hostname)
    @in_hostname = hostname.strip
  end

  # Fully qualified domain name
  def init_full_hostname(fqdn)
    @in_full_hostname = fqdn.strip
  end

  # IP (It actually contains the hostname)
  def init_ip(ip)
    @in_ip = ip
  end

  # Public IP
  def init_public_ip(public_ip)
    @in_public_ip = public_ip
  end

  # Hostname getter
  def hostname
    raise(ScriptError, 'empty hostname, something wrong') if @in_hostname.empty?

    @in_hostname
  end

  # FQDN getter
  def full_hostname
    raise(ScriptError, 'empty hostname, something wrong') if @in_full_hostname.empty?

    @in_full_hostname
  end

  # IP getter
  def ip
    raise(ScriptError, 'empty ip, something wrong') if @in_ip.empty?

    @in_ip
  end

  # Public IP getter
  def public_ip
    raise(ScriptError, 'empty public_ip, something wrong') if @in_public_ip.empty?

    @in_public_ip
  end

  # run functions
  def run(cmd, separated_results: false, check_errors: true, timeout: DEFAULT_TIMEOUT, user: 'root', successcodes: [0], buffer_size: 65_536)
    if separated_results
      out, err, _lo, _rem, code = test_and_store_results_separately(cmd, user, timeout, buffer_size)
    else
      out, _lo, _rem, code = test_and_store_results_together(cmd, user, timeout, buffer_size)
    end
    raise(ScriptError, "FAIL: #{cmd} returned #{code}. output : #{out}") if check_errors && !successcodes.include?(code)

    if separated_results
      [out, err, code]
    else
      [out, code]
    end
  end

  # Run a command remotely until the result code pass or a timeout raise
  def run_until_ok(cmd)
    result = nil
    repeat_until_timeout(report_result: true) do
      result, code = run(cmd, check_errors: false)
      break if code.zero?

      sleep(2)
      result
    end
  end

  # Run a command remotely until the result code fails or a timeout raise
  def run_until_fail(cmd)
    result = nil
    repeat_until_timeout(report_result: true) do
      result, code = run(cmd, check_errors: false)
      break if code.nonzero?

      sleep(2)
      result
    end
  end

  # Wait while a process is running or until a timeout raise
  def wait_while_process_running(process)
    result = nil
    repeat_until_timeout(report_result: true) do
      result, code = run("pgrep -x #{process} >/dev/null", check_errors: false)
      break if code.nonzero?

      sleep(2)
      result
    end
  end
end
