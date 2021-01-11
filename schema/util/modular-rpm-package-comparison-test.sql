DO
$$
declare
  result NUMERIC := 0;
begin

select * into result from rpm.rpmstrcmp('7.module_el8.2.0+305+5e198a4', '7.module_el8.2.0+458+dab581ed');
if result <> -1
then
  raise exception 'rpm.rpmstrcmp(7.module_el8.2.0+305+5e198a4, 7.module_el8.2.0+458+dab581ed) should be -1';
end if;

select * into result from rpm.rpmstrcmp('7.module_el8.2.0+458+dab581ed', '7.module_el8.2.0+305+5e198a4');
if result <> 1
then
  raise exception 'rpm.rpmstrcmp(7.module_el8.2.0+458+dab581ed, 7.module_el8.2.0+305+5e198a4) should be 1';
end if;

select * into result from rpm.rpmstrcmp('10.module+el8.2.0+7749+4a513fb2', '10.module+el8.2.0+7749+5a513fb2');
if result <> -1
then
  raise exception 'rpm.rpmstrcmp(10.module+el8.2.0+7749+4a513fb2, 10.module+el8.2.0+7749+5a513fb2) should be -1';
end if;

select * into result from rpm.rpmstrcmp('10.module+el8.2.0+7749+5a513fb2', '10.module+el8.2.0+7749+4a513fb2');
if result <> 1
then
  raise exception 'rpm.rpmstrcmp(10.module+el8.2.0+7749+5a513fb2, 10.module+el8.2.0+7749+4a513fb2) should be 1';
end if;

select * into result from rpm.rpmstrcmp('6.module+el8+1645+8d4014a6', '7.module_el8.2.0+458+dab581e');
if result <> -1
then
  raise exception 'rpm.rpmstrcmp(6.module+el8+1645+8d4014a6, 7.module_el8.2.0+458+dab581e) should be -1';
end if;

select * into result from rpm.rpmstrcmp('7.module_el8.2.0+458+dab581e', '6.module+el8+1645+8d4014a6');
if result <> 1
then
  raise exception 'rpm.rpmstrcmp(7.module_el8.2.0+458+dab581e, 6.module+el8+1645+8d4014a6) should be 1';
end if;


select * into result from rpm.rpmstrcmp('7.module_el8.2.0+305+5e198a4', '7.module_el8.2.0+305+5e198a4');
if result <> 0
then
  raise exception 'rpm.rpmstrcmp(7.module_el8.2.0+305+5e198a4, 7.module_el8.2.0+305+5e198a4) should be -1';
end if;

end;
$$
