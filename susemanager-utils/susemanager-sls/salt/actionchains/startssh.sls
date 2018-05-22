startssh:
    module.run:
    -   name: mgractionchains.start
    -   actionchain_id: {{ pillar.get('actionchain_id')}}
