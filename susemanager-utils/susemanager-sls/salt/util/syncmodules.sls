sync_modules:
  module.run:
# workaround for https://github.com/saltstack/salt/issues/38095
{% if not salt['pillar.get']('master:__master_opts__') %}
    - name: saltutil.sync_modules
{% else %}
    - name: test.true
{% endif %}
