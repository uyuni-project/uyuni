INSERT INTO rhnRepoRegenQueue (id, channel_label, reason, force)
(SELECT sequence_nextval('rhn_repo_regen_queue_id_seq'),
        C.label,
        'Regenerate Release as flat',
        'Y'
 FROM rhnChannel AS C
     LEFT JOIN rhnChannelArch AS CA ON
         (CA.id=C.channel_arch_id)
 WHERE CA.label LIKE '%-deb');
