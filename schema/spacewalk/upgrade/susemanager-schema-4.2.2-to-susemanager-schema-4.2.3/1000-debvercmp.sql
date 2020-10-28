alter type evr_t add attribute type varchar(10);
ALTER TABLE rhnpackageevr DISABLE TRIGGER USER

-- update evr_t comparison function to take type into account.
create or replace function evr_t_compare( a evr_t, b evr_t )
returns int as $$
begin
  if a.type = b.type then
      if a.type = 'rpm' then
        return rpm.vercmp(a.epoch, a.version, a.release, b.epoch, b.version, b.release);
      elsif a.type = 'deb' then
        return deb.debvercmp(a.epoch, a.version, a.release, b.epoch, b.version, b.release);
      else
        raise notice 'unknown evr type';
      end if;
  else
     raise notice 'cant compare incompatible evr types';
  end if;
end;
$$ language plpgsql immutable strict;


select pe.id, array_agg(distinct pa.label like '%-deb') from rhnpackagearch pa
             join rhnpackage p on p.package_arch_id = pa.id
             join rhnpackageevr pe on p.evr_id = pe.id
             group by pe.id;