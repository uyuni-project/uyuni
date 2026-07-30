{%- if grains.get('transactional', False) %}
snapper-list-snapshots:
  cmd.run:
    - name: snapper --json --no-dbus list
{%- endif %}
