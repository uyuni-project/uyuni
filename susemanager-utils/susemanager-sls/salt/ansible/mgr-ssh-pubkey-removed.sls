/tmp/mgr-ssh-pubkey-removed.yml:
  file.managed:
    - source: 'salt://ansible/mgr-ssh-pubkey-removed.yml'

ssh_pubkey_removed_via_ansible:
  ansible.playbooks:
    - name: mgr-ssh-pubkey-removed.yml
    - rundir: /tmp
    - ansible_kwargs:
        inventory: "{{ pillar['inventory'] }}"
        limit: "{{ pillar['target_host'] }}"
        extra_vars:
            user: "{{ pillar['user'] }}"
            ssh_pubkey: "{{ pillar['ssh_pubkey'] }}"

mgr_ssh_pubkey_remove_playbook_cleanup:
  file.absent:
    - name: /tmp/mgr-ssh-pubkey-removed.yml
