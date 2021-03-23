/etc/pki/ca-trust/source/anchors/RPM-GPG-KEY-ALIYUN:
  file.managed:
    - source:
      - salt://certs/RPM-GPG-KEY-ALIYUN

update-ca-certificates:
  cmd.run:
    - name: /usr/bin/update-ca-trust extract
    - runas: root
    - onchanges:
      - file: /etc/pki/ca-trust/source/anchors/RPM-GPG-KEY-ALIYUN
