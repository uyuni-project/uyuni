include:
  - channels

mgr_ibmpvattest_create_attestdir:
  file.directory:
    - name: /tmp/cocoattest_ibmpvattest
    - dir_mode: 700

mgr_ibmpvattest_inst_pvattest:
  pkg.latest:
    - pkgs:
      - s390-tools
    - require:
      - sls: channels

mgr_ibmpvattest_write_attestation_request:
  cmd.run:
    - name: /usr/bin/echo "{{ salt['pillar.get']('attestation_data:attestation_request') }}" | /usr/bin/base64 -d > /tmp/cocoattest_ibmpvattest/attestation_request.bin
    - onlyif: /usr/bin/test -x /usr/bin/base64
    - require:
      - file: mgr_ibmpvattest_create_attestdir

mgr_ibmpvattest_create_pvattest_response:
  cmd.run:
    - name: /usr/bin/pvattest perform -i /tmp/cocoattest_ibmpvattest/attestation_request.bin -o /tmp/cocoattest_ibmpvattest/attestation_response.bin
    - require:
      - cmd: mgr_ibmpvattest_write_attestation_request
      - file: mgr_ibmpvattest_create_attestdir

mgr_ibmpvattest_pvattest_response:
  cmd.run:
    - name: /usr/bin/cat /tmp/cocoattest_ibmpvattest/attestation_response.bin | /usr/bin/base64
    - require:
      - cmd: mgr_ibmpvattest_create_pvattest_response
      - file: mgr_ibmpvattest_create_attestdir

mgr_ibmpvattest_cleanup_attest:
  file.absent:
    - name: /tmp/cocoattest_ibmpvattest
    - require:
      - file: mgr_ibmpvattest_create_attestdir
