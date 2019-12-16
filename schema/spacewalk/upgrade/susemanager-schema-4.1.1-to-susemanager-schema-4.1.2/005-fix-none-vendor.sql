delete from rhnpackagerepodata where package_id in (select id from rhnpackage where vendor = 'Not defined');

ALTER TABLE rhnpackage ALTER COLUMN vendor DROP NOT NULL;

update rhnpackage set vendor = NULL where vendor = 'Not defined';

insert into rhnRepoRegenQueue (id, CHANNEL_LABEL, REASON, FORCE)
(select sequence_nextval('rhn_repo_regen_queue_id_seq'),
        C.label,
        'fix empty vendor',
        'Y'
   from rhnChannel C
   where C.id in (select distinct cp.channel_id
                    from rhnpackage p
	            join rhnchannelpackage cp on p.id = cp.package_id
	           where p.vendor is NULL));
