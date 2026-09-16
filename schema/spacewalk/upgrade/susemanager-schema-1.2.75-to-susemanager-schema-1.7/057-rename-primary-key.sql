-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

declare
  cname varchar2(30) := '';
begin
    select constraint_name
      into cname
      from user_constraints
     where table_name = 'RHNACTIONIMAGEDEPLOY' and constraint_type = 'P';
    if cname like 'SYS%' then
        execute immediate 'alter table RHNACTIONIMAGEDEPLOY rename constraint ' || cname || ' to rhn_aid_id_pk';
    end if;
end;
/
