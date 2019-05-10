    create or replace function entitle_server (
        server_id_in in numeric,
        type_label_in in varchar
    ) returns void
as $$
    declare
      sgid  numeric := 0;

    begin

      if rhn_entitlements.can_entitle_server(server_id_in,
                                             type_label_in) = 1 then
         sgid := rhn_entitlements.find_compatible_sg (server_id_in,
                                                      type_label_in);
         if sgid is not null then
            insert into rhnServerHistory ( id, server_id, summary, details )
            values ( nextval('rhn_event_id_seq'), server_id_in,
                     'added system entitlement ',
                      case type_label_in
                       when 'enterprise_entitled' then 'Management'
                       when 'bootstrap_entitled' then 'Bootstrap'
                       when 'salt_entitled' then 'Salt'
                       when 'foreign_entitled' then 'Foreign'
                       when 'virtualization_host' then 'Virtualization'
                       when 'container_build_host' then 'Container'
                       when 'osimage_build_host' then 'OS Image'
                       when 'monitoring_entitled' then 'Monitoring'
                      end  );

            perform rhn_server.insert_into_servergroup (server_id_in, sgid);

         else
            perform rhn_exception.raise_exception ('no_available_server_group');
         end if;
      else
         perform rhn_exception.raise_exception ('invalid_entitlement');
      end if;
   end$$
language plpgsql;