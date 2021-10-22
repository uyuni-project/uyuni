/etc/ssl/certs/RHN-ORG-TRUSTED-SSL-CERT.pem:
  file.managed:
    - source:
      - salt://certs/RHN-ORG-TRUSTED-SSL-CERT

salt://certs/update-multi-cert.sh:
  cmd.wait_script:
    - runas: root
    - watch:
        - file: /etc/ssl/certs/RHN-ORG-TRUSTED-SSL-CERT.pem

c_rehash:
  cmd.run:
    - name: /usr/bin/c_rehash
    - runas: root
    - onchanges:
      - file: /etc/ssl/certs/*
