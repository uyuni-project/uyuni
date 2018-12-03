create or replace procedure drop_constraint_if_exists(p_table_name varchar2, p_constraint_name varchar2)
is
  l_cnt integer;
BEGIN
    SELECT COUNT(*)
      INTO l_cnt
      FROM user_constraints
     WHERE constraint_name = UPPER(p_constraint_name);

    IF( l_cnt = 1 )
    THEN
        EXECUTE IMMEDIATE 'ALTER TABLE ' || p_table_name || ' DROP CONSTRAINT ' || p_constraint_name;
    END IF;
END;
/
show errors

