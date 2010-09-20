require File.join(File.dirname(__FILE__), "..", "..", "lib", "spacewalk_testsuite_base", "database")

class TestUserGroupType < Test::Unit::TestCase
  include DatabaseLowLevelAccess
  def setup
    @conn = get_oci8_connection
  end

  def teardown
    @conn.logoff
  end

  def test_rename
    ugt = @conn.exec("SELECT * from RHNUSERGROUPTYPE")
    assert ugt
    while rv = ugt.fetch
      assert rv
      puts "#{rv[1]}  #{rv[2]}"
    end
  end

end # TestUserGroupType
