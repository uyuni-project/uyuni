/usr/local/share/ca-certificates/RHN-ORG-TRUSTED-SSL-CERT.crt:
  file.managed:
    - source:
      - salt://certs/RHN-ORG-TRUSTED-SSL-CERT

update-ca-certificates:
  cmd.run:
    - name: /usr/sbin/update-ca-certificates
    - runas: root
    - onchanges:
      - file: /usr/local/share/ca-certificates/RHN-ORG-TRUSTED-SSL-CERT.crt
