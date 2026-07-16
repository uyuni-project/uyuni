{%- if grains.get('transactional', False) %}
snapper-list-snapshots:
  cmd.run:
    - name: snapper --json --no-dbus list

get-active-snapshot:
  cmd.run:
    - name: "awk '$5==\"/\" {print $4}' /proc/1/mountinfo | grep -oP '\\.snapshots/\\K\\d+'"
{%- endif %}
