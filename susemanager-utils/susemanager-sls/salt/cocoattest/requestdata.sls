include:
  - channels

mgr_create_attestdir:
  file.directory:
    - name: /tmp/cocoattest
    - dir_mode: 700

{% if salt['pillar.get']('attestation_data:environment_type', 'NONE') in ['KVM_AMD_EPYC_MILAN', 'KVM_AMD_EPYC_GENOA'] %}

mgr_inst_snpguest:
  pkg.latest:
    - pkgs:
      - snpguest
      - mokutil
    - require:
      - sls: channels

mgr_write_request_data:
  cmd.run:
    - name: echo "{{ salt['pillar.get']('attestation_data:nonce') }}" | base64 -d > /tmp/cocoattest/request-data.txt
    - onlyif: test -x /usr/bin/base64
    - require:
      - file: mgr_create_attestdir

mgr_create_snpguest_report:
  cmd.run:
    - name: snpguest report /tmp/cocoattest/report.bin /tmp/cocoattest/request-data.txt
    - require:
      - cmd: mgr_write_request_data
      - file: mgr_create_attestdir

mgr_snpguest_report:
  cmd.run:
    - name: cat /tmp/cocoattest/report.bin | base64
    - require:
      - cmd: mgr_create_snpguest_report
      - file: mgr_create_attestdir

mgr_secureboot_enabled:
  cmd.run:
    - name: mokutil --sb-state
    - success_retcodes:
      - 255
      - 0

{% endif %}

mgr_cleanup_attest:
  file.absent:
    - name: /tmp/cocoattest
    - require:
      - file: mgr_create_attestdir
