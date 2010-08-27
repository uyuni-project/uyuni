require 'rubygems'
require 'oci8'
require 'test/unit'
require File.dirname(__FILE__) + '/config'

class TestEncoding < Test::Unit::TestCase
  def setup
    @conn = get_oci8_connection
  end

  def test_select
    drop_table('test_table')
    @conn.exec(<<EOS)
CREATE TABLE test_table
  (C CHAR(10),
   V VARCHAR2(10),
   R RAW(10),
   LR LONG RAW,
   CL CLOB,
   NCL NCLOB,
   BL BLOB)
STORAGE (
   INITIAL 4k
   NEXT 4k
   MINEXTENTS 1
   MAXEXTENTS UNLIMITED
   PCTINCREASE 0)
EOS
    ascii_8bit = Encoding.find('ASCII-8BIT')
    @conn.exec(<<EOS)
INSERT INTO test_table VALUES ('abcd', 'abcd', 'abcd', 'abcd', 'abcd', 'abcd', 'abcd')
EOS
    @conn.exec("SELECT * FROM test_table") do |row|
      assert_equal('abcd      ', row[0], 'CHAR(10)')
      assert_equal(OCI8.encoding, row[0].encoding);
      assert_equal('abcd', row[1], 'VARCHAR2(10)')
      assert_equal(OCI8.encoding, row[1].encoding);
      assert_equal("\xab\xcd", row[2], 'RAW(10)')
      assert_equal(ascii_8bit, row[2].encoding);
      assert_equal("\xab\xcd", row[3], 'LONG RAW')
      assert_equal(ascii_8bit, row[3].encoding);
      assert_equal('abcd', (data = row[4].read), 'CLOB')
      assert_equal(OCI8.encoding, data.encoding);
      assert_equal('abcd', (data = row[5].read), 'NCLOB')
      assert_equal(OCI8.encoding, data.encoding);
      assert_equal("\xab\xcd", (data = row[6].read), 'BLOB')
      assert_equal(ascii_8bit, data.encoding);

      if OCI8.encoding.name == "UTF-8"
        utf_8 = "\u00A1\u00A2\u00A3\u00A4\u00A5\u00A6\u00A7\u00A8\u00A9"
        iso_8859_1 = utf_8.encode("ISO-8859-1")
        # CLOB
        lob = row[4]
        lob.rewind
        lob.write(iso_8859_1) # converted to OCI8.encoding(UTF-8)
        lob.rewind
        assert_equal(utf_8, lob.read)
        # NCLOB
        lob = row[5]
        lob.rewind
        lob.write(iso_8859_1) # converted to OCI8.encoding(UTF-8)
        lob.rewind
        assert_equal(utf_8, lob.read)
        # BLOB
        lob = row[6]
        lob.rewind
        lob.write(iso_8859_1) # written without encoding conversion
        lob.rewind
        assert_equal(iso_8859_1.force_encoding('ASCII-8BIT'), lob.read)
      end
    end
    drop_table('test_table')
  end

  if OCI8.encoding.name == "UTF-8"
    def test_bind_string_with_code_conversion
      drop_table('test_table')
      @conn.exec(<<EOS)
CREATE TABLE test_table
  (V VARCHAR2(3000))
STORAGE (
   INITIAL 4k
   NEXT 4k
   MINEXTENTS 1
   MAXEXTENTS UNLIMITED
   PCTINCREASE 0)
EOS
      utf_8 = "\u00A1" * 1500 # 3000 byte
      iso_8859_1 = utf_8.encode("ISO-8859-1") # 1500 byte
      @conn.exec("INSERT INTO test_table VALUES (:1)", iso_8859_1)
      @conn.exec("SELECT * FROM test_table") do |row|
        assert_equal(utf_8, row[0])
      end
      drop_table('test_table')
    end
  end

  def teardown
    @conn.logoff
  end
end
