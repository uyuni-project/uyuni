{%- set mgr_server = salt['pillar.get']('mgr_server')%}
{%- set port = salt['pillar.get']('mgr_server_https_port', 443)%}

{%- if salt['pillar.get']('mgr_metadata_signing_enabled', false) %}
{%- if grains['os_family'] == 'Debian' %}
mgr_debian_repo_keyring:
  file.managed:
    - name: /usr/share/keyrings/mgr-archive-keyring.gpg
    - source: salt://gpg/mgr-keyring.gpg
    - mode: 644
{% else %}
mgr_trust_customer_gpg_key:
  cmd.run:
    - name: rpm --import https://{{mgr_server}}:{{port}}/pub/mgr-gpg-pub.key
    - runas: root
{%- endif %}
{%- endif %}

{%- if grains['os_family'] == 'RedHat' %}
trust_res_gpg_key:
  cmd.run:
    - name: rpm --import https://{{mgr_server}}:{{port}}/pub/{{ salt['pillar.get']('gpgkeys:res:file') }}
    - unless: rpm -q {{ salt['pillar.get']('gpgkeys:res:name') }}
    - runas: root

trust_suse_manager_tools_rhel_gpg_key:
  cmd.run:
{%- if grains['osmajorrelease']|int == 6 %}
    - name: rpm --import https://{{mgr_server}}:{{port}}/pub/{{ salt['pillar.get']('gpgkeys:res6tools:file') }}
    - unless: rpm -q {{ salt['pillar.get']('gpgkeys:res6tools:name') }}
{%- elif grains['osmajorrelease']|int == 7 %}
    - name: rpm --import https://{{mgr_server}}:{{port}}/pub/{{ salt['pillar.get']('gpgkeys:res7tools:file') }}
    - unless: rpm -q {{ salt['pillar.get']('gpgkeys:res7tools:name') }}
{%- elif grains['osmajorrelease']|int == 8 %}
    - name: rpm --import https://{{mgr_server}}:{{port}}/pub/{{ salt['pillar.get']('gpgkeys:res8tools:file') }}
    - unless: rpm -q {{ salt['pillar.get']('gpgkeys:res8tools:name') }}
{%- elif grains['osmajorrelease']|int == 2 and grains['os'] == 'Amazon' %}
    - name: rpm --import https://{{ salt['pillar.get']('mgr_server') }}/pub/{{ salt['pillar.get']('gpgkeys:res7tools:file') }}
    - unless: rpm -q {{ salt['pillar.get']('gpgkeys:res7tools:name') }}
{% else %}
    - name: /usr/bin/true
{%- endif %}
    - runas: root

{%- elif grains['os_family'] == 'Debian' %}
install_gnupg_debian:
  pkg.installed:
    - pkgs:
      - gnupg

trust_suse_manager_tools_deb_gpg_key:
  mgrcompat.module_run:
    - name: pkg.add_repo_key
    - path: https://{{mgr_server}}:{{port}}/pub/{{ salt['pillar.get']('gpgkeys:ubuntutools:file') }}
{%- endif %}
