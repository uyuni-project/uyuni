include:
  - channels

mgr_keep_system_up2date_updatestack:
  pkg.latest:
    - pkgs:
{%- if salt['pkg.version']('venv-salt-minion') %}
      - venv-salt-minion
{%- else %}
      - salt-minion
{%- endif %}
{%- if grains.os_family == 'Suse' %}
      - zypper
      - libzypp
{%- elif grains['os_family'] == 'RedHat' %}
{%- if grains['osmajorrelease'] >= 8 %}
      - dnf
{%- else %}
      - yum
{%- endif %}
{%- else %}
      - apt
{%- endif %}
    - require:
      - sls: channels

mgr_keep_system_up2date_pkgs:
  pkg.uptodate:
    - refresh: True
    - require:
      - sls: channels
      - pkg: mgr_keep_system_up2date_updatestack

mgr_reboot_if_needed:
  cmd.run:
    - name: shutdown -r +5
{%- if grains['os_family'] == 'RedHat' and grains['osmajorrelease'] >= 8 %}
    - onlyif: 'dnf -q needs-restarting -r; [ $? -eq 1 ]'
{%- elif grains['os_family'] == 'RedHat' and grains['osmajorrelease'] >= 7 %}
    - onlyif: 'needs-restarting -r; [ $? -eq 1 ]'
{%- elif grains['os_family'] == 'Debian' %}
    - onlyif:
      - test -e /var/run/reboot-required
{%- elif grains['os_family'] == 'Suse' and grains['osmajorrelease'] <= 12 %}
    - onlyif:
      - test -e /boot/do_purge_kernels
{%- else %}
    - onlyif: 'zypper ps -s; [ $? -eq 102 ]'
{%- endif %}
