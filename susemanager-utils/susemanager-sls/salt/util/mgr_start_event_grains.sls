mgr_start_event_grains:
  file.append:
    - name: /etc/salt/minion.d/susemanager.conf
    - text: |
        start_event_grains:
          - machine_id
          - saltboot_initrd
          - susemanager
    - unless: grep 'start_event_grains:' /etc/salt/minion.d/susemanager.conf
