-- Drop old stuff
delete from rhnContentSourceSsl;
drop table rhnContentSourceSsl;
drop function rhn_csssl_ins_trig_fun();
drop function rhn_cont_source_ssl_mod_trig_fun();
drop sequence rhn_contentsourcessl_seq;
