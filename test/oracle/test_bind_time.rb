require 'rubygems'
require 'oci8'
require 'test/unit'
require File.dirname(__FILE__) + '/config'

class TestBindTime < Test::Unit::TestCase

  YEAR_CHECK_TARGET = [1971, 1989, 2002, 2037]
  MON_CHECK_TARGET = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12]
  DAY_CHECK_TARGET = [1, 10, 20, 31] # days of January.
  HOUR_CHECK_TARGET = [0, 6, 12, 18, 23]
  MIN_CHECK_TARGET = [0, 15, 30, 45, 59]
  SEC_CHECK_TARGET = [0, 15, 30, 45, 59]

  def setup
    @conn = get_oci8_connection
  end

  def test_set_year
    cursor = @conn.parse("BEGIN :year := TO_NUMBER(TO_CHAR(:time, 'SYYYY'), '9999'); END;")
    cursor.bind_param(:time, Time)
    cursor.bind_param(:year, Fixnum)

    YEAR_CHECK_TARGET.each do |i|
      # set year
      cursor[:time] = Time.local(i, 1)
      # check result
      cursor.exec
      assert_equal(i, cursor[:year])
    end
  end

  def test_get_year
    cursor = @conn.parse("BEGIN :time := TO_DATE(TO_CHAR(:year, '0999'), 'SYYYY'); END;")
    cursor.bind_param(:year, Fixnum)
    cursor.bind_param(:time, Time)
    YEAR_CHECK_TARGET.each do |i|
      # set time via oracle.
      cursor[:year] = i
      cursor.exec
      # check Time#year
      assert_equal(i, cursor[:time].year)
    end
  end

  def test_set_mon
    cursor = @conn.parse("BEGIN :mon := TO_NUMBER(TO_CHAR(:time, 'MM'), '99'); END;")
    cursor.bind_param(:time, Time)
    cursor.bind_param(:mon, Fixnum)
    MON_CHECK_TARGET.each do |i|
      # set mon
      cursor[:time] = Time.local(2001, i)
      # check result via oracle.
      cursor.exec
      assert_equal(i, cursor[:mon])
    end
  end

  def test_get_mon
    cursor = @conn.parse("BEGIN :time := TO_DATE(TO_CHAR(:mon, '99'), 'MM'); END;")
    cursor.bind_param(:mon, Fixnum)
    cursor.bind_param(:time, Time)
    MON_CHECK_TARGET.each do |i|
      # set time via oracle.
      cursor[:mon] = i;
      cursor.exec
      # check Time#mon
      assert_equal(i, cursor[:time].mon)
    end
  end

  def test_set_day
    cursor = @conn.parse("BEGIN :day := TO_NUMBER(TO_CHAR(:time, 'DD'), '99'); END;")
    cursor.bind_param(:time, Time)
    cursor.bind_param(:day, Fixnum)
    DAY_CHECK_TARGET.each do |i|
      # set day
      cursor[:time] = Time.local(2001, 1, i)
      # check result via oracle.
      cursor.exec
      assert_equal(i, cursor[:day])
    end
  end

  def test_get_day
    cursor = @conn.parse("BEGIN :time := TO_DATE('200101' || TO_CHAR(:day, 'FM00'), 'YYYYMMDD'); END;")
    day_in = cursor.bind_param(:day, Fixnum)
    time_out = cursor.bind_param(:time, Time)
    DAY_CHECK_TARGET.each do |i|
      # set time via oracle.
      cursor[:day] = i;
      cursor.exec
      # check Time#day
      assert_equal(i, cursor[:time].day)
    end
  end

  def test_set_hour
    cursor = @conn.parse("BEGIN :hour := TO_NUMBER(TO_CHAR(:time, 'HH24'), '99'); END;")
    cursor.bind_param(:time, Time)
    cursor.bind_param(:hour, Fixnum)
    HOUR_CHECK_TARGET.each do |i|
      # set hour
      cursor[:time] = Time.local(2001, 1, 1, i)
      # check result via oracle.
      cursor.exec
      assert_equal(i, cursor[:hour])
    end
  end

  def test_get_hour
    cursor = @conn.parse("BEGIN :time := TO_DATE(TO_CHAR(:hour, '99'), 'HH24'); END;")
    cursor.bind_param(:hour, Fixnum)
    cursor.bind_param(:time, Time)
    HOUR_CHECK_TARGET.each do |i|
      # set time via oracle.
      cursor[:hour] = i
      cursor.exec
      # check Time#hour
      assert_equal(i, cursor[:time].hour)
    end
  end

  def test_set_min
    cursor = @conn.parse("BEGIN :min := TO_NUMBER(TO_CHAR(:time, 'MI'), '99'); END;")
    cursor.bind_param(:time, Time)
    cursor.bind_param(:min, Fixnum)
    MIN_CHECK_TARGET.each do |i|
      # set min
      cursor[:time] = Time.local(2001, 1, 1, 0, i)
      # check result via oracle.
      cursor.exec
      assert_equal(i, cursor[:min])
    end
  end

  def test_get_min
    cursor = @conn.parse("BEGIN :time := TO_DATE(TO_CHAR(:min, '99'), 'MI'); END;")
    cursor.bind_param(:min, Fixnum)
    cursor.bind_param(:time, Time)
    MIN_CHECK_TARGET.each do |i|
      # set time via oracle.
      cursor[:min] = i;
      cursor.exec
      # check Time#min
      assert_equal(i, cursor[:time].min)
    end
  end

  def test_set_sec
    cursor = @conn.parse("BEGIN :sec := TO_NUMBER(TO_CHAR(:time, 'SS'), '99'); END;")
    cursor.bind_param(:time, Time)
    cursor.bind_param(:sec, Fixnum)
    SEC_CHECK_TARGET.each do |i|
      # set sec
      cursor[:time] = Time.local(2001, 1, 1, 0, 0, i)
      # check result via oracle.
      cursor.exec
      assert_equal(i, cursor[:sec])
    end
  end

  def test_get_sec
    cursor = @conn.parse("BEGIN :time := TO_DATE(TO_CHAR(:sec, '99'), 'SS'); END;")
    cursor.bind_param(:sec, Fixnum)
    cursor.bind_param(:time, Time)
    SEC_CHECK_TARGET.each do |i|
      # set time via oracle.
      cursor[:sec] = i
      cursor.exec
      # check Time#sec
      assert_equal(i, cursor[:time].sec)
    end
  end

  def teardown
    @conn.logoff
  end
end
