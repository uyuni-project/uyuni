require 'rubygems'
require 'dbi'
require 'oci8'
require 'test/unit'
require File.dirname(__FILE__) + '/config'

class TestDBI < Test::Unit::TestCase

  def setup
    @dbh = get_dbi_connection()
  end

  def teardown
    @dbh.disconnect
  end

  def test_select
    drop_table('test_table')
    sql = <<-EOS
CREATE TABLE test_table
  (C CHAR(10) NOT NULL,
   V VARCHAR2(20),
   N NUMBER(10, 2),
   D DATE)
STORAGE (
   INITIAL 4k
   NEXT 4k
   MINEXTENTS 1
   MAXEXTENTS UNLIMITED
   PCTINCREASE 0)
EOS
    @dbh.do(sql)
    sth = @dbh.prepare("INSERT INTO test_table VALUES (?, ?, ?, ?)")
    1.upto(10) do |i|
      sth.execute(format("%10d", i * 10), i.to_s, i, nil)
    end
    sth = @dbh.execute("SELECT * FROM test_table ORDER BY c")
    assert_equal(["C", "V", "N", "D"], sth.column_names)
    1.upto(10) do |i|
      rv = sth.fetch
      assert_equal(format("%10d", i * 10), rv[0])
      assert_equal(i.to_s, rv[1])
      assert_equal(i, rv[2])
    end
    assert_nil(sth.fetch)
    assert_equal(10, @dbh.select_one("SELECT COUNT(*) FROM test_table")[0])
    @dbh.rollback()
    assert_equal(0, @dbh.select_one("SELECT COUNT(*) FROM test_table")[0])
    drop_table('test_table')
  end

  def test_ref_cursor
    drop_table('test_table')
    sql = <<-EOS
CREATE TABLE test_table
  (C CHAR(10) NOT NULL,
   V VARCHAR2(20),
   N NUMBER(10, 2),
   D DATE)
STORAGE (
   INITIAL 4k
   NEXT 4k
   MINEXTENTS 1
   MAXEXTENTS UNLIMITED
   PCTINCREASE 0)
EOS
    @dbh.do(sql)
    sth = @dbh.prepare("INSERT INTO test_table VALUES (?, ?, ?, ?)")
    1.upto(10) do |i|
      sth.execute(format("%10d", i * 10), i.to_s, i, nil)
    end
    # get a ref cursor
    plsql = @dbh.execute("BEGIN OPEN ? FOR SELECT * FROM test_table ORDER BY c; END;", DBI::StatementHandle)
    sth = plsql.func(:bind_value, 1)
    assert_equal(["C", "V", "N", "D"], sth.column_names)
    1.upto(10) do |i|
      rv = sth.fetch
      assert_equal(format("%10d", i * 10), rv[0])
      assert_equal(i.to_s, rv[1])
      assert_equal(i, rv[2])
    end
    @dbh.rollback()
    drop_table('test_table')
  end

  def test_define
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
    @dbh.do(sql)
    sth = @dbh.prepare("INSERT INTO test_table VALUES (:C, :V, :N, :D1, :D2, :D3, :D4, :INT, :BIGNUM)")
    1.upto(10) do |i|
      if i == 1
        if OCI8::oracle_client_version >= OCI8::ORAVER_9_0
          dt = nil
          v = ''
          sth.execute(format("%10d", i * 10), v, i, dt, dt, dt, dt, i, i)
        else
          # explicitly bind nil with datatype to avoid ORA-01475 when using Oracle 8i.
          sth.bind_param(1, format("%10d", i * 10))
          sth.bind_param(2, '')
          sth.bind_param(3, i)
          sth.bind_param(4, nil, {'type' => OraDate})
          sth.bind_param(5, nil, {'type' => OraDate})
          sth.bind_param(6, nil, {'type' => OraDate})
          sth.bind_param(7, nil, {'type' => OraDate})
          sth.bind_param(8, i)
          sth.bind_param(9, i)
          sth.execute
        end
      else
        dt = OraDate.new(2000 + i, 8, 3, 23, 59, 59)
        v = i.to_s
        sth.execute(format("%10d", i * 10), v, i, dt, dt, dt, dt, i, i)
      end
    end
    sth.finish
    sth = @dbh.prepare("SELECT * FROM test_table ORDER BY c")
    sth.func(:define, 5, Time) # define 5th column as Time
    sth.func(:define, 6, Date) # define 6th column as Date
    sth.func(:define, 7, DateTime) # define 7th column as DateTime
    sth.func(:define, 8, Integer) # define 8th column as Integer
    sth.func(:define, 9, Bignum) # define 9th column as Bignum
    sth.execute
    assert_equal(["C", "V", "N", "D1", "D2", "D3", "D4", "INT", "BIGNUM"], sth.column_info.collect {|cl| cl.name})
    1.upto(10) do |i|
      rv = sth.fetch
      assert_equal(format("%10d", i * 10), rv[0])
      assert_equal(i, rv[2])
      if i == 1
	assert_nil(rv[1])
	assert_nil(rv[3])
	assert_nil(rv[4])
	assert_nil(rv[5])
	assert_nil(rv[6])
      else
	assert_equal(i.to_s, rv[1])
        tm = Time.local(2000 + i, 8, 3, 23, 59, 59)
	dt = Date.civil(2000 + i, 8, 3)
	dttm = DateTime.civil(2000 + i, 8, 3, 23, 59, 59, Time.now.utc_offset.to_r/86400)
	assert_equal(tm, rv[3])
	assert_equal(tm, rv[4])
	assert_equal(dt, rv[5])
	assert_equal(dttm, rv[6])
	assert_instance_of(Time, rv[4])
	assert_instance_of(Date, rv[5])
	assert_instance_of(DateTime, rv[6])
      end
      assert_equal(i, rv[7])
      assert_equal(i, rv[8])
    end
    assert_nil(sth.fetch)
    sth.finish
    drop_table('test_table')
  end

  def test_bind_dbi_data_type
    begin
      if DBI::VERSION >= '0.4.0'
        # suppress deprecated warnings while running this test.
        saved_action = Deprecated.action
        Deprecated.set_action(Proc.new {})
      end

      inval = DBI::Date.new(2004, 3, 20)
      sth = @dbh.execute("BEGIN ? := ?; END;", DBI::Date, inval)
      outval = sth.func(:bind_value, 1)
      assert_instance_of(DBI::Date, outval)
      assert_equal(inval.to_time, outval.to_time)

      inval = DBI::Timestamp.new(2004, 3, 20, 18, 26, 33)
      sth = @dbh.execute("BEGIN ? := ?; END;", DBI::Timestamp, inval)
      outval = sth.func(:bind_value, 1)
      assert_instance_of(DBI::Timestamp, outval)
      assert_equal(inval.to_time, outval.to_time)
    ensure
      Deprecated.set_action(saved_action) if saved_action
    end
  end

  def test_column_info
    if $oracle_version < OCI8::ORAVER_8_1
      begin
        @dbh.columns('tab')
      rescue RuntimeError
        assert_equal("This feature is unavailable on Oracle 8.0", $!.to_s)
      end
      return
    end

    # data_size factor for nchar charset_form.
    sth = @dbh.execute("select N'1' from dual")
    cfrm = sth.column_info[0]['precision']
    if $oracle_version >=  OCI8::ORAVER_9_0
      # data_size factor for char semantics.
      sth = @dbh.execute("select CAST('1' AS CHAR(1 char)) from dual")
      csem = sth.column_info[0]['precision']
    else
      csem = 1
    end

    ora80 = OCI8::ORAVER_8_0
    ora81 = OCI8::ORAVER_8_1
    ora90 = OCI8::ORAVER_9_0
    ora101 = OCI8::ORAVER_10_1
    coldef =
      [
       # oracle_version, definition,    sql_type,        type_name, nullable, precision,scale,indexed,primary,unique,default
       [ora80, "CHAR(10) NOT NULL",        DBI::SQL_CHAR,    'CHAR',     false,        10, nil, true, true, true, nil],
       [ora90, "CHAR(10 CHAR)",            DBI::SQL_CHAR,    'CHAR',     true,  10 * csem, nil, false,false,false,nil],
       [ora80, "NCHAR(10)",                DBI::SQL_CHAR,    'NCHAR',    true,  10 * cfrm, nil, true, false,true, nil],
       [ora80, "VARCHAR2(10) DEFAULT 'a''b'", DBI::SQL_VARCHAR, 'VARCHAR2', true,         10, nil, true, false,false, "a'b"],
       [ora90, "VARCHAR2(10 CHAR)",        DBI::SQL_VARCHAR, 'VARCHAR2', true,  10 * csem, nil, false,false,false,nil],
       [ora80, "NVARCHAR2(10)",            DBI::SQL_VARCHAR, 'NVARCHAR2',true,  10 * cfrm, nil, false,false,false,nil],
       [ora80, "RAW(10)",                  DBI::SQL_VARBINARY, 'RAW',    true,         10, nil, false,false,false,nil],
       [ora81, "CLOB",                     DBI::SQL_CLOB,    'CLOB',     true,       4000, nil, false,false,false,nil],
       [ora81, "NCLOB",                    DBI::SQL_CLOB,    'NCLOB',    true,       4000, nil, false,false,false,nil],
       [ora80, "BLOB",                     DBI::SQL_BLOB,    'BLOB',     true,       4000, nil, false,false,false,nil],
       [ora80, "BFILE",                    DBI::SQL_BLOB,    'BFILE',    true,       4000, nil, false,false,false,nil],
       [ora80, "NUMBER",                   DBI::SQL_NUMERIC, 'NUMBER',   true,         38, nil, false,false,false,nil],
       [ora80, "NUMBER(10)",               DBI::SQL_NUMERIC, 'NUMBER',   true,         10,   0, false,false,false,nil],
       [ora80, "NUMBER(10,2)",             DBI::SQL_NUMERIC, 'NUMBER',   true,         10,   2, false,false,false,nil],
       [ora80, "FLOAT",                    DBI::SQL_FLOAT,   'FLOAT',    true, (126 * 0.30103).ceil, nil, false,false,false,nil],
       [ora80, "FLOAT(10)",                DBI::SQL_FLOAT,   'FLOAT',    true, (10 * 0.30103).ceil, nil, false,false,false,nil],
       [ora101,"BINARY_FLOAT",             DBI::SQL_FLOAT,   'BINARY_FLOAT', true,      7, nil, false,false,false,nil],
       [ora101,"BINARY_DOUBLE",            DBI::SQL_DOUBLE,  'BINARY_DOUBLE', true,    16, nil, false,false,false,nil],
       [ora80, "DATE",                     DBI::SQL_DATE,    'DATE',     true,         19, nil, false,false,false,nil],
       [ora90, "TIMESTAMP",                DBI::SQL_TIMESTAMP, 'TIMESTAMP', true,  20 + 6, nil, false,false,false,nil],
       [ora90, "TIMESTAMP(9)",             DBI::SQL_TIMESTAMP, 'TIMESTAMP', true,  20 + 9, nil, false,false,false,nil],
       [ora90, "TIMESTAMP WITH TIME ZONE",          DBI::SQL_TIMESTAMP, 'TIMESTAMP WITH TIME ZONE', true,  27 + 6, nil, false,false,false,nil],
       [ora90, "TIMESTAMP(9) WITH TIME ZONE",       DBI::SQL_TIMESTAMP, 'TIMESTAMP WITH TIME ZONE', true,  27 + 9, nil, false,false,false,nil],
       [ora90, "TIMESTAMP WITH LOCAL TIME ZONE",    DBI::SQL_TIMESTAMP, 'TIMESTAMP WITH LOCAL TIME ZONE', true,  20 + 6, nil, false,false,false,nil],
       [ora90, "TIMESTAMP(9) WITH LOCAL TIME ZONE", DBI::SQL_TIMESTAMP, 'TIMESTAMP WITH LOCAL TIME ZONE', true,  20 + 9, nil, false,false,false,nil],
       [ora90, "INTERVAL YEAR TO MONTH",      DBI::SQL_OTHER, 'INTERVAL YEAR TO MONTH', true, 2 + 3, nil, false,false,false,nil],
       [ora90, "INTERVAL YEAR(4) TO MONTH",   DBI::SQL_OTHER, 'INTERVAL YEAR TO MONTH', true, 4 + 3, nil, false,false,false,nil],
       [ora90, "INTERVAL DAY TO SECOND",      DBI::SQL_OTHER, 'INTERVAL DAY TO SECOND', true, 2 + 10 + 6, nil, false,false,false,nil],
       [ora90, "INTERVAL DAY(4) TO SECOND(9)",DBI::SQL_OTHER, 'INTERVAL DAY TO SECOND', true, 4 + 10 + 9, nil, false,false,false,nil],
      ]

    coldef.reject! do |c| c[0] > $oracle_version end

    drop_table('test_table')
    @dbh.execute(<<-EOS)
CREATE TABLE test_table (#{n = 0; coldef.collect do |c| n += 1; "C#{n} " + c[1] + (c[8] ? ' PRIMARY KEY' : ''); end.join(',')})
STORAGE (
   INITIAL 100k
   NEXT 100k
   MINEXTENTS 1
   MAXEXTENTS UNLIMITED
   PCTINCREASE 0)
EOS
    coldef.each_with_index do |col, idx|
      next if col[8] # primary
      if col[7] # indexed
        @dbh.execute(<<-EOS)
CREATE #{col[9] ? 'UNIQUE' : ''} INDEX test_table_idx#{idx + 1} ON test_table(C#{idx + 1})
STORAGE (
   INITIAL 100k
   NEXT 100k
   MINEXTENTS 1
   MAXEXTENTS UNLIMITED
   PCTINCREASE 0)
EOS
      end
    end

    @dbh.columns('test_table').each_with_index do |ci, i|
      assert_equal("C#{i + 1}",  ci['name'],      "'#{coldef[i][1]}': name")
      assert_equal(coldef[i][2], ci['sql_type'],  "'#{coldef[i][1]}': sql_type")
      assert_equal(coldef[i][3], ci['type_name'], "'#{coldef[i][1]}': type_name")
      assert_equal(coldef[i][4], ci['nullable'],  "'#{coldef[i][1]}': nullable")
      assert_equal(coldef[i][5], ci['precision'], "'#{coldef[i][1]}': precision")
      assert_equal(coldef[i][6], ci['scale'],     "'#{coldef[i][1]}': scale")
      assert_equal(coldef[i][7], ci['indexed'],   "'#{coldef[i][1]}': indexed")
      assert_equal(coldef[i][8], ci['primary'],   "'#{coldef[i][1]}': primary")
      assert_equal(coldef[i][9], ci['unique'],    "'#{coldef[i][1]}': unique")
      assert_equal(coldef[i][10],ci['default'],   "'#{coldef[i][1]}': default")
    end

    # temporarily change OCI8::BindType::Mapping.
    saved_mapping = {}
    [OCI8::SQLT_TIMESTAMP_TZ,
     OCI8::SQLT_TIMESTAMP_LTZ,
     OCI8::SQLT_INTERVAL_YM,
     OCI8::SQLT_INTERVAL_DS].each do |sqlt_type|
      saved_mapping[sqlt_type] = OCI8::BindType::Mapping[sqlt_type]
      OCI8::BindType::Mapping[sqlt_type] = OCI8::BindType::String
    end
    begin
      sth = @dbh.execute("SELECT * FROM test_table")
    ensure
      saved_mapping.each do |key, val|
        OCI8::BindType::Mapping[key] = val
      end
    end
    sth.column_info.each_with_index do |ci, i|
      assert_equal("C#{i + 1}",  ci['name'],      "'#{coldef[i][1]}': name")
      assert_equal(coldef[i][2], ci['sql_type'],  "'#{coldef[i][1]}': sql_type")
      assert_equal(coldef[i][3], ci['type_name'], "'#{coldef[i][1]}': type_name")
      assert_equal(coldef[i][4], ci['nullable'],  "'#{coldef[i][1]}': nullable")
      assert_equal(coldef[i][5], ci['precision'], "'#{coldef[i][1]}': precision")
      assert_equal(coldef[i][6], ci['scale'],     "'#{coldef[i][1]}': scale")
      assert_equal(nil,          ci['indexed'],   "'#{coldef[i][1]}': indexed")
      assert_equal(nil,          ci['primary'],   "'#{coldef[i][1]}': primary")
      assert_equal(nil,          ci['unique'],    "'#{coldef[i][1]}': unique")
      assert_equal(nil,          ci['default'],   "'#{coldef[i][1]}': default")
    end

    drop_table('test_table')
  end

  def test_column_info_of_tab
    coldef =
      [
       # name,      sql_type,        type_name, nullable,precision,scale,indexed,primary,unique,default
       ["TNAME",    DBI::SQL_VARCHAR,'VARCHAR2',false,   30,       nil,  false,  false,  false, nil],
       ["TABTYPE",  DBI::SQL_VARCHAR,'VARCHAR2',true,     7,       nil,  false,  false,  false, nil],
       ["CLUSTERID",DBI::SQL_NUMERIC,'NUMBER',  true,    38,       nil,  false,  false,  false, nil],
      ]
    begin
      @dbh.columns('tab').each_with_index do |ci, i|
        assert_equal(coldef[i][0], ci['name'],      "'#{coldef[i][0]}': name")
        assert_equal(coldef[i][1], ci['sql_type'],  "'#{coldef[i][0]}': sql_type")
        assert_equal(coldef[i][2], ci['type_name'], "'#{coldef[i][0]}': type_name")
        assert_equal(coldef[i][3], ci['nullable'],  "'#{coldef[i][0]}': nullable")
        assert_equal(coldef[i][4], ci['precision'], "'#{coldef[i][0]}': precision")
        assert_equal(coldef[i][5], ci['scale'],     "'#{coldef[i][0]}': scale")
        assert_equal(coldef[i][6], ci['indexed'],   "'#{coldef[i][0]}': indexed")
        assert_equal(coldef[i][7], ci['primary'],   "'#{coldef[i][0]}': primary")
        assert_equal(coldef[i][8], ci['unique'],    "'#{coldef[i][0]}': unique")
        assert_equal(coldef[i][9], ci['default'],    "'#{coldef[i][0]}': default")
      end
    rescue RuntimeError
      if $oracle_version < OCI8::ORAVER_8_1
        assert_equal("This feature is unavailable on Oracle 8.0", $!.to_s)
      else
        raise
      end
    end

    @dbh.execute("SELECT * FROM tab").column_info.each_with_index do |ci, i|
      assert_equal(coldef[i][0], ci['name'],      "'#{coldef[i][0]}': name")
      assert_equal(coldef[i][1], ci['sql_type'],  "'#{coldef[i][0]}': sql_type")
      assert_equal(coldef[i][2], ci['type_name'], "'#{coldef[i][0]}': type_name")
      assert_equal(coldef[i][3], ci['nullable'],  "'#{coldef[i][0]}': nullable")
      assert_equal(coldef[i][4], ci['precision'], "'#{coldef[i][0]}': precision")
      assert_equal(coldef[i][5], ci['scale'],     "'#{coldef[i][0]}': scale")
      assert_equal(nil,          ci['indexed'],   "'#{coldef[i][0]}': indexed")
      assert_equal(nil,          ci['primary'],   "'#{coldef[i][0]}': primary")
      assert_equal(nil,          ci['unique'],    "'#{coldef[i][0]}': unique")
      assert_equal(nil,          ci['default'],   "'#{coldef[i][0]}': default")
    end
  end

end # TestDBI
