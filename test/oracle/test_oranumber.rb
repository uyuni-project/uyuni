# Low-level API
require 'rubygems'
require 'oci8'
require 'test/unit'
require File.dirname(__FILE__) + '/config'
require 'yaml'
require 'bigdecimal'
require 'rational'

class TestOraNumber < Test::Unit::TestCase

  LARGE_RANGE_VALUES = [
    "12345678901234567890123456789012345678",
    "1234567890123456789012345678901234567",
    "1234567890123456789012345678901234567.8",
    "12.345678901234567890123456789012345678",
    "1.2345678901234567890123456789012345678",
    "1.234567890123456789012345678901234567",
    "0.0000000000000000000000000000000000001",
    "0.000000000000000000000000000000000001",
    "0",
    "2147483647", # max of 32 bit signed value
    "2147483648", # max of 32 bit signed value + 1
    "-2147483648", # min of 32 bit signed value
    "-2147483649", # min of 32 bit signed value - 1
    "9223372036854775807",  # max of 64 bit signed value
    "9223372036854775808",  # max of 64 bit signed value + 1
    "-9223372036854775808",  # min of 64 bit signed value
    "-9223372036854775809",  # min of 64 bit signed value - 1
    "-12345678901234567890123456789012345678",
    "-1234567890123456789012345678901234567",
    "-123456789012345678901234567890123456",
    "-1234567890123456789012345678901234567.8",
    "-12.345678901234567890123456789012345678",
    "-1.2345678901234567890123456789012345678",
    "-0.0000000000000000000000000000000000001",
    "-0.000000000000000000000000000000000001",
    "-0.00000000000000000000000000000000001",
    "1",
    "20",
    "300",
    "-1",
    "-20",
    "-300",
    "1.123",
    "12.123",
    "123.123",
    "1.1",
    "1.12",
    "1.123",
    "-1.123",
    "-12.123",
    "-123.123",
    "-1.1",
    "-1.12",
    "-1.123",
  ]

  SMALL_RANGE_VALUES = [
    "10",
    "3",
    "3.14159265358979323846", # PI
    "2",
    "1.57079632679489661923", # PI/2
    "0.5",
    "0.0000000001",
    "0",
    "-0.0000000001",
    "-0.5",
    "-1.57079632679489661923", # -PI/2
    "-2",
    "-3.14159265358979323846", # -PI
    "-3",
    "-10",
  ]

  def compare_with_float(values, rettype, proc1, proc2 = nil)
    proc2 = proc1 if proc2.nil?
    values.each do |x|
      expected_val = proc1.call(x.to_f)
      actual_val = proc2.call(OraNumber.new(x))
      assert_kind_of(rettype, actual_val)
      delta = [expected_val.abs * 1.0e-12, 1.0e-14].max
      assert_in_delta(expected_val, actual_val, delta, x)
    end
  end

  def compare_with_float2(values, proc_args, proc1, proc2 = nil)
    proc2 = proc1 if proc2.nil?
    values.each do |x|
      proc_args.each do |y|
        expected_val = proc1.call(x.to_f, y)
        actual_val = proc2.call(OraNumber.new(x), y)
        begin
          delta = [expected_val.abs * 1.0e-12, 1.0e-14].max
        rescue
          puts '-----------'
          p x
          p y
          p expected_val
          puts '-----------'
          raise $!
        end
        # explicity convert actual_val to a Float to prevent
        # SEGV in OCINumberSub() if the Oracle client vesion
        # is less than 10.2.0.4.
        if defined? ::MiniTest and OCI8.oracle_client_version < OCI8::OracleVersion.new('10.2.0.4')
          actual_val = actual_val.to_f
        end
        assert_in_delta(expected_val, actual_val, delta, x)
      end
    end
  end

  def test_in_bind
    conn = get_oci8_connection
    begin
      conn.exec("alter session set nls_numeric_characters = '.,'")
      cursor = conn.parse("BEGIN :out := TO_CHAR(:in); END;")
      cursor.bind_param(:out, nil, String, 40)
      cursor.bind_param(:in, OraNumber)
      LARGE_RANGE_VALUES.each do |val|
        cursor[:in] = OraNumber.new(val)
        cursor.exec
        # convert 0.0001 and -0.0001 to .0001 and -.0001 respectively
        val = $1+'.'+$2 if /(-?)0\.(.*)/ =~ val
        assert_equal(val, cursor[:out])
      end
    ensure
      conn.logoff
    end
  end

  def test_out_bind
    conn = get_oci8_connection
    begin
      conn.exec("alter session set nls_numeric_characters = '.,'")
      cursor = conn.parse("BEGIN :out := TO_NUMBER(:in); END;")
      cursor.bind_param(:out, OraNumber)
      cursor.bind_param(:in, nil, String, 40)
      LARGE_RANGE_VALUES.each do |val|
        cursor[:in] = val
        cursor.exec
        assert_equal(OraNumber.new(val), cursor[:out])
      end
    ensure
      conn.logoff
    end
  end

  def test_dup
    LARGE_RANGE_VALUES.each do |x|
      n = OraNumber.new(x)
      assert_equal(n, n.dup)
      assert_equal(n, n.clone)
    end
  end

  def test_marshal
    LARGE_RANGE_VALUES.each do |x|
      n = OraNumber.new(x)
      assert_equal(n, Marshal.load(Marshal.dump(n)))
    end
  end

  def test_yaml
    LARGE_RANGE_VALUES.each do |x|
      n = OraNumber.new(x)
      assert_equal(n, YAML.load(YAML.dump(n)))
    end
  end

  # OCI8::Math.acos(x) -> ocinumber
  def test_math_acos
    test_values = []
    -1.0.step(1.0, 0.01) do |n|
      test_values << n
    end
    compare_with_float(test_values, OraNumber,
                       Proc.new {|n| Math::acos(n)},
                       Proc.new {|n| OCI8::Math::acos(n)})
  end

  # OCI8::Math.asin(x) -> ocinumber
  def test_math_asin
    test_values = []
    -1.0.step(1.0, 0.01) do |n|
      test_values << n
    end
    compare_with_float(test_values, OraNumber,
                       Proc.new {|n| Math::asin(n)},
                       Proc.new {|n| OCI8::Math::asin(n)})
  end

  # OCI8::Math.atan(x) -> ocinumber
  def test_math_atan
    compare_with_float(SMALL_RANGE_VALUES, OraNumber,
                       Proc.new {|n| Math::atan(n)},
                       Proc.new {|n| OCI8::Math::atan(n)})
  end

  # OCI8::Math.atan2(y, x) -> ocinumber
  def test_math_atan2
    compare_with_float2(SMALL_RANGE_VALUES, SMALL_RANGE_VALUES,
                        Proc.new {|x, y| Math::atan2(x, y.to_f)},
                        Proc.new {|x, y| OCI8::Math::atan2(x, y.to_f)})
    compare_with_float2(SMALL_RANGE_VALUES, SMALL_RANGE_VALUES,
                        Proc.new {|x, y| Math::atan2(y.to_f, x)},
                        Proc.new {|x, y| OCI8::Math::atan2(y.to_f, x)})
  end

  # OCI8::Math.cos(x) -> ocinumber
  def test_math_cos
    compare_with_float(SMALL_RANGE_VALUES, OraNumber,
                       Proc.new {|n| Math::cos(n)},
                       Proc.new {|n| OCI8::Math::cos(n)})
  end

  # OCI8::Math.cosh(x) -> ocinumber
  def test_math_cosh
    compare_with_float(SMALL_RANGE_VALUES, OraNumber,
                       Proc.new {|n| Math::cosh(n)},
                       Proc.new {|n| OCI8::Math::cosh(n)})
  end

  # OCI8::Math.exp(x) -> ocinumber
  def test_exp
    compare_with_float(SMALL_RANGE_VALUES, OraNumber,
                       Proc.new {|n| Math::exp(n)},
                       Proc.new {|n| OCI8::Math::exp(n)})
  end

  # OCI8::Math.log(numeric) -> ocinumber
  # OCI8::Math.log(numeric, base_num) -> ocinumber
  def test_log
    test_values = LARGE_RANGE_VALUES.reject do |x|
      # reject minus and zero values
      x[0,1] == '-' || x == '0'
    end
    compare_with_float(test_values, OraNumber,
                       Proc.new {|n| Math::log(n)},
                       Proc.new {|n| OCI8::Math::log(n)})
    compare_with_float(test_values, OraNumber,
                       Proc.new {|n| Math::log(n)/Math::log(3)},
                       Proc.new {|n| OCI8::Math::log(n, 3)})
  end

  # OCI8::Math.log10(numeric) -> ocinumber
  def test_log10
    test_values = LARGE_RANGE_VALUES.reject do |x|
      # reject minus and zero values
      x[0,1] == '-' || x == '0'
    end
    compare_with_float(test_values, OraNumber,
                       Proc.new {|n| Math::log10(n)},
                       Proc.new {|n| OCI8::Math::log10(n)})
  end

  # OCI8::Math.sin(x) -> ocinumber
  def test_math_sin
    compare_with_float(SMALL_RANGE_VALUES, OraNumber,
                       Proc.new {|n| Math::sin(n)},
                       Proc.new {|n| OCI8::Math::sin(n)})
  end

  # OCI8::Math.sinh(x) -> ocinumber
  def test_math_sinh
    compare_with_float(SMALL_RANGE_VALUES, OraNumber,
                       Proc.new {|n| Math::sinh(n)},
                       Proc.new {|n| OCI8::Math::sinh(n)})
  end

  # OCI8::Math.sqrt(numeric) -> ocinumber
  def test_sqrt
    test_values = LARGE_RANGE_VALUES.reject do |x|
      # reject minus values
      x[0,1] == '-'
    end
    compare_with_float(test_values, OraNumber,
                       Proc.new {|n| Math::sqrt(n)},
                       Proc.new {|n| OCI8::Math::sqrt(n)})
  end

  # OCI8::Math.tan(x) -> ocinumber
  def test_math_tan
    test_values = SMALL_RANGE_VALUES.reject do |x|
      # reject PI/2 and -PI/2.
      # Those values are +inf and -info
      radian = x.to_f
      (radian.abs - Math::PI/2).abs < 0.000001
    end
    compare_with_float(test_values, OraNumber,
                       Proc.new {|n| Math::tan(n)},
                       Proc.new {|n| OCI8::Math::tan(n)})
  end

  # OCI8::Math.tanh() -> ocinumber
  def test_math_tanh
    compare_with_float(SMALL_RANGE_VALUES, OraNumber,
                       Proc.new {|n| Math::tanh(n)},
                       Proc.new {|n| OCI8::Math::tanh(n)})
  end

  # onum % other -> onum
  # def test_mod

  # onum * other -> onum
  def test_mul
    compare_with_float2(SMALL_RANGE_VALUES, SMALL_RANGE_VALUES,
                        Proc.new {|x, y| x * y.to_f})
    compare_with_float2(SMALL_RANGE_VALUES, SMALL_RANGE_VALUES,
                        Proc.new {|x, y| y.to_f * x})
  end

  # onum ** other -> onum
  def test_pow
    base_values = SMALL_RANGE_VALUES.reject do |x|
      # reject minus and zero values
      x[0,1] == '-' || x == '0'
    end
    compare_with_float2(base_values, SMALL_RANGE_VALUES,
                        Proc.new {|x, y| x ** y.to_f})
    compare_with_float2(SMALL_RANGE_VALUES, base_values,
                        Proc.new {|x, y| y.to_f ** x})
  end

  # onum + other -> onum
  def test_add
    compare_with_float2(SMALL_RANGE_VALUES, SMALL_RANGE_VALUES,
                        Proc.new {|x, y| x + y.to_f})
    compare_with_float2(SMALL_RANGE_VALUES, SMALL_RANGE_VALUES,
                        Proc.new {|x, y| y.to_f + x})
  end

  # onum - other -> onum
  def test_minus
    compare_with_float2(SMALL_RANGE_VALUES, SMALL_RANGE_VALUES,
                        Proc.new {|x, y| x - y.to_f})
    compare_with_float2(SMALL_RANGE_VALUES, SMALL_RANGE_VALUES,
                        Proc.new {|x, y| y.to_f - x})
  end

  # -ocinumber -> ocinumber
  def test_uminus
    compare_with_float(LARGE_RANGE_VALUES, OraNumber, Proc.new {|n| -n})
  end

  # onum / other -> onum
  # TODO: test_div

  # onum <=> other -> -1, 0, +1
  def test_cmp
    assert_equal(-1, 1 <=> OraNumber(2))
    assert_equal(-1, 1.0 <=> OraNumber(2))
    assert_equal(-1, BigDecimal("1") <=> OraNumber(2))
    assert_equal(-1, Rational(1) <=> OraNumber(2))
    assert_equal(0, 2 <=> OraNumber(2))
    assert_equal(0, 2.0 <=> OraNumber(2))
    assert_equal(0, BigDecimal("2") <=> OraNumber(2))
    assert_equal(0, Rational(2) <=> OraNumber(2))
    assert_equal(1, 3 <=> OraNumber(2))
    assert_equal(1, 3.0 <=> OraNumber(2))
    assert_equal(1, BigDecimal("3") <=> OraNumber(2))
    assert_equal(1, Rational(3) <=> OraNumber(2))
  end

  # onum.abs -> ocinumber
  def test_abs
    compare_with_float(LARGE_RANGE_VALUES, OraNumber, Proc.new {|n| n.abs})
  end

  # onum.ceil -> integer
  def test_ceil
    compare_with_float(LARGE_RANGE_VALUES, Integer, Proc.new {|n| n.ceil})
  end

  # onum.floor -> integer
  def test_floor
    compare_with_float(LARGE_RANGE_VALUES, Integer, Proc.new {|n| n.floor})
  end

  # onum.round -> integer
  # onum.round(decplace) -> onum
  def test_round
    compare_with_float(LARGE_RANGE_VALUES, Integer, Proc.new {|n| n.round})
    compare_with_float(LARGE_RANGE_VALUES, OraNumber,
                       Proc.new {|n| (n * 10).round * 0.1},
                       Proc.new {|n| n.round(1)})
    compare_with_float(LARGE_RANGE_VALUES, OraNumber,
                       Proc.new {|n| (n * 100).round * 0.01},
                       Proc.new {|n| n.round(2)})
    compare_with_float(LARGE_RANGE_VALUES, OraNumber,
                       Proc.new {|n| (n * 0.1).round * 10},
                       Proc.new {|n| n.round(-1)})
  end

  # onum.round_prec(digits) -> ocinumber
  def test_round_prec
    if OCI8::oracle_client_version >= OCI8::ORAVER_8_1
      # Oracle 8.1 client or upper
      compare_with_float2(LARGE_RANGE_VALUES, [1, 2, 3, 5, 10, 20],
                          Proc.new {|x, y|
                            return 0.0 if x == 0.0
                            factor = 10 ** (Math::log10(x.abs).to_i - y + 1)
                            (x / factor).round * factor
                          },
                          Proc.new {|x, y| x.round_prec(y)})
    else
      # Oracle 8.0 client
      assert_raise NoMethodError do
        OraNumber.new(1).round_prec(1)
      end
    end
  end

  # onum.shift(fixnum) -> ocinumber
  def test_shift
    if OCI8::oracle_client_version >= OCI8::ORAVER_8_1
      # Oracle 8.1 client or upper
      compare_with_float2(LARGE_RANGE_VALUES, [-5, -4, -3, -1, 0, 1, 2, 3, 4, 5],
                          Proc.new {|x, y| x * (10 ** y)},
                          Proc.new {|x, y| x.shift(y)})
    else
      # Oracle 8.0 client
      assert_raise NoMethodError do
        OraNumber.new(1).shift(1)
      end
    end
  end

  # onum.to_char(fmt = nil, nls_params = nil) -> string
  def test_to_char
    onum = OraNumber.new(123.45)
    assert_equal('   123.4500',   onum.to_char('99999.9999'))
    assert_equal('  0123.4500',   onum.to_char('90000.0009'))
    assert_equal(' 00123.4500',   onum.to_char('00000.0000'))
    assert_equal('123.45',        onum.to_char('FM99999.9999'))
    assert_equal('0123.450',      onum.to_char('FM90000.0009'))
    assert_equal('00123.4500',    onum.to_char('FM00000.0000'))
    assert_equal('  -123.4500',(-onum).to_char('99999.9999'))
    assert_equal(' -0123.4500',(-onum).to_char('90000.0009'))
    assert_equal('-00123.4500',(-onum).to_char('00000.0000'))
    assert_equal('-123.45',    (-onum).to_char('FM99999.9999'))
    assert_equal('-0123.450',  (-onum).to_char('FM90000.0009'))
    assert_equal('-00123.4500',(-onum).to_char('FM00000.0000'))
    assert_equal(' 0,123.4500',   onum.to_char('0G000D0000', "NLS_NUMERIC_CHARACTERS = '.,'"))
    assert_equal(' 0.123,4500',   onum.to_char('0G000D0000', "NLS_NUMERIC_CHARACTERS = ',.'"))
    assert_equal('Ducat123.45',    onum.to_char('FML9999.999', "NLS_CURRENCY = 'Ducat'"))
  end

  # onum.to_f -> float
  def test_to_f
    LARGE_RANGE_VALUES.each do |x|
      expected_val = x.to_f
      actual_val = OraNumber.new(x).to_f
      delta = [expected_val.abs * 1.0e-12, 1.0e-14].max
      assert_in_delta(expected_val, actual_val, delta, x)
    end
  end

  # onum.to_i -> integer
  def test_to_i
    LARGE_RANGE_VALUES.each do |x|
      expected_val = x.to_i
      actual_val = OraNumber.new(x).to_i
      assert_equal(expected_val, actual_val, x)
    end
  end

  # onum.to_s -> string
  def test_to_s
    LARGE_RANGE_VALUES.each do |x|
      expected_val = x
      actual_val = OraNumber.new(x).to_s
      assert_equal(expected_val, actual_val, x)
    end

    conn = get_oci8_connection()
    begin
      cursor = conn.parse('select to_number(:1) from dual')
      cursor.define(1, OraNumber)
      cursor.bind_param(1, nil, String, 200)
      [
       "100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000", # 1E125
       "10000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000", # 1E124
       "234567890234567890234567890234567890000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
       "23456789023456789023456789023456789000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
       "2345678902345678902345678902345678900",
       "234567890234567890234567890234567890",
       "23456789023456789023456789023456789",
       "2345678902345678902345678902345678.9",
       "234567890234567890234567890234567.89",
       "23.456789023456789023456789023456789",
       "2.3456789023456789023456789023456789",
       "0.23456789023456789023456789023456789", # 2.34..snip..E-1
       "0.023456789023456789023456789023456789", # 2.34..snip..E-2
       "0.0023456789023456789023456789023456789", # 2.34..snip..E-3
       "0.00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000023456789023456789023456789023456789", # 2.34..snip..E-130
       "0.000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001", # 1E-129
       "0.0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001", # 1E-130
      ].each do |str|
        cursor[1] = str
        cursor.exec
        onum = cursor.fetch[0]
        assert_equal(str, onum.to_s, "test data: " + str)

        str = '-' + str
        cursor[1] = str
        cursor.exec
        onum = cursor.fetch[0]
        assert_equal(str, onum.to_s, "test data: " + str)
      end
    ensure
      conn.logoff
    end
  end

  # onum.truncate -> integer
  # onum.truncate(decplace) -> ocinumber
  # TODO: test_truncate

  # onum.zero? -> true or false
  def test_zero_p
    LARGE_RANGE_VALUES.each do |x|
      expected_val = x.to_f.zero?
      actual_val = OraNumber.new(x).zero?
      assert_equal(expected_val, actual_val, x)
    end
  end

  def test_new_from_string
    conn = get_oci8_connection()
    begin
      cursor = conn.parse('select to_number(:1) from dual')
      cursor.define(1, OraNumber)
      cursor.bind_param(1, nil, String, 200)
      [
       "100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000", # 1E125
       "10000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000", # 1E124
       "234567890234567890234567890234567890000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
       "23456789023456789023456789023456789000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
       "2345678902345678902345678902345678900",
       "234567890234567890234567890234567890",
       "23456789023456789023456789023456789",
       "2345678902345678902345678902345678.9",
       "234567890234567890234567890234567.89",
       "23.456789023456789023456789023456789",
       "2.3456789023456789023456789023456789",
       "0.23456789023456789023456789023456789", # 2.34..snip..E-1
       "0.023456789023456789023456789023456789", # 2.34..snip..E-2
       "0.0023456789023456789023456789023456789", # 2.34..snip..E-3
       "0.00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000023456789023456789023456789023456789", # 2.34..snip..E-130
       "0.000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001", # 1E-129
       "0.0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001", # 1E-130

       # leading spaces
       "       123",
       # trailing spaces
       "123        ",
       "123.456    ",
       "123E10    ",
       "123.456E10    ",
       # scientific notation
       "1234567890000e-9",
       "123456789000e-8",
       "123456789e-5",
       "12345.6789e-1",
       "12.3456789e+1",
       "0.0000123456789E7",
       # round to zero
       "1E-131",
       # overflow
       "1E126",
       "10E125",
       "100000000000E115",
       "1E+126",
       # invalid number
       "",
       "     ",
       "abc",
       "1E10a",
       "11E10a",
       "1.1.1",
       # round down
       "444444444444444444444444444444444444444444444444440000",
       "44444444444444444444444444444444444444444444444444000",
       "44444444444444444444444444.444444444444444444444444",
       "4444444444444444444444444.4444444444444444444444444",
       "0.000000044444444444444444444444444444444444444444444444444",
       "0.00000044444444444444444444444444444444444444444444444444",
       # round up
       "555555555555555555555555555555555555555555555555550000",
       "55555555555555555555555555555555555555555555555555000",
       "55555555555555555555555555.555555555555555555555555",
       "5555555555555555555555555.5555555555555555555555555",
       "0.000000055555555555555555555555555555555555555555555555555",
       "0.00000055555555555555555555555555555555555555555555555555",
       "999999999999999999999999999999999999999999999999990000",
       "99999999999999999999999999999999999999999999999999000",
       "99999999999999999999999999.999999999999999999999999",
       "9999999999999999999999999.9999999999999999999999999",
       "0.000000099999999999999999999999999999999999999999999999999",
       "0.00000099999999999999999999999999999999999999999999999999",
       # overflow by round up
       "999999999999999999999999999999999999999999999999999999999900000000000000000000000000000000000000000000000000000000000000000000",
      ].each do |str|
        run_test_new_from_string(cursor, str)
        run_test_new_from_string(cursor, '-' + str)
      end
    ensure
      conn.logoff
    end
  end

  def run_test_new_from_string(cursor, str)
    cursor[1] = str
    onum = nil
    begin
      cursor.exec
      onum = cursor.fetch[0]
    rescue OCIError => oraerr
      begin
        OraNumber.new(str)
        flunk("exception expected but none was thrown. test data: " + str)
      rescue
        assert_equal(oraerr.to_s, $!.to_s, "test data: " + str)
      end
    end
    if onum
      assert_equal(onum.dump, OraNumber.new(str).dump, "test data: " + str)
    end
  end

  def test_new_from_bigdecimal
    ["+Infinity", "-Infinity", "NaN"].each do |n|
      assert_raise TypeError do
        OraNumber.new(BigDecimal.new(n))
      end
    end

    LARGE_RANGE_VALUES.each do |val|
      assert_equal(val, OraNumber.new(BigDecimal.new(val)).to_s)
    end
  end

  def test_new_from_rational
    [
     [Rational(1, 2), "0.5"],
     [Rational(3, 5), "0.6"],
     [Rational(10, 3), "3.33333333333333333333333333333333333333"],
     [Rational(20, 3), "6.66666666666666666666666666666666666667"],
    ].each do |ary|
      assert_equal(ary[1], OraNumber.new(ary[0]).to_s)
    end
  end
end
