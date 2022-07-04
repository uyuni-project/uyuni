{%- if salt['pillar.get']('mgr_metadata_signing_enabled', false) %}
{%- if grains['os_family'] == 'Debian' %}
mgr_debian_repo_keyring:
  file.managed:
    - name: /usr/share/keyrings/mgr-archive-keyring.gpg
    - source: salt://gpg/mgr-keyring.gpg
    - mode: 644
{% else %}
mgr_deploy_customer_gpg_key:
  file.managed:
    - name: /etc/pki/rpm-gpg/mgr-gpg-pub.key
    - source: salt://gpg/mgr-gpg-pub.key
    - makedirs: True
    - mode: 644
{%- endif %}
{%- endif %}

mgr_deploy_tools_uyuni_key:
  file.managed:
    - name: /etc/pki/rpm-gpg/uyuni-tools-gpg-pubkey-0d20833e.key
    - source: salt://gpg/uyuni-tools-gpg-pubkey-0d20833e.key
    - makedirs: True
    - mode: 644

{%- if grains['os_family'] == 'RedHat' %}
{# deploy all keys to the clients. If they get imported dependes on the used channels #}

mgr_deploy_res_gpg_key:
  file.managed:
    - name: /etc/pki/rpm-gpg/res-gpg-pubkey-0182b964.key
    - source: salt://gpg/res-gpg-pubkey-0182b964.key
    - makedirs: True
    - mode: 644

mgr_deploy_tools_rhel_gpg_key:
  file.managed:
    - name: /etc/pki/rpm-gpg/el-tools-gpg-pubkey-39db7c82.key
    - source: salt://gpg/el-tools-gpg-pubkey-39db7c82.key
    - mode: 644

mgr_deploy_legacy_tools_rhel_gpg_key:
  file.managed:
    - name: /etc/pki/rpm-gpg/el6-tools-gpg-pubkey-307e3d54.key
    - source: salt://gpg/el6-tools-gpg-pubkey-307e3d54.key
    - mode: 644

{%- endif %}


{# deploy keys defined by the admin #}

{%- for keyname in salt['pillar.get']('custom_gpgkeys', []) %}
mgr_deploy_{{ keyname }}:
    file.managed:
{%- if grains['os_family'] == 'Debian' %}
    - name: /usr/share/keyrings/{{ keyname }}
{%- else %}
    - name: /etc/pki/rpm-gpg/{{ keyname }}
{%- endif %}
    - source: salt://gpg/{{ keyname }}
    - mode: 644
{%- endfor %}
