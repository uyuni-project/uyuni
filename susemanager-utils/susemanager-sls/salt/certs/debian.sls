mgr_download_mgr_cert:
  file.managed:
    - name: /usr/local/share/ca-certificates/susemanager/RHN-ORG-TRUSTED-SSL-CERT.crt
    - makedirs: True
    - source:
      - salt://certs/RHN-ORG-TRUSTED-SSL-CERT

mgr_update_ca_certs:
  cmd.run:
    - name: /usr/sbin/update-ca-certificates
    - runas: root
    - onchanges:
      - file: /usr/local/share/ca-certificates/susemanager/RHN-ORG-TRUSTED-SSL-CERT.crt
