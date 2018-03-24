remote_command:
  cmd.script:
    - source: {{ pillar.get('mgr_remote_cmd_script') }}
    - runas: {{ pillar.get('mgr_remote_cmd_runas', 'root') }}
    # TODO GID