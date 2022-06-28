mgr_copy_RHN-ORG-PRIVATE-SSL-KEY:
  file.managed:
    - name: /root/ssl-build/RHN-ORG-PRIVATE-SSL-KEY
    - source: salt://configure-proxy/RHN-ORG-PRIVATE-SSL-KEY
    - makedirs: True

mgr_copy_RHN-ORG-TRUSTED-SSL-CERT:
  file.managed:
    - name: /root/ssl-build/RHN-ORG-TRUSTED-SSL-CERT
    - source: salt://configure-proxy/RHN-ORG-TRUSTED-SSL-CERT
    - makedirs: True

mgr_copy_rhn-ca-openssl.cnf:
  file.managed:
    - name: /root/ssl-build/rhn-ca-openssl.cnf
    - source: salt://configure-proxy/rhn-ca-openssl.cnf
    - makedirs: True

/root/config-answers.txt:
  file.managed:
    - source: salt://configure-proxy/config-answers.txt
    - template: jinja

configure-proxy:
  cmd.run:
    - name: configure-proxy.sh --non-interactive --rhn-user={{ grains.get('server_username') | default('admin', true) }} --rhn-password={{ grains.get('server_password') | default('admin', true) }} --answer-file=/root/config-answers.txt | tee -a /root/proxy.log
    - requires:
      - pkg: patterns-uyuni_proxy
      - file: /root/config-answers.txt
      - file: mgr_copy_RHN-ORG-TRUSTED-SSL-CERT
      - file: mgr_copy_RHN-ORG-PRIVATE-SSL-KEY
      - file: mgr_copy_rhn-ca-openssl.cnf

