{% if pillar.get('boot', {}).get('kernel') -%}
kernel_cached:
  file.managed:
    - name: /tmp/virt/{{ pillar['name'] }}/kernel
    - source: salt://bootloader/{{ pillar['boot']['kernel'] }}
    - makedirs: True

initrd_cached:
  file.managed:
    - name: /tmp/virt/{{ pillar['name'] }}/initrd
    - source: salt://bootloader/{{ pillar['boot']['initrd'] }}
    - makedirs: True
{%- endif %}

{%- if 'interfaces' in pillar %}
nets-{{ pillar['name'] }}:
  virt_utils.network_running:
    - networks:
    {%- for nic in pillar['interfaces'] %}
      - {{ nic['source'] }}
    {%- endfor %}
{%- endif %}

{%- if 'disks' in pillar %}
pools-{{ pillar['name'] }}:
  virt_utils.pool_running:
    - pools:
  {%- for disk in pillar['disks'] %}
    {%- if 'pool' in disk %}
      - {{ disk['pool'] }}
    {%- endif %}
  {%- endfor %}
{%- endif %}

{% macro domain_params() -%}
    - name: {{ pillar['name'] }}
    - cpu: {{ pillar['vcpus'] }}
    - mem: {{ pillar['mem'] // 1024 }}
    - os_type: {{ pillar['os_type'] }}
    - arch: {{ pillar['arch'] }}
    - vm_type: {{ pillar['vm_type'] }}
    - disks:
{%- for disk in pillar['disks'] %}
      - name: {{ disk['name'] }}
        model: {{ disk['model'] }}
    {%- if 'device' in disk %}
        device: {{ disk['device'] }}
    {%- endif %}
    {%- if 'format' in disk %}
        format: {{ disk['format'] }}
    {%- endif %}
    {%- if 'source_file' in disk %}
        source_file: {{ disk['source_file'] if disk['source_file'] != '' else 'null' }}
    {%- endif %}
    {%- if 'pool' in disk %}
        pool: {{ disk['pool'] }}
    {%- endif %}
    {%- if 'size' in disk %}
        size: {{ disk['size'] }}
    {%- endif %}
    {%- if 'image' in disk %}
        image: {{ disk['image'] }}
    {%- endif %}
{%- endfor %}
{%- if 'interfaces' in pillar %}
    - interfaces:
    {%- for nic in pillar['interfaces'] %}
      - name: {{ nic['name'] }}
        type: {{ nic['type'] }}
        source: {{ nic['source'] }}
        {%- if 'mac' in nic %}
        mac: {{ nic['mac'] if nic['mac'] != '' else 'null' }}
        {%- endif %}
    {%- endfor %}
{%- endif %}
    - graphics:
        type: {{ pillar['graphics']['type'] }}
{%- endmacro %}

{%- set cdrom_boot = pillar.get('boot_dev', 'hd').startswith('cdrom') -%}
{%- set ks_boot = pillar.get('boot', {}).get('kernel') is not none -%}
domain_first_boot_define:
  virt.running:
    {{ domain_params() }}
    - seed: False
    - boot_dev: {{ pillar.get('boot_dev', 'hd') }}
{%- if cdrom_boot or ks_boot %}
    - stop_on_reboot: True
{%- endif %}
{%- if ks_boot %}
    - boot:
        kernel: /tmp/virt/{{ pillar['name'] }}/kernel
        initrd: /tmp/virt/{{ pillar['name'] }}/initrd
        cmdline: {{ pillar['boot']['kopts'] }}
    - require:
      - file: kernel_cached
      - file: initrd_cached
  {%- if 'interfaces' in pillar %}
      - virt_utils: nets-{{ pillar['name'] }}
  {%- endif %}
  {%- if 'disks' in pillar %}
      - virt_utils: pools-{{ pillar['name'] }}
  {%- endif %}
{%- endif %}

{%- if cdrom_boot or ks_boot %}
domain_define:
  virt.defined:
    {{ domain_params() }}
    - live: False
    - stop_on_reboot: False
  {%- if cdrom_boot %}
    - boot_dev: "hd"
  {%- endif %}
{%- if ks_boot %}
    - boot:
        kernel: null
        initrd: null
        cmdline: null
{%- endif %}
    - require:
      - virt: domain_first_boot_define
{%- endif %}
