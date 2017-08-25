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
end
