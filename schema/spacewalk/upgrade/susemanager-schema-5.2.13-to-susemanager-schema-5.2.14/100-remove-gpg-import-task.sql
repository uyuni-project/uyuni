DELETE FROM rhnTaskoRun
    WHERE template_id IN (SELECT id FROM rhnTaskoTemplate
        WHERE bunch_id = (SELECT id FROM rhnTaskoBunch WHERE name = 'custom-gpg-key-import-bunch')
    );

DELETE FROM rhnTaskoTemplate
    WHERE bunch_id = (SELECT id FROM rhnTaskoBunch WHERE name = 'custom-gpg-key-import-bunch');

DELETE FROM rhnTaskoTask
    WHERE name = 'custom-gpg-key-import' AND class = 'com.redhat.rhn.taskomatic.task.GpgImportTask';

DELETE FROM rhnTaskoBunch
    WHERE name = 'custom-gpg-key-import-bunch';
