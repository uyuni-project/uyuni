/etc/zypp/repos.d/susemanager:channels.repo:
  file.managed:
    - source:
      - salt://channels/channels.repo.{{ grains['machine_id'] }}
      - salt://channels/empty.repo
    - user: root
    - group: root
    - mode: 644

/etc/pki/trust/anchors/RHN-ORG-TRUSTED-SSL-CERT:
  file.managed:
    - source:
      - salt://channels/RHN-ORG-TRUSTED-SSL-CERT

update-ca-certificates:
  cmd.run:
    - name: /usr/sbin/update-ca-certificates
    - user: root
    - require:
      - file: /etc/pki/trust/anchors/RHN-ORG-TRUSTED-SSL-CERT
