salt-minion:
  pkg.installed:
    - name: salt-minion
  service.running:
    - enable: True
