require 'rubygems'
require 'oci8'
require 'test/unit'
require File.dirname(__FILE__) + '/config'

class TestConnStr < Test::Unit::TestCase
  TEST_CASES =
    [
     # success cases:
     #  [ 'connect_string', expected result as an array]
     # error cases:
     #  [ 'connect_string', ExceptionClass]
     ["hr/hr@host/service_name", ["hr", "hr", "host/service_name", nil]],
     ["sys/syspw@host/service_name   AS    SYSdba ", ["sys", "syspw", "host/service_name", :SYSDBA]],
     ["sys/syspw@host:1521/service_name as sysdba", ["sys", "syspw", "host:1521/service_name", :SYSDBA]],
     # error cases
     ["service_name", ArgumentError],
     ["", ArgumentError],
     ["foo bar/baz", ArgumentError],
     ["foo@bar/baz", ArgumentError],
     # raise error in connecting but no error in parse_connect_string.
     ["foo/bar as sysdbaaa", ["foo", "bar", nil, "sysdbaaa"]],

     ##
     ## following test cases are contributed by Shiwei Zhang.
     ##
     #"username/password"
     ["username/password", ["username", "password", nil, nil]],
     #"username/password@[//]host[:port][/service_name]"
     ["username/password@host", ["username", "password", "host", nil]],
     ["username/password@host/service_name", ["username", "password", "host/service_name", nil]],
     ["username/password@host:1521", ["username", "password", "host:1521", nil]],
     ["username/password@host:1521/service_name", ["username", "password", "host:1521/service_name", nil]],
     ["username/password@//host", ["username", "password", "//host", nil]],
     ["username/password@//host/service_name", ["username", "password", "//host/service_name", nil]],
     ["username/password@//host:1521", ["username", "password", "//host:1521", nil]],
     ["username/password@//host:1521/service_name", ["username", "password", "//host:1521/service_name", nil]],
     #"username/password as{sysoper|sysdba}"
     ["username/password as sysoper", ["username", "password", nil, :SYSOPER]],
     ["username/password as sysdba", ["username", "password", nil, :SYSDBA]],
     #"username/password@[//]host[:port][/service_name] as {sysoper|sysdba}"
     ["username/password@host as sysoper", ["username", "password", "host", :SYSOPER]],
     ["username/password@host as sysdba", ["username", "password", "host", :SYSDBA]],
     ["username/password@host/service_name as sysoper", ["username", "password", "host/service_name", :SYSOPER]],
     ["username/password@host/service_name as sysdba", ["username", "password", "host/service_name", :SYSDBA]],
     ["username/password@host:1521 as sysoper", ["username", "password", "host:1521", :SYSOPER]],
     ["username/password@host:1521 as sysdba", ["username", "password", "host:1521", :SYSDBA]],
     ["username/password@host:1521/service_name as sysoper", ["username", "password", "host:1521/service_name", :SYSOPER]],
     ["username/password@host:1521/service_name as sysdba", ["username", "password", "host:1521/service_name", :SYSDBA]],
     ["username/password@//host as sysoper", ["username", "password", "//host", :SYSOPER]],
     ["username/password@//host as sysdba", ["username", "password", "//host", :SYSDBA]],
     ["username/password@//host/service_name as sysoper", ["username", "password", "//host/service_name", :SYSOPER]],
     ["username/password@//host/service_name as sysdba", ["username", "password", "//host/service_name", :SYSDBA]],
     ["username/password@//host:1521 as sysoper", ["username", "password", "//host:1521", :SYSOPER]],
     ["username/password@//host:1521 as sysdba", ["username", "password", "//host:1521", :SYSDBA]],
     ["username/password@//host:1521/service_name as sysoper", ["username", "password", "//host:1521/service_name", :SYSOPER]],
     ["username/password@//host:1521/service_name as sysdba", ["username", "password", "//host:1521/service_name", :SYSDBA]],
     ["/passwd@192.168.19.19:1521/orcl as sysdba", ["", "passwd", "192.168.19.19:1521/orcl", :SYSDBA]],
     ["/", [nil, nil, nil, nil]],
     ["/ as sysdba", [nil, nil, nil, :SYSDBA]],
    ]

  def test_connstr
    obj = OCI8.allocate # create an uninitialized object.
    TEST_CASES.each do |test_case|
      case test_case[1]
      when Array
        # use instance_eval to call a private method parse_connect_string.
        result = obj.instance_eval { parse_connect_string(test_case[0]) }
        assert_equal(test_case[1], result, test_case[0])
      when Class
        assert_raises(test_case[1]) do
          result = obj.instance_eval { parse_connect_string(test_case[0]) }
        end
      else
        raise "unsupported testcase"
      end
    end
  end
end

Test::Unit::AutoRunner.run() if $0 == __FILE__
