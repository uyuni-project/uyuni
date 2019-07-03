-- oracle equivalent source sha1 c6b1c2215f7624c1f08cd54ca960f2bb1ff98915
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
        -- HACK: insert is isolated in own function in order to be able to declare this function immutable
        -- Postgres optimizes immutable functions calls but those are compatible with the contract of lookup_\*
        -- see https://www.postgresql.org/docs/9.6/xfunc-volatility.html
        return insert_md_keyword(label_in);
    end if;

    return md_keyword_id;
end;
$$ language plpgsql immutable;
