CREATE OR REPLACE PROCEDURE add_column_if_not_exists(
  alter_table_query VARCHAR2
) IS
    column_exists exception;
    pragma exception_init (column_exists , -01430);
begin
  begin
    execute immediate alter_table_query;
  exception
    when column_exists then null;
  end;
end;
/
show errors

