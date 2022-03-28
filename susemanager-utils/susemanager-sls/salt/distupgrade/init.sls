{% if grains['os_family'] == 'Suse' %}
spmigration:
  mgrcompat.module_run:
    - name: pkg.upgrade
    - dist_upgrade: True
    - dryrun: {{ salt['pillar.get']('susemanager:distupgrade:dryrun', False) }}
{% if grains['osrelease_info'][0] >= 12 %}
    - novendorchange: {{ not salt['pillar.get']('susemanager:distupgrade:allow_vendor_change', False) }}
{% else %}
    - fromrepo: {{ salt['pillar.get']('susemanager:distupgrade:channels', []) }}
{% endif %}
    -   require:
        - file: mgrchannels*
{% endif %}

{% if not salt['pillar.get']('susemanager:distupgrade:dryrun') %}
{% if pillar.get('missing_successors', [])%}
pkg_removed:
  pkg.removed:
    -   pkgs:
{%- for missing_successor in pillar.get('missing_successors', [])%}
        - {{missing_successor}}-release
{%- endfor %}
{% endif %}
{% endif %}

include:
  - channels
