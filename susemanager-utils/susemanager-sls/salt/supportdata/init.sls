install-supportdata-command:
  pkg.latest:
{%- if grains['os_family'] == 'Suse' %}
    - name: supportutils
{%- else %}
    - name: sos
{%- endif %}


gather-supportdata:
  mgrcompat.module_run:
    - name: supportdata.get
    - cmd_args: "{{ pillar.get('arguments', '') }}"
    - require:
      - install-supportdata-command
