-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

create or replace trigger
rhn_srv_net_iface_mod_trig
before insert or update on rhnServerNetInterface
for each row
begin
		if :new.id is null then
	        select rhn_srv_net_iface_id_seq.nextval into :new.id from dual;
	    end if;
        :new.modified := sysdate;
end;
/
show errors
