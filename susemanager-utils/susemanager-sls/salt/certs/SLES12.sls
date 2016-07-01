/etc/pki/trust/anchors/RHN-ORG-TRUSTED-SSL-CERT:
  file.managed:
    - source:
      - salt://certs/RHN-ORG-TRUSTED-SSL-CERT

update-ca-certificates:
  cmd.wait:
    - name: /usr/sbin/update-ca-certificates
    - user: root
    - watch:
      - file: /etc/pki/trust/anchors/RHN-ORG-TRUSTED-SSL-CERT
