remove_mgmt_node_temp_ssh_key:
  salt.state:
    - tgt: {{ pillar['management_node']}}
    - tgt_type: list
    - sls:
      - caasp.temp_ssh_key.remove_mgmt_node_key
    - order: last

remove_nodes_temp_ssh_key:
  salt.state:
    - tgt: {{ pillar['nodes'] }}
    - tgt_type: list
    - pillar:
        cluster: {{ pillar['cluster'] }}
    - sls:
      - caasp.temp_ssh_key.remove_nodes_ssh_key
    - order: last

remove_server_temp_ssh_key:
  file.absent:
    - name: /srv/susemanager/salt/cluster/{{ pillar['cluster'] }}/temp_caasp_key.pub
    - order: last
    - require:
      - salt: remove_nodes_temp_ssh_key
