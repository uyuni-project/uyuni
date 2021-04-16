create or replace function suse_srv_inst_prod_iud_trig_fun() returns trigger as
$$
begin
        if tg_op='INSERT' or tg_op='UPDATE' then
                update suseSCCRegCache
                   set scc_reg_required = 'Y'
                 where server_id = new.rhn_server_id;
                return new;
        end if;
        if tg_op='DELETE' then
                update suseSCCRegCache
                   set scc_reg_required = 'Y'
                 where server_id = old.rhn_server_id;
                return old;
        end if;
end;
$$ language plpgsql;

drop trigger if exists susesrvinstprod_iud_trig on suseServerInstalledProduct;
create trigger
susesrvinstprod_iud_trig
after insert or update or delete on suseServerInstalledProduct
for each row
execute procedure suse_srv_inst_prod_iud_trig_fun();

