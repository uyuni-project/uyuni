declare
  data_bunch_id number := 0;
begin
  begin
      SELECT id INTO data_bunch_id FROM rhnTaskoBunch WHERE name='cleanup-data-bunch';
  exception
    WHEN no_data_found THEN

      INSERT INTO rhnTaskoBunch (id, name, description, org_bunch)
      VALUES (sequence_nextval('rhn_tasko_bunch_id_seq'), 'cleanup-data-bunch', 'Cleans up orphaned and outdated data', null);

      commit;
  end;
  INSERT INTO rhnTaskoTemplate (id, bunch_id, task_id, ordering, start_if)
    VALUES (sequence_nextval('rhn_tasko_template_id_seq'),
        (SELECT id FROM rhnTaskoBunch WHERE name='cleanup-data-bunch'),
        (SELECT id FROM rhnTaskoTask WHERE name='cleanup-packagechangelog-data'),
        1,
        null);

end;
/

