{%- if grains['osmajorrelease'] == 6 %}
enable_ca_store:
  cmd.run:
    - name: /usr/bin/update-ca-trust enable
    - runas: root
    - unless: "/usr/bin/update-ca-trust check | grep \"PEM/JAVA Status: ENABLED\""
{%- endif %}
/etc/pki/ca-trust/source/anchors/RHN-ORG-TRUSTED-SSL-CERT:
  file.managed:
    - source:
      - salt://certs/RHN-ORG-TRUSTED-SSL-CERT
{%- if grains['osmajorrelease'] == 6 %}
    - require:
      - cmd: enable_ca_store
{%- endif %}
update-ca-certificates:
  cmd.run:
    - name: /usr/bin/update-ca-trust extract
    - runas: root
    - onchanges:
      - file: /etc/pki/ca-trust/source/anchors/RHN-ORG-TRUSTED-SSL-CERT
