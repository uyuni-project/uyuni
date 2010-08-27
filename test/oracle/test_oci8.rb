require 'rubygems'
require 'oci8'
require 'test/unit'
require File.dirname(__FILE__) + '/config'
require 'bigdecimal'
require 'rational'

class TestOCI8 < Test::Unit::TestCase

  def setup
    @conn = get_oci8_connection
  end

  def teardown
    @conn.logoff
  end

  def test_rename
    drop_table('test_table')
    drop_table('test_rename_table')
    sql = <<-EOS
CREATE TABLE test_rename_table
  (C CHAR(10) NOT NULL)
EOS
    @conn.exec(sql)
    @conn.exec("RENAME test_rename_table TO test_table")
    drop_table('test_rename_table')
  end

  # USE_DYNAMIC_FETCH doesn't work well...
  # This test is disabled.
  def _test_long_type
    drop_table('test_table')
    @conn.exec('CREATE TABLE test_table (id number(38), lng long)')
    test_data1 = 'a' * 70000
    test_data2 = 'b' * 3000
    test_data3 = nil
    test_data4 = 'c' * 70000
    @conn.exec('insert into test_table values (:1, :2)', 1, test_data1)
    @conn.exec('insert into test_table values (:1, :2)', 2, [test_data2, :long])
    @conn.exec('insert into test_table values (:1, :2)', 3, [nil, :long])
    @conn.exec('insert into test_table values (:1, :2)', 4, [test_data4, :long])

    [8000, 65535, 65536, 80000].each do |read_len|
      @conn.long_read_len = read_len
      cursor = @conn.parse('SELECT lng from test_table order by id')
      cursor.exec
      assert_equal(test_data1, cursor.fetch[0])
      assert_equal(test_data2, cursor.fetch[0])
      assert_equal(test_data3, cursor.fetch[0])
      assert_equal(test_data4, cursor.fetch[0])
      cursor.close
    end
    drop_table('test_table')
  end

  def test_long_type
    @conn.long_read_len = 80000
    drop_table('test_table')
    @conn.exec('CREATE TABLE test_table (id number(38), lng long)')
    test_data1 = 'a' * 70000
    test_data2 = 'b' * 3000
    test_data3 = nil
    test_data4 = 'c' * 70000
    @conn.exec('insert into test_table values (:1, :2)', 1, test_data1)
    @conn.exec('insert into test_table values (:1, :2)', 2, [test_data2, :long])
    @conn.exec('insert into test_table values (:1, :2)', 3, [nil, :long])
    @conn.exec('insert into test_table values (:1, :2)', 4, [test_data4, :long])

    cursor = @conn.parse('SELECT lng from test_table order by id')
    cursor.exec
    assert_equal(test_data1, cursor.fetch[0])
    assert_equal(test_data2, cursor.fetch[0])
    assert_equal(test_data3, cursor.fetch[0])
    assert_equal(test_data4, cursor.fetch[0])
    cursor.close
    drop_table('test_table')
  end

  def test_select
    drop_table('test_table')
    sql = <<-EOS
CREATE TABLE test_table
  (C CHAR(10) NOT NULL,
   V VARCHAR2(20),
   N NUMBER(10, 2),
   D1 DATE, D2 DATE, D3 DATE, D4 DATE,
   INT NUMBER(30), BIGNUM NUMBER(30))
STORAGE (
   INITIAL 4k
   NEXT 4k
   MINEXTENTS 1
   MAXEXTENTS UNLIMITED
   PCTINCREASE 0)
EOS
    @conn.exec(sql)
    cursor = @conn.parse("INSERT INTO test_table VALUES (:C, :V, :N, :D1, :D2, :D3, :D4, :INT, :BIGNUM)")
    1.upto(10) do |i|
      if i == 1
	dt = [nil, OraDate]
      else
	dt = OraDate.new(2000 + i, 8, 3, 23, 59, 59)
      end
      cursor.exec(format("%10d", i * 10), i.to_s, i, dt, dt, dt, dt, i * 11111111111, i * 10000000000)
    end
    cursor.close
    cursor = @conn.parse("SELECT * FROM test_table ORDER BY c")
    cursor.define(5, Time) # define 5th column as Time
    cursor.define(6, Date) # define 6th column as Date
    cursor.define(7, DateTime) # define 7th column as DateTime
    cursor.define(8, Integer) # define 8th column as Integer
    cursor.define(9, Bignum) # define 9th column as Bignum
    cursor.exec
    assert_equal(["C", "V", "N", "D1", "D2", "D3", "D4", "INT", "BIGNUM"], cursor.get_col_names)
    1.upto(10) do |i|
      rv = cursor.fetch
      assert_equal(format("%10d", i * 10), rv[0])
      assert_equal(i.to_s, rv[1])
      assert_equal(i, rv[2])
      if i == 1
	assert_nil(rv[3])
	assert_nil(rv[4])
	assert_nil(rv[5])
	assert_nil(rv[6])
      else
        tm = Time.local(2000 + i, 8, 3, 23, 59, 59)
	dt = Date.civil(2000 + i, 8, 3)
	dttm = DateTime.civil(2000 + i, 8, 3, 23, 59, 59, Time.now.utc_offset.to_r/86400)
	assert_equal(tm, rv[3])
	assert_equal(tm, rv[4])
	assert_equal(dt, rv[5])
	assert_equal(dttm, rv[6])
      end
      assert_equal(i * 11111111111, rv[7])
      assert_equal(i * 10000000000, rv[8])
    end
    assert_nil(cursor.fetch)

    # fetch_hash with block
    cursor.exec
    i = 1
    cursor.fetch_hash do |row|
      assert_equal(format("%10d", i * 10), row['C'])
      assert_equal(i.to_s, row['V'])
      assert_equal(i, row['N'])
      if i == 1
	assert_nil(row['D1'])
	assert_nil(row['D2'])
	assert_nil(row['D3'])
	assert_nil(row['D4'])
      else
        tm = Time.local(2000 + i, 8, 3, 23, 59, 59)
	dt = Date.civil(2000 + i, 8, 3)
	dttm = DateTime.civil(2000 + i, 8, 3, 23, 59, 59, Time.now.utc_offset.to_r/86400)
	assert_equal(tm, row['D1'])
	assert_equal(tm, row['D2'])
	assert_equal(dt, row['D3'])
	assert_equal(dttm, row['D4'])
      end
      assert_equal(i * 11111111111, row['INT'])
      assert_equal(i * 10000000000, row['BIGNUM'])
      i += 1
    end
    assert_equal(11, i)

    cursor.close
    drop_table('test_table')
  end

  def test_bind_cursor
    # FIXME: check again after upgrading Oracle 9.2 to 9.2.0.4.
    return if $oracle_version < OCI8::ORAVER_10_1

    drop_table('test_table')
    sql = <<-EOS
CREATE TABLE test_table
  (C CHAR(10) NOT NULL,
   V VARCHAR2(20),
   N NUMBER(10, 2),
   D1 DATE, D2 DATE, D3 DATE,
   INT NUMBER(30), BIGNUM NUMBER(30))
STORAGE (
   INITIAL 4k
   NEXT 4k
   MINEXTENTS 1
   MAXEXTENTS UNLIMITED
   PCTINCREASE 0)
EOS
    @conn.exec(sql)
    cursor = @conn.parse("INSERT INTO test_table VALUES (:C, :V, :N, :D1, :D2, :D3, :INT, :BIGNUM)")
    1.upto(10) do |i|
      if i == 1
	dt = [nil, OraDate]
      else
	dt = OraDate.new(2000 + i, 8, 3, 23, 59, 59)
      end
      cursor.exec(format("%10d", i * 10), i.to_s, i, dt, dt, dt, i, i)
    end
    cursor.close
    plsql = @conn.parse("BEGIN OPEN :cursor FOR SELECT * FROM test_table ORDER BY c; END;")
    plsql.bind_param(':cursor', nil, OCI8::Cursor)
    plsql.exec
    cursor = plsql[':cursor']
    cursor.define(5, Time) # define 5th column as Time
    cursor.define(6, Date) # define 6th column as Date
    cursor.define(7, Integer) # define 7th column as Integer
    cursor.define(8, Bignum) # define 8th column as Integer
    assert_equal(["C", "V", "N", "D1", "D2", "D3", "INT", "BIGNUM"], cursor.get_col_names)
    1.upto(10) do |i|
      rv = cursor.fetch
      assert_equal(format("%10d", i * 10), rv[0])
      assert_equal(i.to_s, rv[1])
      assert_equal(i, rv[2])
      if i == 1
	assert_nil(rv[3])
	assert_nil(rv[4])
	assert_nil(rv[5])
      else
	dttm = DateTime.civil(2000 + i, 8, 3, 23, 59, 59, Time.now.utc_offset.to_r/86400)
        tm = Time.local(2000 + i, 8, 3, 23, 59, 59)
	dt = Date.civil(2000 + i, 8, 3)
	assert_equal(tm, rv[3])
	assert_equal(tm, rv[4])
	assert_equal(dt, rv[5])
      end
      assert_equal(i, rv[6])
      assert_equal(i, rv[7])
    end
    assert_nil(cursor.fetch)
    cursor.close
    drop_table('test_table')
  end

  def test_cursor_in_result_set
    drop_table('test_table')
    sql = <<-EOS
CREATE TABLE test_table (N NUMBER(10, 2))
STORAGE (
   INITIAL 4k
   NEXT 4k
   MINEXTENTS 1
   MAXEXTENTS UNLIMITED
   PCTINCREASE 0)
EOS
    @conn.exec(sql)
    cursor = @conn.parse("INSERT INTO test_table VALUES (:1)")
    1.upto(10) do |i|
      cursor.exec(i)
    end
    cursor.close
    cursor = @conn.exec(<<EOS)
select a.n, cursor (select a.n + b.n
                      from test_table b
                     order by n)
  from test_table a
 order by n
EOS
    1.upto(10) do |i|
      row = cursor.fetch
      assert_equal(i, row[0])
      cursor_in_result_set = row[1]
      1.upto(10) do |j|
        row2 = cursor_in_result_set.fetch
        assert_equal(i + j, row2[0])
      end
      assert_nil(cursor_in_result_set.fetch) # check end of row data
      cursor_in_result_set.close
    end
    assert_nil(cursor.fetch) # check end of row data
    drop_table('test_table')
  end

  def test_binary_float
    return if $oracle_version < OCI8::ORAVER_10_1

    # Oracle 10g or upper
    cursor = @conn.parse("select CAST(:1 AS BINARY_FLOAT), CAST(:2 AS BINARY_DOUBLE) from dual")
    bind_val = -1.0
    cursor.bind_param(1, 10.0, :binary_double)
    cursor.bind_param(2, nil, :binary_double)
    while bind_val < 10.0
      cursor[2] = bind_val
      cursor.exec
      rv = cursor.fetch
      assert_equal(10.0, rv[0])
      assert_equal(bind_val, rv[1])
      bind_val += 1.234
    end
    [-1.0/0.0, # -Infinite
     +1.0/0.0, # +Infinite
     0.0/0.0   # NaN
    ].each do |num|
      cursor[1] = num
      cursor[2] = num
      cursor.exec
      rv = cursor.fetch
      if num.nan?
        assert(rv[0].nan?)
        assert(rv[1].nan?)
      else
        assert_equal(num, rv[0])
        assert_equal(num, rv[1])
      end
    end
    cursor.close
  end

  def test_clob_nclob_and_blob
    return if OCI8::oracle_client_version < OCI8::ORAVER_8_1

    drop_table('test_table')
    sql = <<-EOS
CREATE TABLE test_table (id number(5), C CLOB, NC NCLOB, B BLOB)
STORAGE (
   INITIAL 100k
   NEXT 100k
   MINEXTENTS 1
   MAXEXTENTS UNLIMITED
   PCTINCREASE 0)
EOS
    @conn.exec(sql)
    cursor = @conn.parse("INSERT INTO test_table VALUES (:1, :2, :3, :4)")
    0.upto(9) do |i|
      val = format('%d', i) * 4096
      cursor.exec(i, OCI8::CLOB.new(@conn, val), OCI8::NCLOB.new(@conn, val), OCI8::BLOB.new(@conn, val))
    end
    cursor.close
    cursor = @conn.exec("select * from test_table order by id")
    0.upto(9) do |i|
      rv = cursor.fetch
      val = format('%d', i) * 4096
      assert_equal(i, rv[0])
      assert_instance_of(OCI8::CLOB, rv[1])
      assert_instance_of(OCI8::NCLOB, rv[2])
      assert_instance_of(OCI8::BLOB, rv[3])
      assert_equal(val, rv[1].read)
      assert_equal(val.length, rv[2].size)
      assert_equal(val, rv[2].read)
      assert_equal(val, rv[3].read)
    end
    assert_nil(cursor.fetch)
    cursor.close
    drop_table('test_table')
  end

  def test_select_number
    drop_table('test_table')
    @conn.exec(<<EOS)
CREATE TABLE test_table (n NUMBER, n20 NUMBER(20), n14_2 NUMBER(14,2), n15_2 NUMBER(15,2), flt FLOAT)
STORAGE (
   INITIAL 100k
   NEXT 100k
   MINEXTENTS 1
   MAXEXTENTS UNLIMITED
   PCTINCREASE 0)
EOS
    @conn.exec(<<EOS)
INSERT INTO test_table values(12345678901234, 12345678901234567890, 123456789012.34, 1234567890123.45, 1234.5)
EOS
    @conn.exec("select * from test_table") do |row|
      assert_equal(row[0], 12345678901234)
      assert_equal(row[1], 12345678901234567890)
      assert_equal(row[2], 123456789012.34)
      assert_equal(row[3], BigDecimal("1234567890123.45"))
      assert_equal(row[4], 1234.5)
      assert_instance_of(BigDecimal, row[0])
      assert_instance_of(Bignum, row[1])
      assert_instance_of(Float, row[2])
      assert_instance_of(BigDecimal, row[3])
      assert_instance_of(Float, row[4])
    end
    drop_table('test_table')
  end

  def test_bind_number_with_implicit_conversions
    src = [1, 1.2, BigDecimal("1.2"), Rational(12, 10)]
    int = [1, 1, 1, 1]
    flt = [1, 1.2, 1.2, 1.2]
    dec = [BigDecimal("1"), BigDecimal("1.2"), BigDecimal("1.2"), BigDecimal("1.2")]
    rat = [Rational(1), Rational(12, 10), Rational(12, 10), Rational(12, 10)]

    cursor = @conn.parse("begin :1 := :2; end;")

    # Float
    cursor.bind_param(1, nil, Float)
    cursor.bind_param(2, nil, Float)
    src.each_with_index do |s, idx|
      cursor[2] = s
      cursor.exec
      assert_equal(cursor[1], flt[idx])
      assert_kind_of(Float, cursor[1])
    end

    # Fixnum
    cursor.bind_param(1, nil, Fixnum)
    cursor.bind_param(2, nil, Fixnum)
    src.each_with_index do |s, idx|
      cursor[2] = s
      cursor.exec
      assert_equal(cursor[1], int[idx])
      assert_kind_of(Fixnum, cursor[1])
    end

    # Integer
    cursor.bind_param(1, nil, Integer)
    cursor.bind_param(2, nil, Integer)
    src.each_with_index do |s, idx|
      cursor[2] = s
      cursor.exec
      assert_equal(cursor[1], int[idx])
      assert_kind_of(Integer, cursor[1])
    end

    # BigDecimal
    cursor.bind_param(1, nil, BigDecimal)
    cursor.bind_param(2, nil, BigDecimal)
    src.each_with_index do |s, idx|
      cursor[2] = s
      cursor.exec
      assert_equal(cursor[1], dec[idx])
      assert_kind_of(BigDecimal, cursor[1])
    end

    # Rational
    cursor.bind_param(1, nil, Rational)
    cursor.bind_param(2, nil, Rational)
    src.each_with_index do |s, idx|
      cursor[2] = s
      cursor.exec
      assert_equal(cursor[1], rat[idx])
      assert_kind_of(Rational, cursor[1])
    end
  end

end # TestOCI8
