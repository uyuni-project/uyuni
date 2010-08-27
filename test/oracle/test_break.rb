# High-level API
require 'rubygems'
require 'oci8'
require 'test/unit'
require File.dirname(__FILE__) + '/config'

class TestBreak < Test::Unit::TestCase

  def setup
    @conn = get_oci8_connection
  end

  def teardown
    @conn.logoff
  end

  def report(str)
    printf "%d: %s\n", (Time.now - $start_time), str
  end
  
  PLSQL_DONE = 1
  OCIBREAK = 2
  SEND_BREAK = 3

  TIME_IN_PLSQL = 5
  TIME_TO_BREAK = 2
  MARGIN = 0.1

  def do_test_ocibreak(conn, expect)
    $start_time = Time.now

    th = Thread.start do 
      begin
	conn.exec("BEGIN DBMS_LOCK.SLEEP(#{TIME_IN_PLSQL}); END;")
	assert_equal(expect[PLSQL_DONE], (Time.now - $start_time + MARGIN).to_i, 'PLSQL_DONE')
      rescue OCIBreak
	assert_equal(expect[OCIBREAK], (Time.now - $start_time + MARGIN).to_i, 'OCIBREAK')
      end
    end

    sleep(0.01) # make a time to run DBMS_LOCK.SLEEP
    sleep(TIME_TO_BREAK)
    assert_equal(expect[SEND_BREAK], (Time.now - $start_time + MARGIN).to_i, 'SEND_BREAK')
    conn.break()
    th.join
  end

  def test_blocking_mode
    @conn.non_blocking = false
    assert_equal(false, @conn.non_blocking?)
    expect = []
    expect[PLSQL_DONE] = TIME_IN_PLSQL
    expect[OCIBREAK]   = "Invalid status"
    expect[SEND_BREAK] = TIME_IN_PLSQL + TIME_TO_BREAK
    do_test_ocibreak(@conn, expect)
  end

  def test_non_blocking_mode
    is_windows_server = false
    @conn.exec('select banner from v$version') do |row|
      is_windows_server = true if row[0].include? 'Windows'
    end

    @conn.non_blocking = true
    assert_equal(true, @conn.non_blocking?)
    expect = []
    if is_windows_server
      if $oracle_server_version >= OCI8::ORAVER_9_0
        # raise after sleeping #{TIME_IN_PLSQL} seconds.
        expect[PLSQL_DONE] = "Invalid status"
        expect[OCIBREAK] = TIME_IN_PLSQL
      else
        expect[PLSQL_DONE] = TIME_IN_PLSQL
        expect[OCIBREAK] = "Invalid status"
      end
    else
      # raise immediately by OCI8#break.
      expect[PLSQL_DONE] = "Invalid status"
      expect[OCIBREAK] = TIME_TO_BREAK
    end
    expect[SEND_BREAK]   = TIME_TO_BREAK
    do_test_ocibreak(@conn, expect)
  end
end
