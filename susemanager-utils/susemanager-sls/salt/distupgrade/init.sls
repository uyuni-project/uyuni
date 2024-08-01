{% if grains['os_family'] == 'Suse' %}
spmigration:
  mgrcompat.module_run:
    - name: pkg.upgrade
    - dist_upgrade: True
    - dryrun: {{ salt['pillar.get']('susemanager:distupgrade:dryrun', False) }}
{% if grains['osrelease_info'][0] >= 12 or grains['transactional'] == True %}
    - novendorchange: {{ not salt['pillar.get']('susemanager:distupgrade:allow_vendor_change', False) }}
{% else %}
    - fromrepo: {{ salt['pillar.get']('susemanager:distupgrade:channels', []) }}
{% endif %}
    -   require:
        - file: mgrchannels*
{% elif grains['os_family'] == 'RedHat' %}
{% if not salt['pillar.get']('susemanager:distupgrade:dryrun', False) %}
{# if pillar liberate:reinstall_packages is not set, it default to true. This is the default we want #}
{% include 'liberate/init.sls' %}

{% set logname='/var/log/dnf_sll_migration.log' %}
{% if grains['osrelease_info'][0] == 7 %}
{%   set logname='/var/log/yum_sles_es_migration.log' %}
{% elif grains['osrelease_info'][0] == 8 %}
{%   set logname='/var/log/dnf_sles_es_migration.log' %}
{% endif %}

spmigration:
  cmd.run:
    - name: cat {{ logname }}
    - require:
      - cmd: re_install_from_SLL

{% endif %}
{% endif %}

{% if not salt['pillar.get']('susemanager:distupgrade:dryrun') %}
{% if pillar.get('missing_successors', [])%}
mgr_release_pkg_removed:
  pkg.removed:
    -   pkgs:
{%- for missing_successor in pillar.get('missing_successors', [])%}
        - {{missing_successor}}-release
{%- endfor %}
{% endif %}
{% endif %}

include:
  - channels
