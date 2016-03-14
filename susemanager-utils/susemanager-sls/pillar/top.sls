base:
  '*':
    - ignore_missing: True
    - server_{{ grains['machine_id'] }}
