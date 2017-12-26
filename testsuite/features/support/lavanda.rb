require 'twopence'
require 'timeout'

# extend the objects node vms with usefull methods needed for testsuite.
# All function added here, will be avaible like $server.run
#  or $minion.run_until_ok etc.
module LavandaBasic
  # init the hotnames, only one time
  def init_hostname(hostname)
    @in_hostname = hostname.strip
  end

  def init_full_hostname(fqn)
    @in_full_hostname = fqn.strip
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

  # monkeypatch the run
  def run(cmd, fatal = true, timeout = DEFAULT_TIMEOUT, user = 'root')
    out, _lo, _rem, code = test_and_store_results_together(cmd, user, timeout)
    if fatal
      raise "FAIL: #{cmd} returned #{code}. output : #{out}" if code != 0
    end
    [out, code]
  end

  def run_until_ok(cmd)
    result = nil
    Timeout.timeout(DEFAULT_TIMEOUT) do
      loop do
        result, code = run(cmd, false)
        break if code.zero?
        sleep 2
      end
    end
  rescue Timeout::Error
    raise "timeout finished! something went wrong! \n #{result}"
  end

  def run_until_fail(cmd)
    result = nil
    Timeout.timeout(DEFAULT_TIMEOUT) do
      loop do
        result, code = run(cmd, false)
        break if code.nonzero?
        sleep 2
      end
    end
  rescue Timeout::Error
    raise "timeout finished! something went wrong! \n #{result}"
  end
end
