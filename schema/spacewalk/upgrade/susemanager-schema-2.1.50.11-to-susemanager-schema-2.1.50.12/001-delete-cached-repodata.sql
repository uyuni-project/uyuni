delete from rhnPackageRepodata;

insert into rhnRepoRegenQueue (id, CHANNEL_LABEL, REASON, FORCE)
(select sequence_nextval('rhn_repo_regen_queue_id_seq'),
        C.label,
        'fix double XML quoting',
        'Y'
   from rhnChannel C);
