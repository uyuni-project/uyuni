mgr_cluster_add_node:
  module.run:
    - name: mgrclusters.add_node
    - provider_module: {{ pillar['cluster_type'] }}
    - params: {{ pillar['params'] }}
