insert into rhnTimezone (id, olson_name, display_name) (
    select sequence_nextval('rhn_timezone_id_seq'),
           'Australia/Adelaide',
           'Australia Central (Adelaide)'
      from dual
     where not exists (
           select 1
             from rhnTimezone
            where olson_name = 'Australia/Adelaide'
              and display_name = 'Australia Central (Adelaide)'
     )
);
