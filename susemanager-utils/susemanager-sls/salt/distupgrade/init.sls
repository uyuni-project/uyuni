{% if grains['os_family'] == 'Suse' %}
spmigration:
  module.run:
    - name: pkg.upgrade
    - dist_upgrade: True
    - dryrun: {{ salt['pillar.get']('susemanager:distupgrade:dryrun', False) }}
{% if grains['osrelease_info'][0] >= 12 %}
    - novendorchange: True
{% else %}
    - fromrepo: {{ salt['pillar.get']('susemanager:distupgrade:channels', []) }}
{% endif %}
    -   require:
        - module: applychannels
{% endif %}

applychannels:
    module.run:
    -  name: state.apply
    -  mods: channels
