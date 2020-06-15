generate_ssh_key:
  salt.state:
    - tgt: {{ pillar['management_node'] }}
    - sls:
      - caasp.temp_ssh_key.gen_ssh_key

# TODO create /srv/susemanager/salt/cluster/ manually and make salt:salt owner
cluster_dir:
  file.directory:
    - name: /srv/susemanager/salt/cluster/{{ pillar['cluster'] }}

# TODO is there a better way of copying files from the minion cache ?
copy_temp_ssh_key:
  file.copy:
    - name: /srv/susemanager/salt/cluster/{{ pillar['cluster'] }}/temp_caasp_key.pub
    - source: /var/cache/salt/master/minions/{{ pillar['management_node'] }}/files/temp_caasp_key.pub
    - force: True
    - require: 
      - file: cluster_dir

deploy_temp_key:
  salt.state:
    - tgt: {{ pillar['nodes'] }}
    - tgt_type: list
    - pillar:
        cluster: {{ pillar['cluster'] }}
    - sls:
      - caasp.temp_ssh_key.deploy_ssh_key
    - require:
      - file: copy_temp_ssh_key
   