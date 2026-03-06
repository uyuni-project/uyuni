#!/bin/bash
# SPDX-FileCopyrightText: 2025 SUSE LLC
#
# SPDX-License-Identifier: Apache-2.0

run_sql() {
  psql -v ON_ERROR_STOP=1 \
    -h localhost \
    -p "${PGPORT:-5432}" \
    -U "$POSTGRES_USER" \
    --no-password --no-psqlrc -d susemanager "$@"
}

cat << EOF | run_sql
do \$\$ begin
create type evr_t as (
        epoch           varchar(16),
        version         varchar(512),
        release         varchar(512),
        type            varchar(10)
);
exception when duplicate_object then null;
end \$\$;

create or replace function evr_t(e varchar, v varchar, r varchar, t varchar)
returns evr_t as \$\$
select row(\$1,\$2,\$3,\$4)::evr_t
\$\$ language sql;

create or replace function evr_t_compare( a evr_t, b evr_t )
returns int as \$\$
begin
  if a.type = b.type then
      if a.type = 'rpm' then
        return rpm.vercmp(a.epoch, a.version, a.release, b.epoch, b.version, b.release);
      elsif a.type = 'deb' then
        return deb.debvercmp(a.epoch, a.version, a.release, b.epoch, b.version, b.release);
      else
        raise EXCEPTION 'unknown evr type (using rpm) -> %', a.type;
      end if;
  else
     raise NOTICE 'comparing incompatible evr types. Using %', a.type;
     if a.type = 'deb' then
       return -1;
     else
       return 1;
     end if;
  end if;
end;
\$\$ language plpgsql immutable strict;

create or replace function evr_t_lt( a evr_t, b evr_t )
returns boolean as \$\$
begin
  return evr_t_compare( a, b ) < 0;
end;
\$\$ language plpgsql immutable strict;

create or replace function evr_t_le( a evr_t, b evr_t )
returns boolean as \$\$
begin
  return evr_t_compare( a, b ) <= 0;
end;
\$\$ language plpgsql immutable strict;

create or replace function evr_t_eq( a evr_t, b evr_t )
returns boolean as \$\$
begin
  return evr_t_compare( a, b ) = 0;
end;
\$\$ language plpgsql immutable strict;

create or replace function evr_t_ne( a evr_t, b evr_t )
returns boolean as \$\$
begin
  return evr_t_compare( a, b ) != 0;
end;
\$\$ language plpgsql immutable strict;

create or replace function evr_t_ge( a evr_t, b evr_t )
returns boolean as \$\$
begin
  return evr_t_compare( a, b ) >= 0;
end;
\$\$ language plpgsql immutable strict;

create or replace function evr_t_gt( a evr_t, b evr_t )
returns boolean as \$\$
begin
  return evr_t_compare( a, b ) > 0;
end;
\$\$ language plpgsql immutable strict;

-- operators: idempotent via do blocks
do \$\$ begin
create operator < (
  leftarg = evr_t,
  rightarg = evr_t,
  procedure = evr_t_lt,
  commutator = >,
  negator = >=,
  restrict = scalarltsel,
  join = scalarltjoinsel
);
exception when duplicate_object or duplicate_function then null;
end \$\$;

do \$\$ begin
create operator <= (
  leftarg = evr_t,
  rightarg = evr_t,
  procedure = evr_t_le,
  commutator = >=,
  negator = >,
  restrict = scalarltsel,
  join = scalarltjoinsel
);
exception when duplicate_object or duplicate_function then null;
end \$\$;

do \$\$ begin
create operator = (
  leftarg = evr_t,
  rightarg = evr_t,
  procedure = evr_t_eq,
  commutator = =,
  negator = <>,
  restrict = eqsel,
  join = eqjoinsel
);
exception when duplicate_object or duplicate_function then null;
end \$\$;

do \$\$ begin
create operator >= (
  leftarg = evr_t,
  rightarg = evr_t,
  procedure = evr_t_ge,
  commutator = <=,
  negator = <,
  restrict = scalargtsel,
  join = scalargtjoinsel
);
exception when duplicate_object or duplicate_function then null;
end \$\$;

do \$\$ begin
create operator > (
  leftarg = evr_t,
  rightarg = evr_t,
  procedure = evr_t_gt,
  commutator = <,
  negator = <=,
  restrict = scalargtsel,
  join = scalargtjoinsel
);
exception when duplicate_object or duplicate_function then null;
end \$\$;

do \$\$ begin
create operator <> (
  leftarg = evr_t,
  rightarg = evr_t,
  procedure = evr_t_ne,
  commutator = <>,
  negator = =,
  restrict = eqsel,
  join = eqjoinsel
);
exception when duplicate_object or duplicate_function then null;
end \$\$;

do \$\$ begin
create operator class evr_t_ops
default for type evr_t using btree as
  operator 1 <,
  operator 2 <=,
  operator 3 =,
  operator 4 >=,
  operator 5 >,
  function 1 evr_t_compare( evr_t, evr_t )
;
exception when duplicate_object or duplicate_function then null;
end \$\$;

create or replace function evr_t_as_vre( a evr_t ) returns varchar as \$\$
begin
  return a.version || '-' || a.release || ':' || coalesce(a.epoch, '');
end;
\$\$ language plpgsql immutable strict;

create or replace function evr_t_as_vre_simple( a evr_t ) returns varchar as \$\$
declare
  vre_out VARCHAR(256);
begin
  vre_out := a.version || '-' || a.release;
  if a.epoch is not null
  then
    vre_out := vre_out || ':' || a.epoch;
  end if;
  return vre_out;
end;
\$\$ language plpgsql immutable strict;

create or replace function evr_t_larger(a evr_t, b evr_t)
returns evr_t
as \$\$
begin
  if a > b
  then
    return a;
  else
    return b;
  end if;
end;
\$\$ language plpgsql immutable strict;

do \$\$ begin
create aggregate max (
  sfunc=evr_t_larger,
  basetype=evr_t,
  stype=evr_t
);
exception when duplicate_object or duplicate_function then null;
end \$\$;
EOF
