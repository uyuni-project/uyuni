require 'rubygems'
require 'dbi'
require 'oci8'
require 'test/unit'
require File.dirname(__FILE__) + '/config'

class TestDbiCLob < Test::Unit::TestCase

  def setup
    @dbh = get_dbi_connection()
  end

  def test_insert
    filename = File.basename($lobfile)
    @dbh.do("DELETE FROM test_clob WHERE filename = :1", filename)

    # insert an empty clob and get the rowid.
    rowid = @dbh.execute("INSERT INTO test_clob(filename, content) VALUES (:1, EMPTY_CLOB())", filename) do |sth|
      sth.func(:rowid)
    end
    lob = @dbh.select_one("SELECT content FROM test_clob WHERE filename = :1 FOR UPDATE", filename)[0]
    begin
      open($lobfile) do |f|
        while f.gets()
          lob.write($_)
        end
      end
    ensure
      lob.close()
    end
  end

  def test_read
    filename = File.basename($lobfile)
    test_insert() # first insert data.
    lob = @dbh.select_one("SELECT content FROM test_clob WHERE filename = :1 FOR UPDATE", filename)[0]
    begin
      open($lobfile) do |f|
        while buf = lob.read($lobreadnum)
          fbuf = f.read(buf.size)
          assert_equal(fbuf, buf)
        end
        assert_equal(nil, buf)
        assert_equal(true, f.eof?)
      end
    ensure
      lob.close()
    end
  end

  def teardown
    @dbh.disconnect
  end
end
