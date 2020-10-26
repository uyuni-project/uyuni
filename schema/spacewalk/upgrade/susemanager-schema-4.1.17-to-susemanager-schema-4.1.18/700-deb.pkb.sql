-- oracle equivalent source sha1 539cb03eb177b7e87992701071488bbb32bb0624
create schema if not exists deb;

--update pg_setting
update pg_settings set setting = 'deb,' || setting where name = 'search_path';

-- Debian version comparison
-- See: https://www.debian.org/doc/debian-policy/ch-controlfields.html#version
-- See: https://salsa.debian.org/dpkg-team/dpkg/blob/master/lib/dpkg/version.c#L140

CREATE OR REPLACE FUNCTION lastIndexOf(needle text, haystack text)
RETURNS integer AS $$
    DECLARE
        rc INTEGER;
    BEGIN
     rc := length(haystack) - position(needle in reverse(haystack));
     if rc = length(haystack) then rc = -1; end if;
     return rc;
    END;
$$ language 'plpgsql';

create or replace function isdigit(ch CHAR)
    RETURNS BOOLEAN as $$
    BEGIN
        if ascii(ch) between ascii('0') and ascii('9')
        then
            return TRUE;
        end if;
        return FALSE;
    END ;
$$ language 'plpgsql';


create or replace FUNCTION isalpha(ch CHAR)
    RETURNS BOOLEAN as $$
    BEGIN
        if ascii(ch) between ascii('a') and ascii('z') or
            ascii(ch) between ascii('A') and ascii('Z')
        then
            return TRUE;
        end if;
        return FALSE;
    END;
$$ language 'plpgsql';

create or replace FUNCTION charAt(str IN VARCHAR, pos IN INTEGER)
    RETURNS CHARACTER as $$
    BEGIN
        return SUBSTR(str, pos + 1, 1);
    END;
$$ language 'plpgsql';

create or replace function deborder(c INTEGER)
    RETURNS INTEGER as $$
    BEGIN
        if deb.isdigit(chr(c)) then return 0; end if;
        if deb.isalpha(chr(c)) then return c; end if;
        if c = ascii('~') then return -1; end if;
        if c != 0 then return c + 256; end if;
        return 0;
    END ;
$$ language 'plpgsql';

create or replace FUNCTION verrevcmp(a1 IN VARCHAR, b1 IN VARCHAR)
    RETURNS INTEGER as $$
    DECLARE
        a VARCHAR;
        b VARCHAR;
        i INTEGER := 0;
        j INTEGER := 0;
    BEGIN
        IF a1 IS NULL then a := ''; end if;
        IF b1 IS NULL then b := ''; end if;
        a := a1;
        b := b1;
        WHILE (i < LENGTH(a)) or (j < LENGTH(b))
        LOOP
            DECLARE
                firstDiff INTEGER := 0;
            BEGIN
                while ((i < LENGTH(a)) and not deb.isdigit(deb.charAt(a,i))) or ((j < LENGTH(b)) and not deb.isdigit(deb.charAt(b,j)))
                LOOP
                DECLARE
                    ac INTEGER;
                    bc INTEGER;
                BEGIN
                    if i >= length(a)
                    then
                        ac := 0;
                    else
                        ac := deb.deborder(ascii(deb.charAt(a, i)));
                    end if;
                    if j >= length(b)
                    then
                        bc := 0;
                    else
                        bc := deb.deborder(ascii(deb.charAt(b, j)));
                    end if;
                    if ac != bc then return ac-bc; end if;
                    i := i + 1;
                    j := j + 1;
                END;
                END LOOP;
                while (i < length(a)) and (deb.charAt(a, i) = '0')
                LOOP
                    i := i + 1;
                END LOOP;
                while (j < length(b)) and (deb.charAt(b, j) = '0')
                LOOP
                    j := j + 1;
                END LOOP;
                WHILE (i < LENGTH(a)) and (j < LENGTH(b)) and (deb.isdigit(deb.charAt(a,i))) and (deb.isdigit(deb.charAt(b, j)))
                LOOP
                    if firstDiff = 0 then firstDiff := ascii(deb.charAt(a, i)) - ascii(deb.charAt(b, j)); end if;
                    i := i + 1;
                    j := j + 1;
                END LOOP;
                IF (i < LENGTH(a)) and (deb.isdigit(deb.charAt(a,i))) then return 1; end if;
                IF (j < LENGTH(b)) and (deb.isdigit(deb.charAt(b,j))) then return -1; end if;
                IF firstDiff != 0 then return firstDiff; end if;
            END;
        END LOOP;
        RETURN 0;
    END;
$$ language 'plpgsql';

create or replace FUNCTION debstrcmp (o1 IN VARCHAR, o2 IN VARCHAR)
    RETURNS INTEGER as $$
    declare
        version1 VARCHAR := o1;
        version2 VARCHAR := o2;
        revision1 VARCHAR;
        revision2 VARCHAR;
        rc INTEGER;
        rv INTEGER;
    BEGIN
        IF deb.lastIndexOf('-', version1) > 0
        then
            revision1 := SUBSTR(version1, POSITION('-' IN version1) + 1);
            version1 := SUBSTR(version1, 0, POSITION('-' IN version1));
        end if;
        IF deb.lastIndexOf('-',version2) > 0
        then
            revision2 := SUBSTR(version2, POSITION('-' IN version2) + 1);
            version2 := SUBSTR(version2, 0, POSITION('-' IN version2));
        end if;
        rc := deb.verrevcmp(version1, version2);
        if rc > 0 then return 1; end if;
        if rc < 0 then return -1; end if;
        if rc = 0
        then
            rv := deb.verrevcmp(revision1, revision2);
            if rv > 0 then return 1; end if;
            if rv < 0 then return -1; end if;
            return 0;
        end if;
    END;
$$ language 'plpgsql';

create or replace FUNCTION debvercmp(
        e1 VARCHAR, v1 VARCHAR, r1 VARCHAR,
        e2 VARCHAR, v2 VARCHAR, r2 VARCHAR)
    RETURNS INTEGER as $$
    declare
        rc INTEGER;
          ep1 INTEGER;
          ep2 INTEGER;
          BEGIN
            if e1 is null or e1 = '' then
              ep1 := 0;
            else
              ep1 := e1::integer;
            end if;
            if e2 is null or e2 = '' then
              ep2 := 0;
            else
              ep2 := e2::integer;
            end if;
            -- Epochs are non-null; compare them
            if ep1 < ep2 then return -1; end if;
            if ep1 > ep2 then return 1; end if;
            rc := deb.debstrcmp(v1, v2);
            if rc != 0 then return rc; end if;
           return deb.debstrcmp(r1, r2);
         END;
         $$ language 'plpgsql';

-- restore the original setting
update pg_settings set setting = overlay( setting placing '' from 1 for (length('deb')+1) ) where name = 'search_path';
