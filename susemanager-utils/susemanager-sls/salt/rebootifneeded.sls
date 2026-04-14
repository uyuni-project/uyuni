{%- if salt['pillar.get']('mgr_reboot_if_needed', True) and salt['pillar.get']('custom_info:mgr_reboot_if_needed', 'true')|lower in ('true', '1', 'yes', 't') %}
mgr_reboot_if_needed:
  cmd.run:
    - name: command -p shutdown -r +5
{%- if grains['os_family'] == 'RedHat' %}
    - onlyif: 'command -p needs-restarting -r; [ $? -eq 1 ]'
{%- elif grains['os_family'] == 'Debian' %}
    - onlyif:
      - command -p test -e /var/run/reboot-required
{%- elif grains.get('transactional', False) and grains['os_family'] == 'Suse' %}
    - onlyif:
      - command -p snapper list --columns number 2>/dev/null | command -p grep '+'
{%- elif grains['os_family'] == 'Suse' and grains['osmajorrelease'] <= 12 %}
    - onlyif:
      - command -p test -e /boot/do_purge_kernels
{%- else %}
    - onlyif: 'command -p zypper ps -s; [ $? -eq 102 ]'
{%- endif %}
{%- endif %}
