include:
  - {{ salt['pillar.get']('actions:join:state', '.default') }}