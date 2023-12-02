insert into RHNINFOPANE(ID,LABEL,ACL) 
select sequence_nextval('rhn_info_pane_id_seq'),'subscription-warning', null  
where not exists( select label from RHNINFOPANE where label = 'subscription-warning');  
