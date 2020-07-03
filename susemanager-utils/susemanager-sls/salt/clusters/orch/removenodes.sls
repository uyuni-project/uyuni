include:
  - {{ salt['pillar.get']('actions:remove:state', '.default') }}