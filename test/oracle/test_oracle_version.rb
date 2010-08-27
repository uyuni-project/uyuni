require 'rubygems'
require 'oci8'
require 'test/unit'
require File.dirname(__FILE__) + '/config'

class TestOracleVersion < Test::Unit::TestCase

  def test_init
    oraver = OCI8::OracleVersion.new('8.1.6.2.3')
    assert_equal(8, oraver.major)
    assert_equal(1, oraver.minor)
    assert_equal(6, oraver.update)
    assert_equal(2, oraver.patch)
    assert_equal(3, oraver.port_update)

    oraver = OCI8::OracleVersion.new('8.1.6')
    assert_equal(8, oraver.major)
    assert_equal(1, oraver.minor)
    assert_equal(6, oraver.update)
    assert_equal(0, oraver.patch)
    assert_equal(0, oraver.port_update)

    oraver = OCI8::OracleVersion.new('10')
    assert_equal(10, oraver.major)
    assert_equal(0, oraver.minor)
    assert_equal(0, oraver.update)
    assert_equal(0, oraver.patch)
    assert_equal(0, oraver.port_update)

    oraver = OCI8::OracleVersion.new(0x08106203)
    assert_equal(8, oraver.major)
    assert_equal(1, oraver.minor)
    assert_equal(6, oraver.update)
    assert_equal(2, oraver.patch)
    assert_equal(3, oraver.port_update)
  end

  def test_compare
    oraver = OCI8::OracleVersion.new('8.1.6.2.3')
    assert_operator(oraver, :==, OCI8::OracleVersion.new('8.1.6.2.3'))
    assert_operator(oraver, :<, OCI8::OracleVersion.new('9.1.6.2.3'))
    assert_operator(oraver, :<, OCI8::OracleVersion.new('8.2.6.2.3'))
    assert_operator(oraver, :<, OCI8::OracleVersion.new('8.1.7.2.3'))
    assert_operator(oraver, :<, OCI8::OracleVersion.new('8.1.6.3.3'))
    assert_operator(oraver, :<, OCI8::OracleVersion.new('8.1.6.2.4'))
    assert_operator(oraver, :>, OCI8::OracleVersion.new('7.1.6.2.3'))
    assert_operator(oraver, :>, OCI8::OracleVersion.new('8.0.6.2.3'))
    assert_operator(oraver, :>, OCI8::OracleVersion.new('8.1.5.2.3'))
    assert_operator(oraver, :>, OCI8::OracleVersion.new('8.1.6.1.3'))
    assert_operator(oraver, :>, OCI8::OracleVersion.new('8.1.6.2.2'))
  end

  def test_to_s
    oraver = OCI8::OracleVersion.new('8.1.6.2.3')
    assert_equal('8.1.6.2.3', oraver.to_s)
  end

  def test_to_i
    oraver = OCI8::OracleVersion.new('8.1.6.2.3')
    assert_equal(0x08106203, oraver.to_i)
  end

  def test_eql
    oraver = OCI8::OracleVersion.new('8.1.6.2.3')
    assert_equal(true, oraver.eql?(OCI8::OracleVersion.new('8.1.6.2.3')))
    assert_equal(false, oraver.eql?(OCI8::OracleVersion.new('8.2.6.2.3')))
    assert_equal(false, oraver.eql?('8.1.6.2.3'))
  end
end

Test::Unit::AutoRunner.run() if $0 == __FILE__
