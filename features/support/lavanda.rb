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

  # getter functions, executed on testsuite
  def hostname
    raise 'empty hostname, something wrong' if @in_hostname.empty?
    @in_hostname
  end

  def full_hostname
    raise 'empty hostname, something wrong' if @in_full_hostname.empty?
    @in_full_hostname
  end

  # monkeypatch the run
  def run(cmd, fatal = true, timeout = DEFAULT_TIMEOUT, user = 'root')
    out, _lo, _rem, code = test_and_store_results_together(cmd, user, timeout)
    if fatal
      raise "FAIL: #{cmd} returned #{code}. output : #{out}" if code != 0
    end
    [out, code]
  end

  def run_until_ok(cmd, ok_msg = nil)
    Timeout.timeout(DEFAULT_TIMEOUT) do
      loop do
        _out, code = run(cmd, false)
        if code.zero?
          puts ok_msg unless ok_msg.nil?
          break
        end
        sleep 2
      end
    end
  rescue Timeout::Error
    raise 'timeout finished! something went wrong'
  end

  def run_until_fail(cmd, nok_msg = nil)
    Timeout.timeout(DEFAULT_TIMEOUT) do
      loop do
        _out, code = run(cmd, false)
        if code.nonzero?
          puts nok_msg unless nok_msg.nil?
          break
        end
        sleep 2
      end
    end
  rescue Timeout::Error
    raise 'timeout finished! something went wrong'
  end
end
