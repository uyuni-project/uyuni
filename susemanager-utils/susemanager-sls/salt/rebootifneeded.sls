{%- if salt['pillar.get']('mgr_reboot_if_needed', True) and salt['pillar.get']('custom_info:mgr_reboot_if_needed', 'true')|lower in ('true', '1', 'yes', 't') %}
mgr_reboot_if_needed:
  cmd.run:
{%- if grains['os_family'] == 'Suse' and grains['osmajorrelease'] <= 12 %}
    - name: /sbin/shutdown -r +5
{%- else %}
    - name: /usr/sbin/shutdown -r +5
{%- endif %}
{%- if grains['os_family'] == 'RedHat' and grains['osmajorrelease'] >= 8 %}
    - onlyif: '/usr/bin/dnf -q needs-restarting -r; /usr/bin/test $? -eq 1'
{%- elif grains['os_family'] == 'RedHat' and grains['osmajorrelease'] >= 7 %}
    - onlyif: '/usr/bin/needs-restarting -r; /usr/bin/test $? -eq 1'
{%- elif grains['os_family'] == 'Debian' %}
    - onlyif:
      - /usr/bin/test -e /var/run/reboot-required
{%- elif grains.get('transactional', False) and grains['os_family'] == 'Suse' %}
    - onlyif:
      - /usr/bin/snapper list --columns number 2>/dev/null | /usr/bin/grep '+'
{%- elif grains['os_family'] == 'Suse' and grains['osmajorrelease'] <= 12 %}
    - onlyif:
      - /usr/bin/test -e /boot/do_purge_kernels
{%- else %}
    - onlyif: '/usr/bin/zypper ps -s; [ $? -eq 102 ]'
{%- endif %}
{%- endif %}
