-- oracle equivalent source sha1 c56da5f96a376dc75deb6082bbd93d4340bb565d
-- retrieved from ./1234445323/8c9aab43b76cfe2b234425a270944019bb987884/schema/spacewalk/rhnsat/procs/lookup_feature_type.sql
--
-- Copyright (c) 2008--2013 Red Hat, Inc.
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
--
--

create or replace function
lookup_feature_type(label_in in varchar)
returns numeric 
as
$$
declare
	feature_id numeric;
begin
	select	id
	into	feature_id
	from  rhnFeature 	
	where	label = label_in;

	if not found then
            perform rhn_exception.raise_exception('invalid_feature');
	end if;

	return feature_id;
end;
$$
language plpgsql
stable;

