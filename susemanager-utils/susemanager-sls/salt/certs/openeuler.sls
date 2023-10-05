mgr_ca_cert:
  file.managed:
    - name: /etc/pki/ca-trust/source/anchors/RHN-ORG-TRUSTED-SSL-CERT
    - source:
      - salt://certs/RHN-ORG-TRUSTED-SSL-CERT

update-ca-certificates:
  cmd.run:
    - name: /usr/bin/update-ca-trust extract
    - runas: root
    - onchanges:
      - file: mgr_ca_cert
