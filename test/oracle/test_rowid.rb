require 'rubygems'
require 'oci8'
require 'test/unit'
require File.dirname(__FILE__) + '/config'

class TestRowid < Test::Unit::TestCase

  def setup
    @conn = get_oci8_connection
  end

  def test_rowid
    drop_table('test_table')
    sql = <<-EOS
CREATE TABLE test_table (N NUMBER(38))
EOS
    @conn.exec(sql)
    cursor = @conn.parse("INSERT INTO test_table values(1)");
    cursor.exec
    rid1 = cursor.rowid
    assert_instance_of(String, rid1)
    cursor.close
    rid2 = nil
    @conn.exec('select rowid from test_table where rowid = :1', rid1) do |row|
      rid2 = row[0]
    end
    assert_equal(rid2, rid1)
    drop_table('test_table')
  end

  def teardown
    @conn.logoff
  end
end
