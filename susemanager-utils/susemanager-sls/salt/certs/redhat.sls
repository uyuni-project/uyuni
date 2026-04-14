{%- if grains['osrelease']|int == 6 %}
enable_ca_store:
  cmd.run:
    - name: command -p update-ca-trust enable
    - runas: root
    - unless: "command -p update-ca-trust check | command -p grep \"PEM/JAVA Status: ENABLED\""
{%- endif %}

mgr_ca_cert:
  file.managed:
    - name: /etc/pki/ca-trust/source/anchors/RHN-ORG-TRUSTED-SSL-CERT
    - source:
      - salt://certs/RHN-ORG-TRUSTED-SSL-CERT
{%- if grains['osrelease']|int == 6 %}
    - require:
      - cmd: enable_ca_store
{%- endif %}

update-ca-certificates:
  cmd.run:
    - name: command -p update-ca-trust extract
    - runas: root
    - onchanges:
      - file: mgr_ca_cert
