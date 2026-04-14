mgr_ca_cert:
  file.managed:
{%- if grains['osrelease']|int == 11 %}
    - name: /etc/ssl/certs/RHN-ORG-TRUSTED-SSL-CERT.pem
{%- else %}
    - name: /etc/pki/trust/anchors/RHN-ORG-TRUSTED-SSL-CERT
{%- endif %}
    - source: salt://certs/RHN-ORG-TRUSTED-SSL-CERT

{%- if grains['osrelease']|int == 11 %}
mgr_split_ca:
  cmd.wait_script:
    - name: salt://certs/update-multi-cert.sh
    - runas: root
    - watch:
        - file: mgr_ca_cert

c_rehash:
  cmd.run:
    - name: command -p c_rehash
    - runas: root
    - onchanges:
      - file: mgr_ca_cert
    - require:
      - cmd: mgr_split_ca
{%- else %}

update-ca-certificates:
  cmd.run:
    - name: command -p update-ca-certificates
    - runas: root
    - onchanges:
      - file: mgr_ca_cert
    - retry:
        attempts: 5
        interval: 5
        until: True
{%- endif %}
