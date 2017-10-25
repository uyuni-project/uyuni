/etc/pki/ca-trust/source/anchors/RHN-ORG-TRUSTED-SSL-CERT:
  file.managed:
    - source:
      - salt://certs/RHN-ORG-TRUSTED-SSL-CERT

update-ca-certificates:
  cmd.run:
    - name: /usr/bin/update-ca-trust extract
    - runas: root
    - onchanges:
      - file: /etc/pki/ca-trust/source/anchors/RHN-ORG-TRUSTED-SSL-CERT
