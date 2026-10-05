-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

insert into valid_countries(code,short_name,name) ( 
  select 'RS', 'Serbia', 'Republic of Serbia' from dual
   where not exists (
	select 1 from valid_countries where code = 'RS'
   )
);

