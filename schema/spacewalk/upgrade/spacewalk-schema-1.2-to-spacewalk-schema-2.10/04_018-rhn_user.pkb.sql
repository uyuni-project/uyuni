--
-- Copyright (c) 2012 Red Hat, Inc.
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
-- 
-- Red Hat trademarks are not licensed under GPLv2. No permission is
-- granted to use or replicate Red Hat trademarks that are incorporated
-- in this software or its documentation. 
--

-- create schema rhn_user;

-- setup search_path so that these functions are created in appropriate schema.
update pg_settings set setting = 'rhn_user,' || setting where name = 'search_path';

create function role_names (user_id_in in numeric)
	returns varchar
	as
$$
	declare
		rec record;
		tmp varchar(4000);
	begin
		for rec in (
			select type_name
			from rhnUserTypeBase
			where user_id = user_id_in
			order by type_id
			) loop
			if tmp is null then
				tmp := rec.type_name;
			else
				tmp := tmp || ', ' || rec.type_name;
			end if;
		end loop;
		return tmp;
	end;
$$ language plpgsql;

-- restore the original setting
update pg_settings set setting = overlay( setting placing '' from 1 for (length('rhn_user')+1) ) where name = 'search_path';
