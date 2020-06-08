mgr_disable_swap:
  cmd.run:
    - name: swapoff -a
    - unless: grep " swap " /etc/fstab

mgr_remove_swap_from_fstab:
  file.replace:
    - name: /etc/fstab
    - pattern: ^[^#](.* swap .*)
    - repl: '#\1'
    - unless: grep " swap " /etc/fstab

{#
mgr_caasp_node_set_authorized_keys:
  file.append:
    - name: /root/.ssh/authorized_keys
    - text:
      - {{ key }} # TODO
    - makedirs: True

mgr_caasp_node_enable_sshd_root:
  file.replace:
    - name: /etc/ssh/sshd_config
    - pattern: PermitRootLogin.*
    - repl: PermitRootLogin without-password

mgr_restart_sshd:
  service.running:
    - name: sshd
    - enable: True
    - watch:
      - file: mgr_caasp_node_enable_sshd_root
#}