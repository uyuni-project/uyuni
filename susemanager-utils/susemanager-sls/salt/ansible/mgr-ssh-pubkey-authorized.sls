{% set tempdir = salt['temp.dir']('', 'mgr-ssh-pubkey-authorized_') %}
{% set tempfile = tempdir + '/mgr-ssh-pubkey-authorized.yml' %}

mgr_ssh_pubkey_authorize_playbook_create:
  file.managed:
    - name: {{ tempfile }}
    - source: 'salt://ansible/mgr-ssh-pubkey-authorized.yml'

ssh_pubkey_authorized_via_ansible:
  ansible.playbooks:
    - name: mgr-ssh-pubkey-authorized.yml
    - rundir: {{ tempdir }}
    - ansible_kwargs:
        inventory: "{{ pillar['inventory'] }}"
        limit: "{{ pillar['target_host'] }}"
        extra_vars:
            user: "{{ pillar['user'] }}"
            ssh_pubkey: "{{ pillar['ssh_pubkey'] }}"

mgr_ssh_pubkey_authorize_playbook_cleanup:
  file.absent:
    - name: {{ tempdir }}

