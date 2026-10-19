mgr_then_live_formula_file:
  file.managed:
    - name: /usr/share/mgr-then-live-formula-test
    - contents: transactional part

mgr_then_live_formula_live:
  file.managed:
    - name: /tmp/mgr-then-live-formula-test
    - contents: live part
