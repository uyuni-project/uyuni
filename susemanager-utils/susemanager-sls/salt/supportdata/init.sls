{% if not grains.get('transactional', False) %}
install-supportdata-command:
  pkg.latest:
{%- if grains['os_family'] == 'Suse' %}
    - name: supportutils
{%- else %}
    - name: sos
{%- endif %}
{% endif %}


gather-supportdata:
  module.run:
    - name: supportdata.get
    - cmd_args: "{{ pillar.get('arguments', '') }}"
