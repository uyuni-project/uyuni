remote_command:
  cmd.run:
    - name: {{ pillar.get('mgr_remote_command') }}