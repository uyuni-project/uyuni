insert into rhnRepoRegenQueue (id, CHANNEL_LABEL, REASON, FORCE)
(select sequence_nextval('rhn_repo_regen_queue_id_seq'),
        C.label,
        'fix names for cloned patches',
        'Y'
   from rhnChannel C);
