/etc/pki/trust/anchors/RHN-ORG-TRUSTED-SSL-CERT:
  file.managed:
    - source:
      - salt://certs/RHN-ORG-TRUSTED-SSL-CERT

update-ca-certificates:
  cmd.run:
    - name: /usr/sbin/update-ca-certificates
    - user: root
    - require:
      - file: /etc/pki/trust/anchors/RHN-ORG-TRUSTED-SSL-CERT
