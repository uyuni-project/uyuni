install-supportdata-command:
  pkg.installed:
{%- if grains['os_family'] == 'Suse' %}
    - name: supportutils
{%- else %}
    - name: sosreport
{%- endif %}
    - install_recommends: False


gather-supportdata:
  mgrcompat.module_run:
    - name: supportdata.get
    - cmd_args: pillar.get('arguments')
    - require:
      - install-supportdata-command
