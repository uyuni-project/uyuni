-- source: https://git.dpkg.org/cgit/dpkg/dpkg.git/tree/scripts/t/Dpkg_Version.t
DO
$$
declare
	result NUMERIC := 0;
begin
select * into result from rpm.rpmstrcmp('1.0-1', '2.0-2');
if result <> -1
then
	raise notice 'rpm.rpmstrcmp(1.0-1, 2.0-2) should be -1';
end if;
select * into result from rpm.rpmstrcmp('2.2~rc-4', '2.2-1');
if result <> -1
then
	raise notice 'rpm.rpmstrcmp(2.2~rc-4, 2.2-1) should be -1';
end if;
select * into result from rpm.rpmstrcmp('2.2-1', '2.2~rc-4');
if result <> 1
then
	raise notice 'rpm.rpmstrcmp(2.2-1, 2.2~rc-4) should be 1';
end if;
select * into result from rpm.rpmstrcmp('1.0000-1', '1.0-1');
if result <> 0
then
	raise notice 'rpm.rpmstrcmp(1.0000-1, 1.0-1) should be 0';
end if;
select * into result from rpm.rpmstrcmp('1', '1');
if result <> 0
then
	raise notice 'rpm.rpmstrcmp(1, 1) should be 0';
end if;
select * into result from rpm.rpmstrcmp('0', '0-0');
if result <> 0
then
	raise notice 'rpm.rpmstrcmp(0, 0-0) should be 0';
end if;
select * into result from rpm.rpmstrcmp('2.5', '7.5');
if result <> -1
then
	raise notice 'rpm.rpmstrcmp(2.5, 7.5) should be -1';
end if;
select * into result from rpm.rpmstrcmp('0foo', '0foo');
if result <> 0
then
	raise notice 'rpm.rpmstrcmp(0foo, 0foo) should be 0';
end if;
select * into result from rpm.rpmstrcmp('0foo-0', '0foo');
if result <> 0
then
	raise notice 'rpm.rpmstrcmp(0foo-0, 0foo) should be 0';
end if;
select * into result from rpm.rpmstrcmp('0foo', '0foo-0');
if result <> 0
then
	raise notice 'rpm.rpmstrcmp(0foo, 0foo-0) should be 0';
end if;
select * into result from rpm.rpmstrcmp('0foo', '0fo');
if result <> 1
then
	raise notice 'rpm.rpmstrcmp(0foo, 0fo) should be 1';
end if;
select * into result from rpm.rpmstrcmp('0foo-0', '0foo+');
if result <> -1
then
	raise notice 'rpm.rpmstrcmp(0foo-0, 0foo+) should be -1';
end if;
select * into result from rpm.rpmstrcmp('0foo~1', '0foo');
if result <> -1
then
	raise notice 'rpm.rpmstrcmp(0foo~1, 0foo) should be -1';
end if;
select * into result from rpm.rpmstrcmp('0foo~foo+Bar', '0foo~foo+bar');
if result <> -1
then
	raise notice 'rpm.rpmstrcmp(0foo~foo+Bar, 0foo~foo+bar) should be -1';
end if;
select * into result from rpm.rpmstrcmp('0foo~~', '0foo~');
if result <> -1
then
	raise notice 'rpm.rpmstrcmp(0foo~~, 0foo~) should be -1';
end if;
select * into result from rpm.rpmstrcmp('1~', '1');
if result <> -1
then
	raise notice 'rpm.rpmstrcmp(1~, 1) should be -1';
end if;
select * into result from rpm.rpmstrcmp('12345+that-really-is-some-ver-0', '12345+that-really-is-some-ver-10');
if result <> -1
then
	raise notice 'rpm.rpmstrcmp(12345+that-really-is-some-ver-0, 12345+that-really-is-some-ver-10) should be -1';
end if;
select * into result from rpm.rpmstrcmp('0foo-0', '0foo-01');
if result <> -1
then
	raise notice 'rpm.rpmstrcmp(0foo-0, 0foo-01) should be -1';
end if;
select * into result from rpm.rpmstrcmp('0foo.bar', '0foobar');
if result <> 1
then
	raise notice 'rpm.rpmstrcmp(0foo.bar, 0foobar) should be 1';
end if;
select * into result from rpm.rpmstrcmp('0foo.bar', '0foo1bar');
if result <> 1
then
	raise notice 'rpm.rpmstrcmp(0foo.bar, 0foo1bar) should be 1';
end if;
select * into result from rpm.rpmstrcmp('0foo.bar', '0foo0bar');
if result <> 1
then
	raise notice 'rpm.rpmstrcmp(0foo.bar, 0foo0bar) should be 1';
end if;
select * into result from rpm.rpmstrcmp('0foo1bar-1', '0foobar-1');
if result <> -1
then
	raise notice 'rpm.rpmstrcmp(0foo1bar-1, 0foobar-1) should be -1';
end if;
select * into result from rpm.rpmstrcmp('0foo2.0', '0foo2');
if result <> 1
then
	raise notice 'rpm.rpmstrcmp(0foo2.0, 0foo2) should be 1';
end if;
select * into result from rpm.rpmstrcmp('0foo2.0.0', '0foo2.10.0');
if result <> -1
then
	raise notice 'rpm.rpmstrcmp(0foo2.0.0, 0foo2.10.0) should be -1';
end if;
select * into result from rpm.rpmstrcmp('0foo2.0', '0foo2.0.0');
if result <> -1
then
	raise notice 'rpm.rpmstrcmp(0foo2.0, 0foo2.0.0) should be -1';
end if;
select * into result from rpm.rpmstrcmp('0foo2.0', '0foo2.10');
if result <> -1
then
	raise notice 'rpm.rpmstrcmp(0foo2.0, 0foo2.10) should be -1';
end if;
select * into result from rpm.rpmstrcmp('0foo2.1', '0foo2.10');
if result <> -1
then
	raise notice 'rpm.rpmstrcmp(0foo2.1, 0foo2.10) should be -1';
end if;
select * into result from rpm.rpmstrcmp('1.09', '1.9');
if result <> 0
then
	raise notice 'rpm.rpmstrcmp(1.09, 1.9) should be 0';
end if;
select * into result from rpm.rpmstrcmp('1.0.8+nmu1', '1.0.8');
if result <> 1
then
	raise notice 'rpm.rpmstrcmp(1.0.8+nmu1, 1.0.8) should be 1';
end if;
select * into result from rpm.rpmstrcmp('3.11', '3.10+nmu1');
if result <> 1
then
	raise notice 'rpm.rpmstrcmp(3.11, 3.10+nmu1) should be 1';
end if;
select * into result from rpm.rpmstrcmp('0.9j-20080306-4', '0.9i-20070324-2');
if result <> 1
then
	raise notice 'rpm.rpmstrcmp(0.9j-20080306-4, 0.9i-20070324-2) should be 1';
end if;
select * into result from rpm.rpmstrcmp('1.2.0~b7-1', '1.2.0~b6-1');
if result <> 1
then
	raise notice 'rpm.rpmstrcmp(1.2.0~b7-1, 1.2.0~b6-1) should be 1';
end if;
select * into result from rpm.rpmstrcmp('1.011-1', '1.06-2');
if result <> 1
then
	raise notice 'rpm.rpmstrcmp(1.011-1, 1.06-2) should be 1';
end if;
select * into result from rpm.rpmstrcmp('0.0.9+dfsg1-1', '0.0.8+dfsg1-3');
if result <> 1
then
	raise notice 'rpm.rpmstrcmp(0.0.9+dfsg1-1, 0.0.8+dfsg1-3) should be 1';
end if;
select * into result from rpm.rpmstrcmp('4.6.99+svn6582-1', '4.6.99+svn6496-1');
if result <> 1
then
	raise notice 'rpm.rpmstrcmp(4.6.99+svn6582-1, 4.6.99+svn6496-1) should be 1';
end if;
select * into result from rpm.rpmstrcmp('53', '52');
if result <> 1
then
	raise notice 'rpm.rpmstrcmp(53, 52) should be 1';
end if;
select * into result from rpm.rpmstrcmp('0.9.9~pre122-1', '0.9.9~pre111-1');
if result <> 1
then
	raise notice 'rpm.rpmstrcmp(0.9.9~pre122-1, 0.9.9~pre111-1) should be 1';
end if;
select * into result from rpm.rpmstrcmp('2.3.2-2+lenny2', '2.3.2-2');
if result <> 1
then
	raise notice 'rpm.rpmstrcmp(2.3.2-2+lenny2, 2.3.2-2) should be 1';
end if;
select * into result from rpm.rpmstrcmp('3.8.1-1', '3.8.GA-1');
if result <> 1
then
	raise notice 'rpm.rpmstrcmp(3.8.1-1, 3.8.GA-1) should be 1';
end if;
select * into result from rpm.rpmstrcmp('1.0.1+gpl-1', '1.0.1-2');
if result <> 1
then
	raise notice 'rpm.rpmstrcmp(1.0.1+gpl-1, 1.0.1-2) should be 1';
end if;
select * into result from rpm.rpmstrcmp('1a', '1000a');
if result <> -1
then
	raise notice 'rpm.rpmstrcmp(1a, 1000a) should be -1';
end if;
select * into result from rpm.rpmstrcmp('3.1-20170329', '3.1-20150325');
if result <> 1
then
	raise notice 'rpm.rpmstrcmp('3.1-20170329', '3.1-20150325') should be 1';
end if;
select * into result from rpm.rpmstrcmp('1.27+1.3.9', '1.27.1ubuntu1+1.3.9');
if result <> -1
then
	raise notice 'rpm.rpmstrcmp('1.27+1.3.9', '1.27.1ubuntu1+1.3.9') should be -1';
end if;
select * into result from rpm.rpmstrcmp('1.27+1.3.9', '1.27.1+1.3.9');
if result <> -1
then
	raise notice 'rpm.rpmstrcmp('1.27+1.3.9', '1.27.1+1.3.9') should be -1';
end if;
end;
$$
