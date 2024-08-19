include:
  - channels

{%- if grains['os_family'] == 'Suse' %}
mgr_keep_system_up2date_updatestack:
  cmd.run:
    - name: zypper --non-interactive patch --updatestack-only
    - success_retcodes:
      - 104
      - 103
      - 106
      - 0
    - onlyif: 'zypper patch-check --updatestack-only; r=$?; test $r -eq 100 || test $r -eq 101'
    - require:
      - sls: channels

{% set patch_need_reboot = salt['cmd.retcode']('zypper -x list-patches | grep \'restart="true"\' > /dev/null', python_shell=True) %}

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
