/etc/pki/ca-trust/source/anchors/RHN-ORG-TRUSTED-SSL-CERT:
  file.managed:
    - source:
      - salt://certs/RHN-ORG-TRUSTED-SSL-CERT

update-ca-certificates:
  cmd.wait:
    - name: /usr/bin/update-ca-trust extract
    - user: root
    - watch:
      - file: /etc/pki/ca-trust/source/anchors/RHN-ORG-TRUSTED-SSL-CERT
