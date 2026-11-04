{%- if salt['pillar.get']('mgr_pqc_metadata_signing_enabled', false) %}
mgr_deploy_customer_pqc_key:
  file.managed:
    - name: /usr/lib/rpm/pqkeys/mgr-pqc-cert.pem
    - source: salt://pqc/mgr-pqc-cert.pem
    - makedirs: True
    - mode: 644
{%- endif %}

{%- if grains['os_family'] == 'Suse' %}
mgr_deploy_sles15_pqc_key:
  file.managed:
    - name: /usr/lib/rpm/pqkeys/sles15-mldsa87-key.pem
    - source: salt://pqc/sles15-mldsa87-key.pem
    - makedirs: True
    - mode: 644

mgr_deploy_sles16_pqc_key:
  file.managed:
    - name: /usr/lib/rpm/pqkeys/sles16-mldsa87-key.pem
    - source: salt://pqc/sles16-mldsa87-key.pem
    - makedirs: True
    - mode: 644

mgr_deploy_opensuse_pqc_key:
  file.managed:
    - name: /usr/lib/rpm/pqkeys/opensuse-mldsa87-key.pem
    - source: salt://pqc/opensuse-mldsa87-key.pem
    - makedirs: True
    - mode: 644

{%- endif %}

{# deploy keys defined by the admin #}

{%- for keyname in salt['pillar.get']('custom_pqckeys', []) %}
mgr_deploy_{{ keyname }}:
    file.managed:
    - name: /usr/lib/rpm/pqkeys/{{ keyname }}
    - source: salt://pqc/{{ keyname }}
    - makedirs: True
    - mode: 644
{%- endfor %}
