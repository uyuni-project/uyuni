include:
  - caasp.temp_ssh_key.remove_mgmt_node_key

generate_temp_ssh_key:
  cmd.run:
    - name: ssh-keygen -N '' -C 'temp-caasp-key' -f /root/.ssh/temp_caasp_key -t rsa -q
    - creates: /root/.ssh/temp_caasp_key.pub
    - require: 
      - sls: caasp.temp_ssh_key.remove_mgmt_node_key

push_temp_ssh_key:
  module.run:
    - name: cp.push
    - path: /root/.ssh/temp_caasp_key.pub
    - upload_path: temp_caasp_key.pub
    - require:
      - cmd: generate_temp_ssh_key


