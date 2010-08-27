require 'rubygems'
require 'oci8'
require 'test/unit'
require File.dirname(__FILE__) + '/config'

class TestArrayDML < Test::Unit::TestCase
  def setup
    @conn = get_oci8_connection
  end

  def teardown
    @conn.logoff
  end

  # test inserting arrays with different data types
  #   including char, varchar2, number, date and so on
  def test_array_insert1
    drop_table('test_table')
    sql = <<-EOS
CREATE TABLE test_table
  (C CHAR(10) NOT NULL,
   V VARCHAR2(20),
   N NUMBER(10, 2),
   D DATE,
   INT NUMBER(30), 
   BIGNUM NUMBER(30),
   T TIMESTAMP)
STORAGE (
   INITIAL 4k
   NEXT 4k
   MINEXTENTS 1
   MAXEXTENTS UNLIMITED
   PCTINCREASE 0)
EOS
    @conn.exec(sql)
    cursor = @conn.parse("INSERT INTO test_table VALUES (:C, :V, :N, :D, :INT, :BIGNUM, :T)")
    max_array_size = 3
    cursor.max_array_size= max_array_size
    
    cursor.bind_param_array(1, nil, String)
    cursor.bind_param_array(2, nil ,String)
    cursor.bind_param_array(3, nil, Fixnum)
    cursor.bind_param_array(4, nil, OraDate)
    cursor.bind_param_array(5, nil, Integer)
    cursor.bind_param_array(6, nil, Bignum)
    cursor.bind_param_array(7, nil, DateTime)

    c_arr = Array.new
    v_arr = Array.new
    n_arr = Array.new
    d_arr = Array.new
    int_arr = Array.new
    bignum_arr = Array.new
    t_arr = Array.new
    
    1.upto(30) do |i|
      c_arr << format("%10d", i * 10)
      v_arr << i.to_s
      n_arr << i
      d_arr <<  OraDate.new(2000 + i, 12, 24, 23, 59, 59)
      int_arr << i * 11111111111
      bignum_arr << i * 10000000000
      t_arr <<  DateTime.new(2000 + i, 12, 24, 23, 59, 59)
      
      if i%max_array_size == 0
        cursor[1] = c_arr
        cursor[2] = v_arr
        cursor[3] = n_arr
        cursor[4] = d_arr
        cursor[5] = int_arr
        cursor[6] = bignum_arr
        cursor[7] = t_arr
        
        r = cursor.exec_array
        assert_equal(max_array_size, r)
        assert_equal(c_arr, cursor[1])
        assert_equal(v_arr, cursor[2])
        assert_equal(n_arr, cursor[3])
        assert_equal(d_arr, cursor[4])
        assert_equal(int_arr, cursor[5])
        assert_equal(bignum_arr, cursor[6])
        assert_equal(t_arr, cursor[7])
        c_arr.clear
        v_arr.clear
        n_arr.clear
        d_arr.clear
        int_arr.clear
        bignum_arr.clear
        t_arr.clear
      end
    end
    cursor.close
    
    cursor = @conn.parse("SELECT * FROM test_table ORDER BY c")
    cursor.define(5, Integer)
    cursor.define(6, Bignum)
    cursor.exec
    assert_equal(["C","V","N","D","INT","BIGNUM","T"], cursor.get_col_names)
    1.upto(30) do |i|
      rv = cursor.fetch
      assert_equal(format("%10d", i * 10), rv[0])
      assert_equal(i.to_s, rv[1])
      assert_equal(i, rv[2])
      tm = Time.local(2000 + i, 12, 24, 23, 59, 59)
      assert_equal(tm, rv[3])
      assert_equal(i * 11111111111, rv[4])
      assert_equal(i * 10000000000, rv[5])
      assert_equal(tm, rv[6])
    end
    assert_nil(cursor.fetch)
    drop_table('test_table')
  end

  # Raise error when binding arrays are not the same size
  def test_array_insert2
    drop_table('test_table')
    sql = <<-EOS
CREATE TABLE test_table
  (N NUMBER(10, 2) NOT NULL,
   V VARCHAR(20))
EOS
    @conn.exec(sql)
    cursor = @conn.parse("INSERT INTO test_table VALUES (:N, :V)")
    max_array_size = 10
    cursor.max_array_size = max_array_size
    cursor.bind_param_array(1, nil, Fixnum)
    cursor.bind_param_array(2, nil, String)
    n_arr = Array.new
    v_arr = Array.new
    1.upto(max_array_size) do |i|
      n_arr << i
      v_arr << i.to_s if i != max_array_size
    end
    cursor[1] = n_arr
    assert_raise(RuntimeError) { cursor[2] = v_arr }
    cursor.close
    
    drop_table('test_table')
  end  

  # All binds are clear from cursor after calling "max_array_size=", 
  #  in that case, you have to re-bind the array parameters 
  #  otherwise, an error will be raised.
  def test_array_insert3
    drop_table('test_table')
    sql = <<-EOS
CREATE TABLE test_table
  (N NUMBER(10, 2) NOT NULL,
   V VARCHAR(20),
   T TIMESTAMP)
EOS
    @conn.exec(sql)
    cursor = @conn.parse("INSERT INTO test_table VALUES (:N, :V, :T)")
    cursor.max_array_size = 3
    cursor.bind_param_array(1, [1, 2, 3])
    cursor.bind_param_array(2, ['happy', 'new', 'year'])
    cursor.bind_param_array(3, [Time.gm(1990,1,1), Time.gm(2000,1,1), Time.gm(2010,1,1)])
    assert_nothing_raised() { cursor.exec_array }
    cursor.max_array_size = 2
    assert_raise(RuntimeError) { cursor.exec_array }
    drop_table('test_table')
  end

  # The size of binding arrays are not required to be same as max_array_size. The 
  #   only requirement is that they should be the same size, and the size will be 
  #   used as execution count for OCIStmtExecute.
  def test_array_insert4
    drop_table('test_table')
    sql = <<-EOS
CREATE TABLE test_table
  (N NUMBER(10, 2) NOT NULL,
   V VARCHAR(20))
EOS
    @conn.exec(sql)
    cursor = @conn.parse("INSERT INTO test_table VALUES (:N, :V)")
    max_array_size = 4
    cursor.max_array_size = max_array_size
    cursor.bind_param_array(1, nil, Fixnum)
    cursor.bind_param_array(2, nil, String)
    n_arr = Array.new
    v_arr = Array.new
    1.upto( max_array_size - 1 ) do |i|
      n_arr << i
      v_arr << i.to_s
    end
    cursor[1] = n_arr
    cursor[2] = v_arr
    assert_nothing_raised() { cursor.exec_array }
    cursor.close

    cursor = @conn.parse("SELECT * FROM test_table ORDER BY N")
    cursor.exec
    1.upto( max_array_size - 1 ) do |i|
      rv = cursor.fetch
      assert_equal(i, rv[0])
      assert_equal(i.to_s, rv[1])
    end  
    assert_nil(cursor.fetch)
    cursor.close
    drop_table('test_table')    
  end

  # Inserting "nil" elements with array dml raises an error
  def test_array_insert5
    drop_table('test_table')
    sql = <<-EOS
CREATE TABLE test_table
  (N NUMBER(10, 2),
   V VARCHAR(20))
EOS
    @conn.exec(sql)
    cursor = @conn.parse("INSERT INTO test_table VALUES (:N, :V)")
    max_array_size = 3
    cursor.max_array_size = max_array_size
    cursor.bind_param_array(1, nil, Fixnum)
    cursor.bind_param_array(2, nil, String)
    assert_raise(RuntimeError) { cursor.exec_array }
    cursor.close
    drop_table('test_table')
  end

  # delete with array bindings
  def test_array_delete
    drop_table('test_table')
    sql = <<-EOS
CREATE TABLE test_table
  (N NUMBER(10, 2),
   V VARCHAR(20))
EOS
    @conn.exec(sql)    
    cursor = @conn.parse("INSERT INTO test_table VALUES (:N, :V)")
    max_array_size = 10
    cursor.max_array_size = max_array_size
    n_arr = Array.new
    v_arr = Array.new
    1.upto( max_array_size) do |i|
      n_arr << i
      v_arr << i.to_s
    end    
    cursor.bind_param_array(1, nil, Fixnum)
    cursor.bind_param_array(2, nil, String)
    cursor[1] = n_arr
    cursor[2] = v_arr
    cursor.exec_array
    cursor.close

    cursor = @conn.parse("DELETE FROM test_table WHERE N=:1")
    cursor.max_array_size = max_array_size
    delete_arr = Array.new
    1.upto(max_array_size) do |i|
      if i%2 == 0
        delete_arr << i
      end  
    end
    cursor.bind_param_array(1, nil, Fixnum)
    cursor[1] = delete_arr
    cursor.exec_array
    cursor.close
    
    cursor = @conn.parse("SELECT * FROM test_table ORDER BY N")
    cursor.exec
    1.upto( max_array_size ) do |i|
      if i%2 != 0
        rv = cursor.fetch
        assert_equal(rv[0], i)
        assert_equal(rv[1], i.to_s)
      end
    end  
    assert_nil(cursor.fetch)    
    cursor.close
    
    drop_table('test_table')
  end

  # update with array bindings
  def test_array_update
    drop_table('test_table')
    sql = <<-EOS
CREATE TABLE test_table
  (N NUMBER(10, 2),
   V VARCHAR(20))
EOS
    @conn.exec(sql)    
    cursor = @conn.parse("INSERT INTO test_table VALUES (:N, :V)")
    max_array_size = 10
    cursor.max_array_size = max_array_size
    n_arr = Array.new
    v_arr = Array.new
    1.upto( max_array_size) do |i|
      n_arr << i
      v_arr << i.to_s
    end    
    cursor.bind_param_array(1, nil, Fixnum)
    cursor.bind_param_array(2, nil, String)
    cursor[1] = n_arr
    cursor[2] = v_arr
    cursor.exec_array
    cursor.close

    cursor = @conn.parse("UPDATE test_table SET V=:1 WHERE N=:2")
    cursor.max_array_size = max_array_size
    update_arr = Array.new
    update_v_arr = Array.new
    1.upto(max_array_size) do |i|
      if i%2 == 0
        update_arr << i
        update_v_arr << (i * 10).to_s
      end  
    end
    cursor.bind_param_array(1, nil, String)
    cursor.bind_param_array(2, nil, Fixnum)
    cursor[1] = update_v_arr
    cursor[2] = update_arr
    cursor.exec_array
    cursor.close
    
    cursor = @conn.parse("SELECT * FROM test_table ORDER BY N")
    cursor.exec
    1.upto( max_array_size ) do |i|
      rv = cursor.fetch      
      if i%2 != 0
        assert_equal(rv[0], i)
        assert_equal(rv[1], i.to_s)
      else
        assert_equal(rv[0], i)
        assert_equal(rv[1], (i * 10).to_s)
      end
    end  
    assert_nil(cursor.fetch)    

    cursor.close
    drop_table('test_table')
  end
end
