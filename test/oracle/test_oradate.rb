# Low-level API
require 'rubygems'
require 'oci8'
require 'test/unit'
require File.dirname(__FILE__) + '/config'

class TestOraDate < Test::Unit::TestCase

  YEAR_CHECK_TARGET = [-4712, -1, 1, 1192, 1868, 2002, 9999]
  MONTH_CHECK_TARGET = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12]
  DAY_CHECK_TARGET = [1, 10, 20, 31] # days of January.
  HOUR_CHECK_TARGET = [0, 6, 12, 18, 23]
  MINUTE_CHECK_TARGET = [0, 15, 30, 45, 59]
  SECOND_CHECK_TARGET = [0, 15, 30, 45, 59]

  def setup
    @conn = get_oci8_connection
  end

  def check_oradate(target, year, month, day, hour, minute, second)
    assert_equal(year, target.year)
    assert_equal(month, target.month)
    assert_equal(day, target.day)
    assert_equal(hour, target.hour)
    assert_equal(minute, target.minute)
    assert_equal(second, target.second)
  end

  def test_new()
    check_oradate(OraDate.new(), 1, 1, 1, 0, 0, 0)
  end

  def test_set_year
    cursor = @conn.parse("BEGIN :year := TO_NUMBER(TO_CHAR(:date, 'SYYYY'), '9999'); END;")
    cursor.bind_param(:date, OraDate)
    cursor.bind_param(:year, Fixnum)
    date = OraDate.new()
    YEAR_CHECK_TARGET.each do |i|
      # set year
      date.year = i
      # check result via oracle.
      cursor[:date] = date
      cursor.exec
      assert_equal(i, cursor[:year])
    end
  end

  def test_get_year
    cursor = @conn.parse("BEGIN :date := TO_DATE(TO_CHAR(:year, '0999'), 'SYYYY'); END;")
    cursor.bind_param(:year, Fixnum)
    cursor.bind_param(:date, OraDate)
    YEAR_CHECK_TARGET.each do |i|
      # set date via oracle.
      cursor[:year] = i
      cursor.exec
      # check OraDate#year
      assert_equal(i, cursor[:date].year)
    end
  end

  def test_set_month
    cursor = @conn.parse("BEGIN :month := TO_NUMBER(TO_CHAR(:date, 'MM'), '99'); END;")
    cursor.bind_param(:date, OraDate)
    cursor.bind_param(:month, Fixnum)
    date = OraDate.new()
    MONTH_CHECK_TARGET.each do |i|
      # set month
      date.month = i
      # check result via oracle.
      cursor[:date] = date
      cursor.exec
      assert_equal(i, cursor[:month])
    end
  end

  def test_get_month
    cursor = @conn.parse("BEGIN :date := TO_DATE(TO_CHAR(:month, '99'), 'MM'); END;")
    cursor.bind_param(:month, Fixnum)
    cursor.bind_param(:date, OraDate)
    MONTH_CHECK_TARGET.each do |i|
      # set date via oracle.
      cursor[:month] = i
      cursor.exec
      # check OraDate#month
      assert_equal(i, cursor[:date].month)
    end
  end

  def test_set_day
    cursor = @conn.parse("BEGIN :day := TO_NUMBER(TO_CHAR(:date, 'DD'), '99'); END;")
    cursor.bind_param(:date, OraDate)
    cursor.bind_param(:day, Fixnum)
    date = OraDate.new()
    DAY_CHECK_TARGET.each do |i|
      # set day
      date.day = i
      # check result via oracle.
      cursor[:date] = date
      cursor.exec
      assert_equal(i, cursor[:day])
    end
  end

  def test_get_day
    cursor = @conn.parse("BEGIN :date := TO_DATE('200101' || TO_CHAR(:day, 'FM00'), 'YYYYMMDD'); END;")
    cursor.bind_param(:day, Fixnum)
    cursor.bind_param(:date, OraDate)
    DAY_CHECK_TARGET.each do |i|
      # set date via oracle.
      cursor[:day] = i
      cursor.exec
      # check OraDate#day
      assert_equal(i, cursor[:date].day)
    end
  end

  def test_set_hour
    cursor = @conn.parse("BEGIN :hour := TO_NUMBER(TO_CHAR(:date, 'HH24'), '99'); END;")
    cursor.bind_param(:date, OraDate)
    cursor.bind_param(:hour, Fixnum)
    date = OraDate.new()
    HOUR_CHECK_TARGET.each do |i|
      # set hour
      date.hour = i
      # check result via oracle.
      cursor[:date] = date
      cursor.exec
      assert_equal(i, cursor[:hour])
    end
  end

  def test_get_hour
    cursor = @conn.parse("BEGIN :date := TO_DATE(TO_CHAR(:hour, '99'), 'HH24'); END;")
    cursor.bind_param(:hour, Fixnum)
    cursor.bind_param(:date, OraDate)
    HOUR_CHECK_TARGET.each do |i|
      # set date via oracle.
      cursor[:hour] = i
      cursor.exec
      # check OraDate#hour
      assert_equal(i, cursor[:date].hour)
    end
  end

  def test_set_minute
    cursor = @conn.parse("BEGIN :minute := TO_NUMBER(TO_CHAR(:date, 'MI'), '99'); END;")
    cursor.bind_param(:date, OraDate)
    cursor.bind_param(:minute, Fixnum)
    date = OraDate.new()
    MINUTE_CHECK_TARGET.each do |i|
      # set minute
      date.minute = i
      # check result via oracle.
      cursor[:date] = date
      cursor.exec
      assert_equal(i, cursor[:minute])
    end
  end

  def test_get_minute
    cursor = @conn.parse("BEGIN :date := TO_DATE(TO_CHAR(:minute, '99'), 'MI'); END;")
    cursor.bind_param(:minute, Fixnum)
    cursor.bind_param(:date, OraDate)
    MINUTE_CHECK_TARGET.each do |i|
      # set date via oracle.
      cursor[:minute] = i
      cursor.exec
      # check OraDate#minute
      assert_equal(i, cursor[:date].minute)
    end
  end

  def test_set_second
    cursor = @conn.parse("BEGIN :second := TO_NUMBER(TO_CHAR(:date, 'SS'), '99'); END;")
    cursor.bind_param(:date, OraDate)
    cursor.bind_param(:second, Fixnum)
    date = OraDate.new()
    SECOND_CHECK_TARGET.each do |i|
      # set second
      date.second = i
      # check result via oracle.
      cursor[:date] = date
      cursor.exec
      assert_equal(i, cursor[:second])
    end
  end

  def test_get_second
    cursor = @conn.parse("BEGIN :date := TO_DATE(TO_CHAR(:second, '99'), 'SS'); END;")
    cursor.bind_param(:second, Fixnum)
    cursor.bind_param(:date, OraDate)
    SECOND_CHECK_TARGET.each do |i|
      # set date via oracle.
      cursor[:second] = i
      cursor.exec
      # check OraDate#second
      assert_equal(i, cursor[:date].second)
    end
  end

  def test_compare
    d1 = OraDate.new(2003,03,15,18,55,35)
    d2 = OraDate.new(2003,03,15,18,55,35)
    assert_equal(d1, d2)
    assert_operator(d1, :<, OraDate.new(2004,03,15,18,55,35))
    assert_operator(d1, :<, OraDate.new(2003,04,15,18,55,35))
    assert_operator(d1, :<, OraDate.new(2003,03,16,18,55,35))
    assert_operator(d1, :<, OraDate.new(2003,03,15,19,55,35))
    assert_operator(d1, :<, OraDate.new(2003,03,15,18,56,35))
    assert_operator(d1, :<, OraDate.new(2003,03,15,18,55,36))

    assert_operator(OraDate.new(2002,03,15,18,55,35), :<, d1)
    assert_operator(OraDate.new(2003,02,15,18,55,35), :<, d1)
    assert_operator(OraDate.new(2003,03,14,18,55,35), :<, d1)
    assert_operator(OraDate.new(2003,03,15,17,55,35), :<, d1)
    assert_operator(OraDate.new(2003,03,15,18,54,35), :<, d1)
    assert_operator(OraDate.new(2003,03,15,18,55,34), :<, d1)
  end

  def test_to_time
    year, month, day, hour, minute, second = [2003,03,15,18,55,35]
    dt = OraDate.new(year, month, day, hour, minute, second)
    tm = Time.local(year, month, day, hour, minute, second)
    assert_equal(tm, dt.to_time)

    # year, month, day, hour, minute, second = [1900,1,1,0,0,0]
    # dt = OraDate.new(year, month, day, hour, minute, second)
    # assert_exception(RangeError) { dt.to_time }
  end

  def test_to_date
    [[2003,03,15], [1900,1,1], [-4712, 1, 1]].each do |year, month, day|
      odt = OraDate.new(year, month, day)
      rdt = Date.new(year, month, day)
      assert_equal(rdt, odt.to_date)
    end
  end

  def test_dup
    [[2003,03,15], [1900,1,1], [-4712, 1, 1]].each do |year, month, day|
      dt = OraDate.new(year, month, day)
      assert_equal(dt, dt.dup)
      assert_equal(dt, dt.clone)
    end
  end

  def test_marshal
    [[2003,03,15], [1900,1,1], [-4712, 1, 1]].each do |year, month, day|
      dt = OraDate.new(year, month, day)
      assert_equal(dt, Marshal.load(Marshal.dump(dt)))
    end
  end

  def teardown
    @conn.logoff
  end
end
