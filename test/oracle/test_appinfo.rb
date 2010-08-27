require 'rubygems'
require 'oci8'
require 'test/unit'
require File.dirname(__FILE__) + '/config'

class TestAppInfo < Test::Unit::TestCase

  def setup
    @conn = get_oci8_connection
  end

  def test_set_client_identifier
    # set client_id
    client_id = "ruby-oci8:#{Process.pid()}"
    @conn.client_identifier = client_id
    assert_equal(client_id, @conn.select_one("SELECT SYS_CONTEXT('USERENV', 'CLIENT_IDENTIFIER') FROM DUAL")[0]);
    # check the first character
    assert_raise ArgumentError do
      @conn.client_identifier = ':bad_identifier'
    end

    # clear client_id
    @conn.client_identifier = nil
    assert_nil(@conn.select_one("SELECT SYS_CONTEXT('USERENV', 'CLIENT_IDENTIFIER') FROM DUAL")[0]);
  end

  def test_set_module
    # FIXME: check again after upgrading Oracle 9.2 to 9.2.0.4.
    return if @conn.oracle_server_version < OCI8::ORAVER_10_1

    # set module
    @conn.module = 'ruby-oci8'
    assert_equal('ruby-oci8', @conn.select_one("SELECT SYS_CONTEXT('USERENV', 'MODULE') FROM DUAL")[0]);
    # clear module
    @conn.module = nil
    assert_nil(@conn.select_one("SELECT SYS_CONTEXT('USERENV', 'MODULE') FROM DUAL")[0]);
  end

  def test_set_action
    # FIXME: check again after upgrading Oracle 9.2 to 9.2.0.4.
    return if @conn.oracle_server_version < OCI8::ORAVER_10_1

    # set action
    @conn.action = 'test_set_action'
    assert_equal('test_set_action', @conn.select_one("SELECT SYS_CONTEXT('USERENV', 'ACTION') FROM DUAL")[0]);
    # clear action
    @conn.action = nil
    assert_nil(@conn.select_one("SELECT SYS_CONTEXT('USERENV', 'ACTION') FROM DUAL")[0]);
  end

  def test_set_client_info
    # set client_info
    client_info = "ruby-oci8:#{Process.pid()}"
    @conn.client_info = client_info
    assert_equal(client_info, @conn.select_one("SELECT SYS_CONTEXT('USERENV', 'CLIENT_INFO') FROM DUAL")[0]);
    # clear client_info
    @conn.client_info = nil
    assert_nil(@conn.select_one("SELECT SYS_CONTEXT('USERENV', 'CLIENT_INFO') FROM DUAL")[0]);
  end

  def teardown
    @conn.logoff
  end
end
