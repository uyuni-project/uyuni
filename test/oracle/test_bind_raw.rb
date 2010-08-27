# Low-level API
require 'rubygems'
require 'oci8'
require 'test/unit'
require File.dirname(__FILE__) + '/config'

class TestBindRaw < Test::Unit::TestCase
  CHECK_TARGET = [
    ["0123456789:;<=>?", "303132333435363738393A3B3C3D3E3F"],
    ["@ABCDEFGHIJKLMNO", "404142434445464748494A4B4C4D4E4F"],
    ["PQRSTUVWXYZ[\\]^_", "505152535455565758595A5B5C5D5E5F"],
    ["`abcdefghijklmno", "606162636465666768696A6B6C6D6E6F"],
    ["pqrstuvwxyz{|}~", "707172737475767778797A7B7C7D7E"],
  ]

  def setup
    @conn = get_oci8_connection()
  end

  def test_set_raw
    cursor = @conn.parse("BEGIN :hex := RAWTOHEX(:raw); END;")
    cursor.bind_param(:raw, nil, OCI8::RAW, 16)
    cursor.bind_param(:hex, nil, String, 32)

    CHECK_TARGET.each do |raw, hex|
      cursor[:raw] = raw
      cursor.exec
      assert_equal(hex, cursor[:hex])
    end
  end

  def test_get_raw
    cursor = @conn.parse("BEGIN :raw := HEXTORAW(:hex); END;")
    cursor.bind_param(:hex, nil, String, 32)
    cursor.bind_param(:raw, nil, OCI8::RAW, 16)

    CHECK_TARGET.each do |raw, hex|
      cursor[:hex] = hex
      cursor.exec
      assert_equal(raw, cursor[:raw])
    end
  end

  def teardown
    @conn.logoff
  end
end
