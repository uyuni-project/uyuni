{%- if grains['os_family'] == 'Suse' %}

dump_uptime_info:
  cmd.run:
    - name: "cat /etc/zypp/suse-uptime.log | sed -e '/^$/d;s/^/\"/;s/$/\",/' | tr -d '\n' | sed 's/^/[/;s/,$/]/'"
    - onlyif: test -s /etc/zypp/suse-uptime.log

{%- endif %}
