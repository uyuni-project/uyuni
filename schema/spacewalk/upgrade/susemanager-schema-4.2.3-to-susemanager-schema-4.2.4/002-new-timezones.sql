insert into rhnTimezone(id, olson_name, display_name) ( 
  select sequence_nextval('rhn_timezone_id_seq'), 'Asia/Jerusalem', 'Israel'
    from dual
   where not exists (
	select 1 from rhnTimezone where olson_name = 'Asia/Jerusalem'
   )
);

insert into rhnTimezone(id, olson_name, display_name) ( 
  select sequence_nextval('rhn_timezone_id_seq'), 'Asia/Singapore', 'Singapore'
    from dual
   where not exists (
	select 1 from rhnTimezone where olson_name = 'Asia/Singapore'
   )
);

insert into rhnTimezone(id, olson_name, display_name) ( 
  select sequence_nextval('rhn_timezone_id_seq'), 'Asia/Kuala_Lumpur', 'Malaysia'
    from dual
   where not exists (
	select 1 from rhnTimezone where olson_name = 'Asia/Kuala_Lumpur'
   )
);

