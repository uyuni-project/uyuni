/etc/ssl/certs/RHN-ORG-TRUSTED-SSL-CERT.pem:
  file.managed:
    - source:
      - salt://certs/RHN-ORG-TRUSTED-SSL-CERT

c_rehash:
  cmd.wait:
    - name: /usr/bin/c_rehash
    - user: root
    - watch:
      - file: /etc/ssl/certs/RHN-ORG-TRUSTED-SSL-CERT.pem
