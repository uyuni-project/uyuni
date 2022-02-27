insert into rhnTimezone(id, olson_name, display_name) ( 
  select sequence_nextval('rhn_timezone_id_seq'), 'Asia/Beijing', 'China'
    from dual
   where not exists (
	select 1 from rhnTimezone where display_name = 'China'
   )
);
