-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

insert into RHNINFOPANE(ID,LABEL,ACL) 
select sequence_nextval('rhn_info_pane_id_seq'),'subscription-warning', null  
where not exists( select label from RHNINFOPANE where label = 'subscription-warning');  
