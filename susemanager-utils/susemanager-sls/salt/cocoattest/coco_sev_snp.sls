include:
  - channels

mgr_sevsnp_create_attestdir:
  file.directory:
    - name: /tmp/cocoattest_sevsnp
    - dir_mode: 700

mgr_sevsnp_inst_snpguest:
  pkg.latest:
    - pkgs:
      - snpguest
    - require:
      - sls: channels

mgr_sevsnp_write_request_data:
  cmd.run:
    - name: /usr/bin/echo "{{ salt['pillar.get']('attestation_data:nonce') }}" | /usr/bin/base64 -d > /tmp/cocoattest_sevsnp/random_user_nonce.bin
    - onlyif: /usr/bin/test -x /usr/bin/base64
    - require:
      - file: mgr_sevsnp_create_attestdir

mgr_sevsnp_create_snpguest_response:
  cmd.run:
    - name: /usr/bin/snpguest report /tmp/cocoattest_sevsnp/response.bin /tmp/cocoattest_sevsnp/random_user_nonce.bin
    - require:
      - cmd: mgr_sevsnp_write_request_data
      - file: mgr_sevsnp_create_attestdir

mgr_sevsnp_snpguest_response:
  cmd.run:
    - name: /usr/bin/cat /tmp/cocoattest_sevsnp/response.bin | /usr/bin/base64
    - require:
      - cmd: mgr_sevsnp_create_snpguest_response
      - file: mgr_sevsnp_create_attestdir

mgr_sevsnp_create_vlek_certificate:
  cmd.run:
    - name: /usr/bin/snpguest certificates PEM /tmp/cocoattest_sevsnp
    - require:
      - file: mgr_sevsnp_create_attestdir

mgr_sevsnp_vlek_certificate:
  cmd.run:
    - name: /usr/bin/cat /tmp/cocoattest_sevsnp/vlek.pem
    - require:
      - cmd: mgr_sevsnp_create_vlek_certificate
      - file: mgr_sevsnp_create_attestdir

mgr_sevsnp_cleanup_attest:
  file.absent:
    - name: /tmp/cocoattest_sevsnp
    - require:
      - file: mgr_sevsnp_create_attestdir
