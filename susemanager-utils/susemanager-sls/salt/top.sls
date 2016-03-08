base:
  'os_family:Suse':
    - match: grain
    - channels
  '*':
    - certs
    - packages
    - custom
    - custom_groups
    - custom_org
