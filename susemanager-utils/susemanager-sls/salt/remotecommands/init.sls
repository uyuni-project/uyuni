remote_command:
  cmd.script:
    - source: {{ pillar.get('mgr_remote_cmd_script') }}
    - runas: {{ pillar.get('mgr_remote_cmd_runas', 'root') }}
    - timeout: {{ pillar.get('mgr_remote_cmd_timeout') }}
    # TODO GID

