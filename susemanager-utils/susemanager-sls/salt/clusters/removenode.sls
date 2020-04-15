mgr_cluster_remove_node:
  module.run:
    - name: mgrclusters.remove_node
    - provider_module: {{ pillar['cluster_type'] }}
    - params: {{ pillar['params'] }}
