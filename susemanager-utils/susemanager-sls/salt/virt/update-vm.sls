{%- set vm_info = salt.virt_utils.vm_info(pillar['name']) %}
{%- set cluster_id = vm_info[pillar['name']].get('cluster_primitive') %}

{%- if cluster_id %}
temporary_define:
    mgrcompat.module_run:
        - name: virt.define_xml_path
        - path: {{ vm_info[pillar['name']]['definition_path'] }}
{%- endif %}

domain_update:
    mgrcompat.module_run:
        - name: virt.update
        - m_name: {{ pillar['name'] }}
        - cpu: {{ pillar['vcpus'] }}
        - mem: {{ pillar['mem'] }}
{% if 'disks' in pillar %}
        - disks:
    {% for disk in pillar['disks'] %}
            - name: {{ disk['name'] }}
              model: {{ disk['model'] }}
        {% if 'device' in disk %}
              device: {{ disk['device'] }}
        {% endif %}
        {% if 'type' in disk %}
              type: {{ disk['type'] }}
        {% endif %}
        {% if 'format' in disk %}
              format: {{ disk['format'] }}
        {% endif %}
        {% if 'source_file' in disk %}
              source_file: {{ disk['source_file'] if disk['source_file'] != '' else 'null' }}
        {% endif %}
        {% if 'pool' in disk %}
              pool: {{ disk['pool'] }}
        {% endif %}
        {% if 'size' in disk %}
              size: {{ disk['size'] }}
        {% endif %}
        {% if 'image' in disk %}
              image: {{ disk['image'] }}
        {% endif %}
    {% endfor %}
{% endif %}
{% if 'interfaces' in pillar %}
        - interfaces:
    {% for nic in pillar['interfaces'] %}
            - name: {{ nic['name'] }}
              type: {{ nic['type'] }}
              source: {{ nic['source'] }}
        {% if 'mac' in nic %}
              mac: {{ nic['mac'] if nic['mac'] != '' else 'null' }}
        {% endif %}
    {% endfor %}
{% endif %}
        - graphics:
            type: {{ pillar['graphics']['type'] }}
{%- if cluster_id %}
        - require:
            - mgrcompat: temporary_define

{{ vm_info[pillar['name']]['definition_path'] }}:
    mgrutils.cmd_dump:
        - cmd: 'virsh dumpxml --inactive {{ pillar['name'] }}'
        - require:
            - mgrcompat: domain_update

temporary_undefine:
    mgrcompat.module_run:
        - name: virt.undefine
        - vm_: {{ pillar['name'] }}
        - require:
            - mgrutils: {{ vm_info[pillar['name']]['definition_path'] }}
{%- endif %}
