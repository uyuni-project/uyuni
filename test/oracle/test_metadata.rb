require 'rubygems'
require 'oci8'
require 'test/unit'
require File.dirname(__FILE__) + '/config'

class TestMetadata < Test::Unit::TestCase

  def setup
    @conn = get_oci8_connection
  end

  def teardown
    @conn.logoff
  end

  def test_metadata
    if $oracle_version < OCI8::ORAVER_8_1
      begin
        @conn.describe_table('tab').columns
      rescue RuntimeError
        assert_equal("This feature is unavailable on Oracle 8.0", $!.to_s)
      end
      return
    end

    # data_size factor for nchar charset_form.
    cursor = @conn.exec("select N'1' from dual")
    cfrm = cursor.column_metadata[0].data_size
    if $oracle_version >=  OCI8::ORAVER_9_0
      # data_size factor for char semantics.
      cursor = @conn.exec("select CAST('1' AS CHAR(1 char)) from dual")
      csem = cursor.column_metadata[0].data_size
    else
      csem = 1
    end

    ora80 = OCI8::ORAVER_8_0
    ora81 = OCI8::ORAVER_8_1
    ora90 = OCI8::ORAVER_9_0
    ora101 = OCI8::ORAVER_10_1
    coldef =
      [
       # oracle_version, definition,    data_type,  csfrm,    null?,csem?,csize, data_size,prec,scale,fsprec,lfprec
       [ora80, "CHAR(10) NOT NULL",       :char,      :implicit, false, false, 10,        10,   0,    0,    0,    0],
       [ora90, "CHAR(10 CHAR)",           :char,      :implicit, true,  true,  10, 10 * csem,   0,    0,    0,    0],
       [ora80, "NCHAR(10)",               :char,      :nchar,    true,  true,  10, 10 * cfrm,   0,    0,    0,    0],
       [ora80, "VARCHAR2(10)",            :varchar2,  :implicit, true,  false, 10,        10,   0,    0,    0,    0],
       [ora90, "VARCHAR2(10 CHAR)",       :varchar2,  :implicit, true,  true,  10, 10 * csem,   0,    0,    0,    0],
       [ora80, "NVARCHAR2(10)",           :varchar2,  :nchar,    true,  true,  10, 10 * cfrm,   0,    0,    0,    0],
       [ora80, "RAW(10)",                 :raw,       nil,       true,  false,  0,        10,   0,    0,    0,    0],

       # Don't check data_size of CLOB, NCLOB and BLOB.
       #
       #  Oracle 10g XE 10.2.0.1.0 on Linux:
       #   +----------+-----------+
       #   |          | data_size |
       #   +----------+-----------+
       #   | implicit |   4000    |  <= OCI8::Cursor#column_metadata
       #   | explicit |     86    |  <= OCI8.describe_table('table_name').columns
       #   +----------+-----------+
       [ora81, "CLOB",                    :clob,      :implicit, true,  false,  0,       :nc,   0,    0,    0,    0],
       [ora81, "NCLOB",                   :clob,      :nchar,    true,  false,  0,       :nc,   0,    0,    0,    0],
       [ora80, "BLOB",                    :blob,      nil,       true,  false,  0,       :nc,   0,    0,    0,    0],

       [ora80, "BFILE",                   :bfile,     nil,       true,  false,  0,       530,   0,    0,    0,    0],

       # Don't check fsprecision and lfprecision for NUMBER and FLOAT
       #
       #  Oracle 10g XE 10.2.0.1.0 on Linux:
       #   +---------------------------+-------------+-------------+
       #   |                           | fsprecision | lfprecision |
       #   +----------------+----------+-------------+-------------+
       #   | NUMBER         | implicit |     129     |      0      |
       #   |                | explicit |       0     |    129      |
       #   +----------------+----------+-------------+-------------+
       #   | NUMBER(10)     | implicit |       0     |     10      |
       #   |                | explicit |      10     |      0      |
       #   +----------------+----------+-------------+-------------+
       #   | NUMBER(10,2)   | implicit |       2     |     10      |
       #   |                | explicit |      10     |      2      |
       #   +----------------+----------+-------------+-------------+
       #   | FLOAT          | implicit |     129     |    126      |
       #   |                | explicit |     126     |    129      |
       #   +----------------+----------+-------------+-------------+
       #   | FLOAT(10)      | implicit |     129     |     10      |
       #   |                | explicit |      10     |    129      |
       #   +----------------+----------+-------------+-------------+
       [ora80, "NUMBER",                  :number,    nil,       true,  false,  0,        22,   0, $oracle_version >= ora90 ? -127 : 0,  :nc,  :nc],
       [ora80, "NUMBER(10)",              :number,    nil,       true,  false,  0,        22,  10,    0,  :nc,  :nc],
       [ora80, "NUMBER(10,2)",            :number,    nil,       true,  false,  0,        22,  10,    2,  :nc,  :nc],
       [ora80, "FLOAT",                   :number,    nil,       true,  false,  0,        22, 126, -127,  :nc,  :nc],
       [ora80, "FLOAT(10)",               :number,    nil,       true,  false,  0,        22,  10, -127,  :nc,  :nc],

       [ora101,"BINARY_FLOAT",            :binary_float,  nil,   true,  false,  0,         4,   0,    0,    0,    0],
       [ora101,"BINARY_DOUBLE",           :binary_double, nil,   true,  false,  0,         8,   0,    0,    0,    0],
       [ora80, "DATE",                    :date,      nil,       true,  false,  0,         7,   0,    0,    0,    0],

       # Don't check precision and lfprecision for TIMESTAMP
       #
       #  Oracle 10g XE 10.2.0.1.0 on Linux:
       #   +----------------------------------------------+-----------+-------------+
       #   |                                              | precision | lfprecision |
       #   +-----------------------------------+----------+-----------+-------------+
       #   | TIMESTAMP                         | implicit |     0     |      0      |
       #   |                                   | explicit |     6     |      6      |
       #   +-----------------------------------+----------+-----------+-------------+
       #   | TIMESTAMP(9)                      | implicit |     0     |      0      |
       #   |                                   | explicit |     9     |      9      |
       #   +-----------------------------------+----------+-----------+-------------+
       #   | TIMESTAMP WITH TIME ZONE          | implicit |     0     |      0      |
       #   |                                   | explicit |     6     |      6      |
       #   +-----------------------------------+----------+-----------+-------------+
       #   | TIMESTAMP(9) WITH TIME ZONE       | implicit |     0     |      0      |
       #   |                                   | explicit |     9     |      9      |
       #   +-----------------------------------+----------+-----------+-------------+
       #   | TIMESTAMP WITH LOCAL TIME ZONE    | implicit |     0     |      0      |
       #   |                                   | explicit |     6     |      6      |
       #   +-----------------------------------+----------+-----------+-------------+
       #   | TIMESTAMP(9) WITH LOCAL TIME ZONE | implicit |     0     |      0      |
       #   |                                   | explicit |     9     |      9      |
       #   +-----------------------------------+----------+-----------+-------------+
       [ora90, "TIMESTAMP",                         :timestamp,     nil, true, false, 0,  11, :nc,    6,    6,  :nc],
       [ora90, "TIMESTAMP(9)",                      :timestamp,     nil, true, false, 0,  11, :nc,    9,    9,  :nc],
       [ora90, "TIMESTAMP WITH TIME ZONE",          :timestamp_tz,  nil, true, false, 0,  13, :nc,    6,    6,  :nc],
       [ora90, "TIMESTAMP(9) WITH TIME ZONE",       :timestamp_tz,  nil, true, false, 0,  13, :nc,    9,    9,  :nc],
       [ora90, "TIMESTAMP WITH LOCAL TIME ZONE",    :timestamp_ltz, nil, true, false, 0,  11, :nc,    6,    6,  :nc],
       [ora90, "TIMESTAMP(9) WITH LOCAL TIME ZONE", :timestamp_ltz, nil, true, false, 0,  11, :nc,    9,    9,  :nc],

       # Don't check scale and fsprecision for INTERVAL YEAR TO MONTH
       #
       #  Oracle 10g XE 10.2.0.1.0 on Linux:
       #   +-----------------------------------------+-----------+-------------+
       #   |                                         |   scale   | fsprecision |
       #   +------------------------------+----------+-----------+-------------+
       #   | INTERVAL YEAR TO MONTH       | implicit |     0     |      0      |
       #   |                              | explicit |     2     |      2      |
       #   +------------------------------+----------+-----------+-------------+
       #   | INTERVAL YEAR(4) TO MONTH    | implicit |     0     |      0      |
       #   |                              | explicit |     4     |      4      |
       #   +------------------------------+----------+-----------+-------------+
       [ora90, "INTERVAL YEAR TO MONTH",      :interval_ym, nil, true,  false,  0,         5,   2,  :nc,  :nc,    2],
       [ora90, "INTERVAL YEAR(4) TO MONTH",   :interval_ym, nil, true,  false,  0,         5,   4,  :nc,  :nc,    4],

       # Don't check precision and scale for INTERVAL DAY TO SECOND
       #
       #  Oracle 10g XE 10.2.0.1.0 on Linux:
       #   +-----------------------------------------+-----------+-----------+
       #   |                                         | precision |   scale   |
       #   +------------------------------+----------+-----------+-----------+
       #   | INTERVAL DAY TO SECOND       | implicit |     2     |     6     |
       #   |                              | explicit |     6     |     2     |
       #   +------------------------------+----------+-----------+-----------+
       #   | INTERVAL DAY(4) TO SECOND(9) | implicit |     4     |     9     |
       #   |                              | explicit |     9     |     4     |
       #   +------------------------------+----------+-----------+-----------+
       [ora90, "INTERVAL DAY TO SECOND",      :interval_ds, nil, true,  false,  0,        11, :nc,  :nc,    6,    2],
       [ora90, "INTERVAL DAY(4) TO SECOND(9)",:interval_ds, nil, true,  false,  0,        11, :nc,  :nc,    9,    4],
      ]

    coldef.reject! do |c| c[0] > $oracle_version end

    drop_table('test_table')
    sql = <<-EOS
CREATE TABLE test_table (#{idx = 0; coldef.collect do |c| idx += 1; "C#{idx} " + c[1]; end.join(',')})
STORAGE (
   INITIAL 100k
   NEXT 100k
   MINEXTENTS 1
   MAXEXTENTS UNLIMITED
   PCTINCREASE 0)
EOS
    @conn.exec(sql)

    @conn.describe_table('test_table').columns.each_with_index do |md, i|
      # common
      assert_equal("C#{i + 1}", md.name, "'#{coldef[i][1]}': name")
      assert_equal(coldef[i][1], md.type_string, "'#{coldef[i][1]}': type_string")
      assert_equal(coldef[i][2], md.data_type, "'#{coldef[i][1]}': data_type")
      assert_equal(coldef[i][3], md.charset_form, "'#{coldef[i][1]}': charset_form")
      assert_equal(coldef[i][4], md.nullable?, "'#{coldef[i][1]}': nullable? ")
      # string type
      if $oracle_version >= OCI8::ORAVER_9_0
        assert_equal(coldef[i][5], md.char_used?, "'#{coldef[i][1]}': char_used? ")
        assert_equal(coldef[i][6], md.char_size, "'#{coldef[i][1]}': char_size")
      end
      assert_equal(coldef[i][7], md.data_size, "'#{coldef[i][1]}': data_size") if coldef[i][7] != :nc
      # number, timestamp and interval type
      assert_equal(coldef[i][8], md.precision, "'#{coldef[i][1]}': precision") if coldef[i][8] != :nc
      assert_equal(coldef[i][9], md.scale, "'#{coldef[i][1]}': scale") if coldef[i][9] != :nc
      if $oracle_version >= OCI8::ORAVER_9_0
        assert_equal(coldef[i][10], md.fsprecision, "'#{coldef[i][1]}': fsprecision") if coldef[i][10] != :nc
        assert_equal(coldef[i][11], md.lfprecision, "'#{coldef[i][1]}': lfprecision") if coldef[i][11] != :nc
      end
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
      cursor = @conn.exec("SELECT * FROM test_table")
    ensure
      saved_mapping.each do |key, val|
        OCI8::BindType::Mapping[key] = val
      end
    end
    cursor.column_metadata.each_with_index do |md, i|
      # common
      assert_equal("C#{i + 1}", md.name, "'#{coldef[i][1]}': name")
      assert_equal(coldef[i][1], md.type_string, "'#{coldef[i][1]}': type_string")
      assert_equal(coldef[i][2], md.data_type, "'#{coldef[i][1]}': data_type")
      assert_equal(coldef[i][3], md.charset_form, "'#{coldef[i][1]}': charset_form")
      assert_equal(coldef[i][4], md.nullable?, "'#{coldef[i][1]}': nullable? ")
      # string type
      if $oracle_version >=  OCI8::ORAVER_9_0
        assert_equal(coldef[i][5], md.char_used?, "'#{coldef[i][1]}': char_used? ")
        assert_equal(coldef[i][6], md.char_size, "'#{coldef[i][1]}': char_size")
      end
      assert_equal(coldef[i][7], md.data_size, "'#{coldef[i][1]}': data_size") if coldef[i][7] != :nc
      # number, timestamp and interval type
      assert_equal(coldef[i][8], md.precision, "'#{coldef[i][1]}': precision") if coldef[i][8] != :nc
      assert_equal(coldef[i][9], md.scale, "'#{coldef[i][1]}': scale") if coldef[i][9] != :nc
      if $oracle_version >= OCI8::ORAVER_9_0
        assert_equal(coldef[i][10], md.fsprecision, "'#{coldef[i][1]}': fsprecision") if coldef[i][10] != :nc
        assert_equal(coldef[i][11], md.lfprecision, "'#{coldef[i][1]}': lfprecision") if coldef[i][11] != :nc
      end
    end

    drop_table('test_table')
  end

  def test_error_describe_table
    drop_table('test_table')
    begin
      @conn.describe_table('test_table')
      flunk("expects ORA-4043 but no error")
    rescue OCIError
      flunk("expects ORA-4043 but ORA-#{$!.code}") if $!.code != 4043
    end
    @conn.exec('create sequence test_table')
    begin
      begin
        @conn.describe_table('test_table')
        flunk('expects ORA-4043 but no error')
      rescue OCIError
        flunk("expects ORA-4043 but ORA-#{$!.code}") if $!.code != 4043
      end
    ensure
      @conn.exec('drop sequence test_table')
    end
  end

end # TestMetadata
