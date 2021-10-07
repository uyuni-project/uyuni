/etc/salt/minion.d/susemanager.conf:
  file.line:
    - mode: replace
    - match: "master:"
    - content: "master: {{ pillar['mgr_server'] }}"

restart:
  mgrcompat.module_run:
    - name: cmd.run_bg
    - cmd: "sleep 2; service salt-minion restart"
    - python_shell: true
