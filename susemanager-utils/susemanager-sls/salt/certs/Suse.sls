{%- if grains['osmajorrelease'] == 11 %}
/etc/ssl/certs/RHN-ORG-TRUSTED-SSL-CERT.pem:
{%- else %}
/etc/pki/trust/anchors/RHN-ORG-TRUSTED-SSL-CERT:
{%- endif %}
  file.managed:
    - source:
      - salt://certs/RHN-ORG-TRUSTED-SSL-CERT

{%- if grains['osmajorrelease'] == 11 %}
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
{%- else %}

update-ca-certificates:
  cmd.run:
    - name: /usr/sbin/update-ca-certificates
    - runas: root
    - onchanges:
      - file: /etc/pki/trust/anchors/RHN-ORG-TRUSTED-SSL-CERT
{%- if grains['saltversioninfo'][0] >= 3002 %} # Workaround for bsc#1188641
    - unless:
      - fun: service.status
        args:
          - ca-certificates.path
{%- endif %}
{%- endif %}
