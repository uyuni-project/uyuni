include:
  - channels

{%- if grains['os_family'] == 'Suse' %}
mgr_keep_system_up2date_updatestack:
  cmd.run:
    - name: /usr/bin/zypper --non-interactive patch --updatestack-only
    - success_retcodes:
      - 104
      - 103
      - 106
      - 0
    - onlyif: '/usr/bin/zypper patch-check --updatestack-only; r=$?; /usr/bin/test $r -eq 100 || /usr/bin/test $r -eq 101'
    - require:
      - sls: channels

{% set patch_need_reboot = salt['cmd.retcode']('/usr/bin/zypper -x list-patches | /usr/bin/grep \'restart="true"\' > /dev/null', python_shell=True) %}

{% else %}

mgr_keep_system_up2date_updatestack:
  pkg.latest:
    - pkgs:
{%- if salt['pkg.version']('venv-salt-minion') %}
      - venv-salt-minion
{%- else %}
      - salt-minion
{%- endif %}
{%- if grains['os_family'] == 'RedHat' %}
{%- if grains['osmajorrelease'] >= 8 %}
      - dnf
{%- else %}
      - yum
{%- endif %}
{%- elif grains.os_family == 'Debian' %}
      - apt
{%- endif %}
    - require:
      - sls: channels
{%- endif %}

mgr_keep_system_up2date_pkgs:
  pkg.uptodate:
    - refresh: True
    - require:
      - sls: channels
      - mgr_keep_system_up2date_updatestack

{%- if salt['pillar.get']('mgr_reboot_if_needed', True) and salt['pillar.get']('custom_info:mgr_reboot_if_needed', 'true')|lower in ('true', '1', 'yes', 't') %}
mgr_reboot_if_needed:
  cmd.run:
    - name: /usr/sbin/shutdown -r +5
    - require:
      - pkg: mgr_keep_system_up2date_pkgs
{%- if grains['os_family'] == 'RedHat' and grains['osmajorrelease'] >= 8 %}
    - onlyif: '/usr/bin/dnf -q needs-restarting -r; [ $? -eq 1 ]'
{%- elif grains['os_family'] == 'RedHat' and grains['osmajorrelease'] >= 7 %}
    - onlyif: '/usr/bin/needs-restarting -r; [ $? -eq 1 ]'
{%- elif grains['os_family'] == 'Debian' %}
    - onlyif:
      - /usr/bin/test -e /var/run/reboot-required
{%- elif grains['os_family'] == 'Suse' and grains['osmajorrelease'] <= 12 %}
    - onlyif:
      - /usr/bin/test -e /boot/do_purge_kernels
{%- else %}
    - onlyif: '/usr/bin/zypper ps -s; /usr/bin/test $? -eq 102 || /usr/bin/test {{ patch_need_reboot }} -eq 0 '
{%- endif %}
{%- endif %}
