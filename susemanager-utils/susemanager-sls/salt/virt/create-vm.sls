domain_define:
    virt.running:
        - name: {{ pillar['name'] }}
        - cpu: {{ pillar['vcpus'] }}
        - mem: {{ pillar['mem'] // 1024 }}
        - os_type: {{ pillar['os_type'] }}
        - arch: {{ pillar['arch'] }}
        - vm_type: {{ pillar['vm_type'] }}
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
        - seed: False
