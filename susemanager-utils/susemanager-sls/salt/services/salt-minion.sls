include:
  - bootstrap.remove_traditional_stack

salt-minion:
  pkg.installed:
    - name: salt-minion
  service.running:
    - enable: True
