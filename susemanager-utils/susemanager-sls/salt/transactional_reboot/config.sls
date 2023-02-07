{%- if grains['transactional'] %}

copy_conf_file_to_etc_if_not_there:
  file.copy:
    - name: /etc/transactional-update.conf
    - source: /usr/etc/transactional-update.conf

transactional_update_set_rebootmethod_systemd:
  file.keyvalue:
    - name: /etc/transactional-update.conf
    - key_values:
       REBOOT_METHOD: 'systemd'
    - separator: '='
    - uncomment: '# '
    - append_if_not_found: True
    # Only change the reboot method if it is not in default configuration
    - unless:
      - grep -P '^(?=[\s]*+[^#])[^#]*(REBOOT_METHOD=(?!auto))' /etc/transactional-update.conf

{%- endif %}
