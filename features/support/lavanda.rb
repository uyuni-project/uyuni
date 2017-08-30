require 'twopence'

# this module monkeypatch some basic commands from twopence
module LavandaBasic
  def run(cmd, fatal=true, timeout=200, user='root')
    out, _lo, _rem, code = test_and_store_results_together(cmd, user, timeout)
    if fatal
      raise "FAIL: #{cmd} returned #{code}. output : #{out}" if code != 0
    end
    [out, code]
  end

  def run_until_ok(cmd, ok_msg=nil)
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

  def run_until_fail(cmd, nok_msg=nil)
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
