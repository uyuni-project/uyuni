sync_modules:
  module.run:
# workaround for https://github.com/saltstack/salt/issues/38095
{%- if salt['pillar.get']('contact_method') in ['ssh-push', 'ssh-push-tunnel'] %}
    - name: test.true
{%- else %}
    - name: saltutil.sync_modules
{% endif %}
