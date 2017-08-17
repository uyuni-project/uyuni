require 'twopence'
require 'timeout'

# extend the objects node vms with usefull methods needed for testsuite.
# All function added here, will be avaible like $server.run
#  or $minion.run_and_wait etc.
module LavandaBasic
  def hostname
    hostname, _local, _remote, code = test_and_store_results_together('hostname', 'root', 500)
    raise 'cannot get hostname for node' if code.nonzero?
    hostname
  end

  def full_hostname
    full_hostname, _local, _remote, code = test_and_store_results_together('hostname -f', 'root', 500)
    raise 'no full qualified hostname for node' if code.nonzero?
    full_hostname
  end

  # monkeypatch the run
  def run(cmd, fatal = true, timeout = 200, user = 'root')
    out, _lo, _rem, code = test_and_store_results_together(cmd, user, timeout)
    if fatal
      raise "FAIL: #{cmd} returned #{code}. output : #{out}" if code != 0
    end
    [out, code]
  end

  def run_and_wait(cmd, ok_msg = nil)
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
end
