remove_temp_ssh_key:
  file.absent:
    - name: /root/.ssh/temp_caasp_key 

remove_temp_ssh_pub_key:
  file.absent:
    - name: /root/.ssh/temp_caasp_key.pub 