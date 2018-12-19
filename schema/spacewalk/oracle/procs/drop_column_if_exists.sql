CREATE OR REPLACE PROCEDURE drop_column_if_exists(
  p_table_name VARCHAR2,
  p_column_name VARCHAR2
) IS
  l_cnt integer;
BEGIN
  SELECT COUNT(*)
    INTO l_cnt
    FROM user_tab_columns
   WHERE table_name = UPPER(p_table_name)
     and column_name = UPPER(p_column_name);

  IF( l_cnt = 1 )
  THEN
    EXECUTE IMMEDIATE 'ALTER TABLE ' || p_table_name || ' DROP COLUMN ' || p_column_name;
  END IF;
END;
/
show errors

