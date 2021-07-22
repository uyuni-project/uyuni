/etc/pki/trust/anchors/RHN-ORG-TRUSTED-SSL-CERT:
  file.managed:
    - source:
      - salt://certs/RHN-ORG-TRUSTED-SSL-CERT

update-ca-certificates:
  cmd.run:
    - name: /usr/sbin/update-ca-certificates
    - runas: root
    - onchanges:
      - file: /etc/pki/trust/anchors/RHN-ORG-TRUSTED-SSL-CERT
    - unless:
      - fun: service.status
        args:
          - ca-certificates.path
