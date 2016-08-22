enable_ca_store:
  cmd.run:
    - name: /usr/bin/update-ca-trust enable
    - user: root
    - unless: "/usr/bin/update-ca-trust check | grep \"PEM/JAVA Status: ENABLED\""

/etc/pki/ca-trust/source/anchors/RHN-ORG-TRUSTED-SSL-CERT:
  file.managed:
    - source:
      - salt://certs/RHN-ORG-TRUSTED-SSL-CERT
    - require:
      - cmd: enable_ca_store

update-ca-certificates:
  cmd.wait:
    - name: /usr/bin/update-ca-trust extract
    - user: root
    - watch:
      - file: /etc/pki/ca-trust/source/anchors/RHN-ORG-TRUSTED-SSL-CERT
