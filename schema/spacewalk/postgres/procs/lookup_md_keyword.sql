-- oracle equivalent source sha1 e65440cb0d37aca3e609e9812817c53ad751cb29
--
-- Copyright (c) 2012 Novell
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--

create or replace function
lookup_md_keyword(label_in in varchar)
returns numeric
as
$$
declare
    md_keyword_id numeric;
begin
    select id
      into md_keyword_id
      from suseMdKeyword
     where label = label_in;
    if not found then
        md_keyword_id := nextval('suse_mdkeyword_id_seq');
        begin
            perform pg_dblink_exec(
                'insert into suseMdKeyword (id, label) values (' ||
                md_keyword_id || ', ' || coalesce(quote_literal(label_in)) || ')');
        exception when unique_violation then
            select id
              into strict md_keyword_id
              from suseMdKeyword
             where label = label_in;
        end;
    end if;

    return md_keyword_id;
end;
$$ language plpgsql immutable;
