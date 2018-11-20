mgr_create_cert_directory:
  file.directory:
    - user: root
    - name: /usr/share/ca-certificates/suma
    - group: root
    - mode: 755

mgr_download_suma_cert:
  file.managed:
    - name: /usr/share/ca-certificates/suma/RHN-ORG-TRUSTED-SSL-CERT.crt
    - source:
      - salt://certs/RHN-ORG-TRUSTED-SSL-CERT

mgr_add_cert_to_conf:
  file.append:
    - name: /etc/ca-certificates.conf
    - text: suma/RHN-ORG-TRUSTED-SSL-CERT.crt

mgr_update_ca_certs:
  cmd.run:
    - name: /usr/sbin/update-ca-certificates
    - runas: root
    - onchanges:
      - file: /usr/share/ca-certificates/suma/RHN-ORG-TRUSTED-SSL-CERT.crt
