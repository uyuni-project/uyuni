include:
  - channels

mgr_secureboot_inst_mokutil:
  pkg.latest:
    - pkgs:
      - mokutil
    - require:
      - sls: channels

mgr_secureboot_enabled:
  cmd.run:
    - name: /usr/bin/mokutil --sb-state
    - success_retcodes:
      - 255
      - 0
      - 1
