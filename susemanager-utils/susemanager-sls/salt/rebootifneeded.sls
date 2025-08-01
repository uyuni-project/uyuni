{%- if salt['pillar.get']('mgr_reboot_if_needed', True) and salt['pillar.get']('custom_info:mgr_reboot_if_needed', 'true')|lower in ('true', '1', 'yes', 't') %}
mgr_reboot_if_needed:
  cmd.run:
    - name: /usr/sbin/shutdown -r +5
{%- if grains['os_family'] == 'RedHat' and grains['osmajorrelease'] >= 8 %}
    - onlyif: '/usr/bin/dnf -q needs-restarting -r; /usr/bin/test $? -eq 1'
{%- elif grains['os_family'] == 'RedHat' and grains['osmajorrelease'] >= 7 %}
    - onlyif: '/usr/bin/needs-restarting -r; /usr/bin/test $? -eq 1'
{%- elif grains['os_family'] == 'Debian' %}
    - onlyif:
      - /usr/bin/test -e /var/run/reboot-required
{%- elif grains['os_family'] == 'Suse' and grains['osmajorrelease'] <= 12 %}
    - onlyif:
      - /usr/bin/test -e /boot/do_purge_kernels
{%- else %}
    - onlyif: '/usr/bin/zypper ps -s; [ $? -eq 102 ]'
{%- endif %}
{%- endif %}
