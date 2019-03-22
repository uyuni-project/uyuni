{%- if salt['pillar.get']('contact_method') not in ['ssh-push', 'ssh-push-tunnel'] %}
mgr_mine_config_clean_up:
  file.absent:
    - name: /etc/salt/minion.d/susemanager-mine.conf
{% endif %}
