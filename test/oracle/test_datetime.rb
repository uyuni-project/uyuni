require 'rubygems'
require 'oci8'
require 'test/unit'
require File.dirname(__FILE__) + '/config'
require 'scanf'

class TestDateTime < Test::Unit::TestCase

  def timezone_string(tzh, tzm)
    if tzh >= 0
      format("+%02d:%02d", tzh, tzm)
    else
      format("-%02d:%02d", -tzh, -tzm)
    end
  end

  def string_to_time(str)
    /(\d+)-(\d+)-(\d+) ?(?:(\d+):(\d+):(\d+))?(?:\.(\d+))? ?([+-]\d+:\d+)?/ =~ str
    args = []
    args << $1.to_i # year
    args << $2.to_i # month
    args << $3.to_i # day
    args << $4.to_i if $4 # hour
    args << $5.to_i if $5 # minute
    if $8
      args << $6.to_i + $7.to_i.to_r / ('1' + '0' * ($7.length)).to_i
      args << $8
      Time.new(*args)
    else
      if $6
        args << $6.to_i
      end
      if $7
        args << $7.to_i.to_r * 1000000 / ('1' + '0' * ($7.length)).to_i
      end
      # no time zone
      Time.local(*args)
    end
    #Time.local(*str.split(/[- :\.]/).collect do |n| n.to_i; end)
  end

  def setup
    @conn = get_oci8_connection
    @local_timezone = timezone_string(*((::Time.now.utc_offset / 60).divmod 60))
  end

  def teardown
    @conn.logoff
  end

  def test_date_select
    ['2005-12-31 23:59:59',
     '2006-01-01 00:00:00'].each do |date|
      @conn.exec(<<-EOS) do |row|
SELECT TO_DATE('#{date}', 'YYYY-MM-DD HH24:MI:SS') FROM dual
EOS
        assert_equal(Time.local(*date.scanf("%d-%d-%d %d:%d:%d.%06d")), row[0])
      end
    end
  end

  def test_date_out_bind
    cursor = @conn.parse(<<-EOS)
BEGIN
  :out := TO_DATE(:in, 'YYYY-MM-DD HH24:MI:SS');
END;
EOS
    cursor.bind_param(:out, nil, DateTime)
    cursor.bind_param(:in, nil, String, 36)
    ['2005-12-31 23:59:59',
     '2006-01-01 00:00:00'].each do |date|
      cursor[:in] = date
      cursor.exec
      assert_equal(DateTime.parse(date + @local_timezone), cursor[:out])
    end
    cursor.close
  end

  def test_date_in_bind
    cursor = @conn.parse(<<-EOS)
DECLARE
  dt date;
BEGIN
  dt := :in;
  :out := TO_CHAR(dt, 'YYYY-MM-DD HH24:MI:SS');
END;
EOS
    cursor.bind_param(:out, nil, String, 33)
    cursor.bind_param(:in, nil, DateTime)
    ['2005-12-31 23:59:59',
     '2006-01-01 00:00:00'].each do |date|
      cursor[:in] = DateTime.parse(date + @local_timezone)
      cursor.exec
      assert_equal(date, cursor[:out])
    end
    cursor.close
  end

  def test_timestamp_select
    return if $oracle_version < OCI8::ORAVER_9_0

    ['2005-12-31 23:59:59.999999000',
     '2006-01-01 00:00:00.000000000'].each do |date|
      @conn.exec(<<-EOS) do |row|
SELECT TO_TIMESTAMP('#{date}', 'YYYY-MM-DD HH24:MI:SS.FF') FROM dual
EOS
        assert_equal(Time.local(*date.scanf("%d-%d-%d %d:%d:%d.%06d")), row[0])
      end
    end
  end

  def test_timestamp_out_bind
    return if $oracle_version < OCI8::ORAVER_9_0

    cursor = @conn.parse(<<-EOS)
BEGIN
  :out := TO_TIMESTAMP(:in, 'YYYY-MM-DD HH24:MI:SS.FF');
END;
EOS
    cursor.bind_param(:out, nil, DateTime)
    cursor.bind_param(:in, nil, String, 36)
    ['2005-12-31 23:59:59.999999000',
     '2006-01-01 00:00:00.000000000'].each do |date|
      cursor[:in] = date
      cursor.exec
      assert_equal(DateTime.parse(date + @local_timezone), cursor[:out])
    end
    cursor.close
  end

  def test_timestamp_in_bind
    return if $oracle_version < OCI8::ORAVER_9_0

    cursor = @conn.parse(<<-EOS)
BEGIN
  :out := TO_CHAR(:in, 'YYYY-MM-DD HH24:MI:SS.FF');
END;
EOS
    cursor.bind_param(:out, nil, String, 33)
    cursor.bind_param(:in, nil, DateTime)
    ['2005-12-31 23:59:59.999999000',
     '2006-01-01 00:00:00.000000000'].each do |date|
      cursor[:in] = DateTime.parse(date + @local_timezone)
      cursor.exec
      assert_equal(date, cursor[:out])
    end
    cursor.close
  end

  def test_timestamp_tz_select
    return if $oracle_version < OCI8::ORAVER_9_0

    ['2005-12-31 23:59:59.999999000 +08:30',
     '2006-01-01 00:00:00.000000000 -08:30'].each do |date|
      @conn.exec(<<-EOS) do |row|
SELECT TO_TIMESTAMP_TZ('#{date}', 'YYYY-MM-DD HH24:MI:SS.FF TZH:TZM') FROM dual
EOS
        expected_val = begin
                         string_to_time(date)
                       rescue
                         DateTime.parse(date)
                       end
        assert_equal(expected_val, row[0])
      end
    end
  end

  def test_timestamp_tz_out_bind
    return if $oracle_version < OCI8::ORAVER_9_0

    cursor = @conn.parse(<<-EOS)
BEGIN
  :out := TO_TIMESTAMP_TZ(:in, 'YYYY-MM-DD HH24:MI:SS.FF TZH:TZM');
END;
EOS
    cursor.bind_param(:out, nil, DateTime)
    cursor.bind_param(:in, nil, String, 36)
    ['2005-12-31 23:59:59.999999000 +08:30',
     '2006-01-01 00:00:00.000000000 -08:30'].each do |date|
      cursor[:in] = date
      cursor.exec
      assert_equal(DateTime.parse(date), cursor[:out])
    end
    cursor.close
  end

  def test_timestamp_tz_in_bind
    return if $oracle_version < OCI8::ORAVER_9_0

    cursor = @conn.parse(<<-EOS)
BEGIN
  :out := TO_CHAR(:in, 'YYYY-MM-DD HH24:MI:SS.FF TZH:TZM');
END;
EOS
    cursor.bind_param(:out, nil, String, 36)
    cursor.bind_param(:in, nil, DateTime)
    ['2005-12-31 23:59:59.999999000 +08:30',
     '2006-01-01 00:00:00.000000000 -08:30'].each do |date|
      cursor[:in] = DateTime.parse(date)
      cursor.exec
      assert_equal(date, cursor[:out])
    end
    cursor.close
  end

  def test_datetype_duck_typing
    cursor = @conn.parse("BEGIN :out := :in; END;")
    cursor.bind_param(:in, nil, DateTime)
    cursor.bind_param(:out, nil, DateTime)
    obj = Object.new
    # test year, month, day
    def obj.year; 2006; end
    def obj.month; 12; end
    def obj.day; 31; end
    cursor[:in] = obj
    cursor.exec
    assert_equal(DateTime.parse('2006-12-31 00:00:00' + @local_timezone), cursor[:out])
    # test hour
    def obj.hour; 23; end
    cursor[:in] = obj
    cursor.exec
    assert_equal(DateTime.parse('2006-12-31 23:00:00' + @local_timezone), cursor[:out])
    # test min
    def obj.min; 59; end
    cursor[:in] = obj
    cursor.exec
    assert_equal(DateTime.parse('2006-12-31 23:59:00' + @local_timezone), cursor[:out])
    # test sec
    def obj.sec; 59; end
    cursor[:in] = obj
    cursor.exec
    assert_equal(DateTime.parse('2006-12-31 23:59:59' + @local_timezone), cursor[:out])

    # sec_fraction and timezone are available on Oracle 9i or later
    return if $oracle_version < OCI8::ORAVER_9_0

    # test sec_fraction
    def obj.sec_fraction; DateTime.parse('0001-01-01 00:00:00.000001').sec_fraction * 999999 ; end
    cursor[:in] = obj
    cursor.exec
    assert_equal(DateTime.parse('2006-12-31 23:59:59.999999' + @local_timezone), cursor[:out])
    # test utc_offset (Time)
    def obj.utc_offset; @utc_offset; end
    obj.instance_variable_set(:@utc_offset, 9 * 60 * 60)
    cursor[:in] = obj
    cursor.exec
    assert_equal(DateTime.parse('2006-12-31 23:59:59.999999 +09:00'), cursor[:out])
    obj.instance_variable_set(:@utc_offset, -5 * 60 * 60)
    cursor[:in] = obj
    cursor.exec
    assert_equal(DateTime.parse('2006-12-31 23:59:59.999999 -05:00'), cursor[:out])
    # test offset (DateTime)
    def obj.offset; @offset; end
    obj.instance_variable_set(:@offset, 9.to_r / 24)
    cursor[:in] = obj
    cursor.exec
    assert_equal(DateTime.parse('2006-12-31 23:59:59.999999 +09:00'), cursor[:out])
    obj.instance_variable_set(:@offset, -5.to_r / 24)
    cursor[:in] = obj
    cursor.exec
    assert_equal(DateTime.parse('2006-12-31 23:59:59.999999 -05:00'), cursor[:out])
  end

  def test_timezone
    if $oracle_version >= OCI8::ORAVER_9_0
      # temporarily change the mapping to test OCI8::BindType::Util.default_timezone.
      OCI8::BindType::Mapping[:date] = OCI8::BindType::TimeViaOCIDate
    end
    begin
      assert_raise(ArgumentError) do
        OCI8::BindType::Util.default_timezone = :invalid_value
      end

      [:local, :utc].each do |tz|
        OCI8::BindType::Util.default_timezone = tz
        @conn.exec("select sysdate, to_date('2008-01-02', 'yyyy-mm-dd') from dual") do |row|
          row.each do |dt|
            assert_kind_of(Time, dt)
            assert_equal(tz, dt.utc? ? :utc : :local)
          end
          assert_equal(2008, row[1].year)
          assert_equal(1, row[1].month)
          assert_equal(2, row[1].day)
        end
      end
    ensure
      OCI8::BindType::Util.default_timezone = :local
      if $oracle_version >= OCI8::ORAVER_9_0
        OCI8::BindType::Mapping[:date] = OCI8::BindType::Time
      end
    end

    if $oracle_version >= OCI8::ORAVER_9_0
      ses_tz = nil
      @conn.exec('select sessiontimezone from dual') do |row|
        ses_tz = row[0]
      end

      begin
        ['+09:00', '+00:00', '-05:00'].each do |tz|
          @conn.exec("alter session set time_zone = '#{tz}'")
          @conn.exec("select current_timestamp, sysdate, to_timestamp('2008-01-02', 'yyyy-mm-dd') from dual") do |row|
            row.each do |dt|
              case dt
              when Time
                assert_equal(tz, timezone_string(*((dt.utc_offset / 60).divmod 60)))
              when DateTime
                tz = tz.gsub(/:/, '') if RUBY_VERSION <= '1.8.5'
                assert_equal(tz, dt.zone)
              else
                flunk "unexpedted type #{dt.class}"
              end
            end
            assert_equal(2008, row[2].year)
            assert_equal(1, row[2].month)
            assert_equal(2, row[2].day)
          end
        end
      ensure
        @conn.exec("alter session set time_zone = '#{ses_tz}'")
      end
    else
    end
  end

  def test_interval_ym_select
    return if $oracle_version < OCI8::ORAVER_9_0

    [['2006-01-01', '2004-03-01'],
     ['2006-01-01', '2005-03-01'],
     ['2006-01-01', '2006-03-01'],
     ['2006-01-01', '2007-03-01']
    ].each do |date1, date2|
      @conn.exec(<<-EOS) do |row|
SELECT (TO_TIMESTAMP('#{date1}', 'YYYY-MM-DD')
      - TO_TIMESTAMP('#{date2}', 'YYYY-MM-DD')) YEAR TO MONTH
  FROM dual
EOS
        assert_equal(DateTime.parse(date1), DateTime.parse(date2) >> row[0])
      end
    end
  end

  def test_interval_ym_out_bind
    return if $oracle_version < OCI8::ORAVER_9_0

    cursor = @conn.parse(<<-EOS)
DECLARE
  ts1 TIMESTAMP;
  ts2 TIMESTAMP;
BEGIN
  ts1 := TO_TIMESTAMP(:in1, 'YYYY-MM-DD');
  ts2 := TO_TIMESTAMP(:in2, 'YYYY-MM-DD');
  :out := (ts1 - ts2) YEAR TO MONTH;
END;
EOS
    cursor.bind_param(:out, nil, :interval_ym)
    cursor.bind_param(:in1, nil, String, 36)
    cursor.bind_param(:in2, nil, String, 36)
    [['2006-01-01', '2004-03-01'],
     ['2006-01-01', '2005-03-01'],
     ['2006-01-01', '2006-03-01'],
     ['2006-01-01', '2007-03-01']
    ].each do |date1, date2|
      cursor[:in1] = date1
      cursor[:in2] = date2
      cursor.exec
      assert_equal(DateTime.parse(date1), DateTime.parse(date2) >> cursor[:out])
    end
    cursor.close
  end

  def test_interval_ym_in_bind
    return if $oracle_version < OCI8::ORAVER_9_0

    cursor = @conn.parse(<<-EOS)
DECLARE
  ts1 TIMESTAMP;
BEGIN
  ts1 := TO_TIMESTAMP(:in1, 'YYYY-MM-DD');
  :out := TO_CHAR(ts1 + :in2, 'YYYY-MM-DD');
END;
EOS
    cursor.bind_param(:out, nil, String, 36)
    cursor.bind_param(:in1, nil, String, 36)
    cursor.bind_param(:in2, nil, :interval_ym)
    [['2006-01-01', -25],
     ['2006-01-01', -24],
     ['2006-01-01', -23],
     ['2006-01-01', -13],
     ['2006-01-01', -12],
     ['2006-01-01', -11],
     ['2006-01-01',  +2],
     ['2006-01-01',  -2],
     ['2006-01-01', +12]
    ].each do |date, interval|
      cursor[:in1] = date
      cursor[:in2] = interval
      cursor.exec
      assert_equal(DateTime.parse(date) >> interval, DateTime.parse(cursor[:out]))
    end
    cursor.close
  end

  def test_interval_ds_select
    return if $oracle_version < OCI8::ORAVER_9_0

    [['2006-01-01', '2004-03-01'],
     ['2006-01-01', '2005-03-01'],
     ['2006-01-01', '2006-03-01'],
     ['2006-01-01', '2007-03-01'],
     ['2006-01-01', '2006-01-01 23:00:00'],
     ['2006-01-01', '2006-01-01 00:59:00'],
     ['2006-01-01', '2006-01-01 00:00:59'],
     ['2006-01-01', '2006-01-01 00:00:00.999999'],
     ['2006-01-01', '2006-01-01 23:59:59.999999'],
     ['2006-01-01', '2005-12-31 23:00:00'],
     ['2006-01-01', '2005-12-31 00:59:00'],
     ['2006-01-01', '2005-12-31 00:00:59'],
     ['2006-01-01', '2005-12-31 00:00:00.999999'],
     ['2006-01-01', '2005-12-31 23:59:59.999999']
    ].each do |date1, date2|
      @conn.exec(<<-EOS) do |row|
SELECT (TO_TIMESTAMP('#{date1}', 'YYYY-MM-DD HH24:MI:SS.FF')
      - TO_TIMESTAMP('#{date2}', 'YYYY-MM-DD HH24:MI:SS.FF')) DAY(3) TO SECOND
  FROM dual
EOS
        assert_in_delta(string_to_time(date1) - string_to_time(date2), row[0], 0.0000000001)
      end
    end
  end

  def test_interval_ds_out_bind
    return if $oracle_version < OCI8::ORAVER_9_0

    cursor = @conn.parse(<<-EOS)
DECLARE
  ts1 TIMESTAMP;
  ts2 TIMESTAMP;
BEGIN
  ts1 := TO_TIMESTAMP(:in1, 'YYYY-MM-DD HH24:MI:SS.FF');
  ts2 := TO_TIMESTAMP(:in2, 'YYYY-MM-DD HH24:MI:SS.FF');
  :out := (ts1 - ts2) DAY TO SECOND(9);
END;
EOS
    cursor.bind_param(:out, nil, :interval_ds)
    cursor.bind_param(:in1, nil, String, 36)
    cursor.bind_param(:in2, nil, String, 36)
    [['2006-01-01', '2004-03-01'],
     ['2006-01-01', '2005-03-01'],
     ['2006-01-01', '2006-03-01'],
     ['2006-01-01', '2007-03-01'],
     ['2006-01-01', '2006-01-01 23:00:00'],
     ['2006-01-01', '2006-01-01 00:59:00'],
     ['2006-01-01', '2006-01-01 00:00:59'],
     ['2006-01-01', '2006-01-01 00:00:00.999999'],
     ['2006-01-01', '2006-01-01 23:59:59.999999'],
     ['2006-01-01', '2005-12-31 23:00:00'],
     ['2006-01-01', '2005-12-31 00:59:00'],
     ['2006-01-01', '2005-12-31 00:00:59'],
     ['2006-01-01', '2005-12-31 00:00:00.999999'],
     ['2006-01-01', '2005-12-31 23:59:59.999999']
    ].each do |date1, date2|
      cursor[:in1] = date1
      cursor[:in2] = date2
      cursor.exec
      assert_in_delta(string_to_time(date1) - string_to_time(date2), cursor[:out], 0.0000000001)
    end
    cursor.close
  end

  def test_interval_ds_in_bind
    return if $oracle_version < OCI8::ORAVER_9_0

    cursor = @conn.parse(<<-EOS)
DECLARE
  ts1 TIMESTAMP;
BEGIN
  ts1 := TO_TIMESTAMP(:in1, 'YYYY-MM-DD HH24:MI:SS.FF');
  :out := TO_CHAR(ts1 + :in2, 'YYYY-MM-DD HH24:MI:SS.FF6');
END;
EOS
    cursor.bind_param(:out, nil, String, 36)
    cursor.bind_param(:in1, nil, String, 36)
    cursor.bind_param(:in2, nil, :interval_ds)
    [['2006-01-01', -22],
     ['2006-01-01', -10],
     ['2006-01-01',  +2],
     ['2006-01-01', +12],
     ['2006-01-01', -1.to_r / 24], # one hour
     ['2006-01-01', -1.to_r / (24*60)], # one minute
     ['2006-01-01', -1.to_r / (24*60*60)], # one second
     ['2006-01-01', -999999.to_r / (24*60*60*1000000)], # 0.999999 seconds
     ['2006-01-01', +1.to_r / 24], # one hour
     ['2006-01-01', +1.to_r / (24*60)], # one minute
     ['2006-01-01', +1.to_r / (24*60*60)], # one second
     ['2006-01-01', +999999.to_r / (24*60*60*1000000)] # 0.999999 seconds
    ].each do |date, interval|
      interval *= 86400
      cursor[:in1] = date
      cursor[:in2] = interval
      cursor.exec
      assert_equal(string_to_time(date) + interval, string_to_time(cursor[:out]))
    end
    cursor.close
  end

  def test_days_interval_ds_select
    return if $oracle_version < OCI8::ORAVER_9_0

    [['2006-01-01', '2004-03-01'],
     ['2006-01-01', '2005-03-01'],
     ['2006-01-01', '2006-03-01'],
     ['2006-01-01', '2007-03-01'],
     ['2006-01-01', '2006-01-01 23:00:00'],
     ['2006-01-01', '2006-01-01 00:59:00'],
     ['2006-01-01', '2006-01-01 00:00:59'],
     ['2006-01-01', '2006-01-01 00:00:00.999999'],
     ['2006-01-01', '2006-01-01 23:59:59.999999'],
     ['2006-01-01', '2005-12-31 23:00:00'],
     ['2006-01-01', '2005-12-31 00:59:00'],
     ['2006-01-01', '2005-12-31 00:00:59'],
     ['2006-01-01', '2005-12-31 00:00:00.999999'],
     ['2006-01-01', '2005-12-31 23:59:59.999999']
    ].each do |date1, date2|
      begin
        OCI8::BindType::IntervalDS.unit = :day
        @conn.exec(<<-EOS) do |row|
SELECT (TO_TIMESTAMP('#{date1}', 'YYYY-MM-DD HH24:MI:SS.FF')
      - TO_TIMESTAMP('#{date2}', 'YYYY-MM-DD HH24:MI:SS.FF')) DAY(3) TO SECOND
  FROM dual
EOS
          assert_equal(DateTime.parse(date1) - DateTime.parse(date2), row[0])
        end
      ensure
        OCI8::BindType::IntervalDS.unit = :second
      end
    end
  end

  def test_days_interval_ds_out_bind
    return if $oracle_version < OCI8::ORAVER_9_0

    cursor = @conn.parse(<<-EOS)
DECLARE
  ts1 TIMESTAMP;
  ts2 TIMESTAMP;
BEGIN
  ts1 := TO_TIMESTAMP(:in1, 'YYYY-MM-DD HH24:MI:SS.FF');
  ts2 := TO_TIMESTAMP(:in2, 'YYYY-MM-DD HH24:MI:SS.FF');
  :out := (ts1 - ts2) DAY TO SECOND(9);
END;
EOS
    cursor.bind_param(:out, nil, :interval_ds)
    cursor.bind_param(:in1, nil, String, 36)
    cursor.bind_param(:in2, nil, String, 36)
    [['2006-01-01', '2004-03-01'],
     ['2006-01-01', '2005-03-01'],
     ['2006-01-01', '2006-03-01'],
     ['2006-01-01', '2007-03-01'],
     ['2006-01-01', '2006-01-01 23:00:00'],
     ['2006-01-01', '2006-01-01 00:59:00'],
     ['2006-01-01', '2006-01-01 00:00:59'],
     ['2006-01-01', '2006-01-01 00:00:00.999999'],
     ['2006-01-01', '2006-01-01 23:59:59.999999'],
     ['2006-01-01', '2005-12-31 23:00:00'],
     ['2006-01-01', '2005-12-31 00:59:00'],
     ['2006-01-01', '2005-12-31 00:00:59'],
     ['2006-01-01', '2005-12-31 00:00:00.999999'],
     ['2006-01-01', '2005-12-31 23:59:59.999999']
    ].each do |date1, date2|
      begin
        OCI8::BindType::IntervalDS.unit = :day
        cursor[:in1] = date1
        cursor[:in2] = date2
        cursor.exec
        assert_equal(DateTime.parse(date1) - DateTime.parse(date2), cursor[:out])
      ensure
        OCI8::BindType::IntervalDS.unit = :second
      end
    end
    cursor.close
  end

  def test_days_interval_ds_in_bind
    return if $oracle_version < OCI8::ORAVER_9_0

    cursor = @conn.parse(<<-EOS)
DECLARE
  ts1 TIMESTAMP;
BEGIN
  ts1 := TO_TIMESTAMP(:in1, 'YYYY-MM-DD');
  :out := TO_CHAR(ts1 + :in2, 'YYYY-MM-DD HH24:MI:SS.FF');
END;
EOS
    cursor.bind_param(:out, nil, String, 36)
    cursor.bind_param(:in1, nil, String, 36)
    cursor.bind_param(:in2, nil, :interval_ds)
    [['2006-01-01', -22],
     ['2006-01-01', -10],
     ['2006-01-01',  +2],
     ['2006-01-01', +12],
     ['2006-01-01', -1.to_r / 24], # one hour
     ['2006-01-01', -1.to_r / (24*60)], # one minute
     ['2006-01-01', -1.to_r / (24*60*60)], # one second
     ['2006-01-01', -999999.to_r / (24*60*60*1000000)], # 0.999999 seconds
     ['2006-01-01', +1.to_r / 24], # one hour
     ['2006-01-01', +1.to_r / (24*60)], # one minute
     ['2006-01-01', +1.to_r / (24*60*60)], # one second
     ['2006-01-01', +999999.to_r / (24*60*60*1000000)] # 0.999999 seconds
    ].each do |date, interval|
      begin
        OCI8::BindType::IntervalDS.unit = :day
        cursor[:in1] = date
        cursor[:in2] = interval
        cursor.exec
        assert_equal(DateTime.parse(date) + interval, DateTime.parse(cursor[:out]))
      ensure
        OCI8::BindType::IntervalDS.unit = :second
      end
    end
  end
end # TestOCI8
