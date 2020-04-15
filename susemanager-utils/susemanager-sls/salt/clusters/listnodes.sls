mgr_cluster_list_nodes:
  module.run:
    - name: mgrclusters.list_nodes
    - provider_module: {{ pillar['cluster_type'] }}
    - params: {{ pillar['params'] }}
