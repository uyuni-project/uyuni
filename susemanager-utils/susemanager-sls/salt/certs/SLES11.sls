/etc/ssl/certs/RHN-ORG-TRUSTED-SSL-CERT.pem:
  file.managed:
    - source:
      - salt://certs/RHN-ORG-TRUSTED-SSL-CERT

c_rehash:
  cmd.run:
    - name: /usr/bin/c_rehash
    - user: root
    - require:
      - file: /etc/ssl/certs/RHN-ORG-TRUSTED-SSL-CERT.pem
