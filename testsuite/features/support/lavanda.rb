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

  def init_ip(ip)
    @in_ip = ip
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

  def ip
    raise 'empty ip, something wrong' if @in_ip.empty?
    @in_ip
  end

  # run functions
  def run(cmd, fatal = true, timeout = DEFAULT_TIMEOUT, user = 'root')
    out, _lo, _rem, code = test_and_store_results_together(cmd, user, timeout)
    if fatal
      raise "FAIL: #{cmd} returned #{code}. output : #{out}" if code != 0
    end
    [out, code]
  end

  def run_until_ok(cmd)
    result = nil
    repeat_until_timeout(report_result: true) do
      result, code = run(cmd, false)
      break if code.zero?
      sleep 2
      result
    end
  end

  def run_until_fail(cmd)
    result = nil
    repeat_until_timeout(report_result: true) do
      result, code = run(cmd, false)
      break if code.nonzero?
      sleep 2
      result
    end
  end
end
