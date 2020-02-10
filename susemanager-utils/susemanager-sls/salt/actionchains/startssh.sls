startssh:
    module.run:
    -   name: mgractionchains.start
    -   actionchain_id: {{ pillar.get('actionchain_id')}}
    - require:
      - module: sync_modules

include:
  - util.synccustomall
