{#
    `cwd` defines where the script is written to (temporarily) and from where it's executed.
    Users can define `mgr_remote_cmd_cwd` in pillar data to avoid writing the script to
    /tmp if that's required, for example when /tmp is mounted with noexec
#}
{%- set cwd = pillar.get('mgr_remote_cmd_cwd') %}

remote_command:
  cmd.script:
    - source: {{ pillar.get('mgr_remote_cmd_script') }}
    - runas: {{ pillar.get('mgr_remote_cmd_runas', 'root') }}
    - timeout: {{ pillar.get('mgr_remote_cmd_timeout') }}
    {%- if cwd %}
    - cwd: {{ cwd }}
    {%- endif %}
    # TODO GID

