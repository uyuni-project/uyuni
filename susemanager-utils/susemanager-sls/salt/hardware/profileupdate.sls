{%- if grains['cpuarch'] in ['i386', 'i486', 'i586', 'i686', 'x86_64', 'aarch64'] %}
mgr_install_dmidecode:
  pkg.installed:
{%- if grains['os_family'] == 'Suse' and grains['osrelease'] in ['11.3', '11.4'] %}
    - name: pmtools
{%- else %}
    - name: dmidecode
{%- endif %}
    - require:
{%- if grains.get('__suse_reserved_saltutil_states_support', False) %}
      - saltutil: sync_states
{%- else %}
      - mgrcompat: sync_states
{%- endif %}
{%- endif %}

grains:
  mgrcompat.module_run:
    - name: grains.items
    - require:
{%- if grains.get('__suse_reserved_saltutil_states_support', False) %}
      - saltutil: sync_states
{%- else %}
      - mgrcompat: sync_states
{%- endif %}
cpuinfo:
  mgrcompat.module_run:
    - name: status.cpuinfo
    - require:
{%- if grains.get('__suse_reserved_saltutil_states_support', False) %}
      - saltutil: sync_states
{%- else %}
      - mgrcompat: sync_states
{%- endif %}
udev:
  mgrcompat.module_run:
    - name: udev.exportdb
    - require:
{%- if grains.get('__suse_reserved_saltutil_states_support', False) %}
      - saltutil: sync_states
{%- else %}
      - mgrcompat: sync_states
{%- endif %}
network-interfaces:
  mgrcompat.module_run:
    - name: network.interfaces
    - require:
{%- if grains.get('__suse_reserved_saltutil_states_support', False) %}
      - saltutil: sync_states
{%- else %}
      - mgrcompat: sync_states
{%- endif %}
network-ips:
  mgrcompat.module_run:
    - name: sumautil.primary_ips
    - require:
{%- if grains.get('__suse_reserved_saltutil_states_support', False) %}
      - saltutil: sync_states
      - saltutil: sync_modules
{%- else %}
      - mgrcompat: sync_states
      - mgrcompat: sync_modules
{%- endif %}
network-modules:
  mgrcompat.module_run:
    - name: sumautil.get_net_modules
    - require:
{%- if grains.get('__suse_reserved_saltutil_states_support', False) %}
      - saltutil: sync_states
      - saltutil: sync_modules
{%- else %}
      - mgrcompat: sync_states
      - mgrcompat: sync_modules
{%- endif %}

instance-flavor:
  mgrcompat.module_run:
    - name: sumautil.instance_flavor
    - require:
{%- if grains.get('__suse_reserved_saltutil_states_support', False) %}
      - saltutil: sync_states
      - saltutil: sync_modules
{%- else %}
      - mgrcompat: sync_states
      - mgrcompat: sync_modules
{%- endif %}

{% if grains['cpuarch'] in ['i386', 'i486', 'i586', 'i686', 'x86_64'] %}
smbios-records-bios:
  mgrcompat.module_run:
    - name: smbios.records
    - rec_type: 0
    - clean: False
    - require:
{%- if grains.get('__suse_reserved_saltutil_states_support', False) %}
      - saltutil: sync_states
{%- else %}
      - mgrcompat: sync_states
{%- endif %}
smbios-records-system:
  mgrcompat.module_run:
    - name: smbios.records
    - rec_type: 1
    - clean: False
    - require:
{%- if grains.get('__suse_reserved_saltutil_states_support', False) %}
      - saltutil: sync_states
{%- else %}
      - mgrcompat: sync_states
{%- endif %}
smbios-records-baseboard:
  mgrcompat.module_run:
    - name: smbios.records
    - rec_type: 2
    - clean: False
    - require:
{%- if grains.get('__suse_reserved_saltutil_states_support', False) %}
      - saltutil: sync_states
{%- else %}
      - mgrcompat: sync_states
{%- endif %}
smbios-records-chassis:
  mgrcompat.module_run:
    - name: smbios.records
    - rec_type: 3
    - clean: False
    - require:
{%- if grains.get('__suse_reserved_saltutil_states_support', False) %}
      - saltutil: sync_states
{%- else %}
      - mgrcompat: sync_states
{%- endif %}
{% elif grains['cpuarch'] in ['s390', 's390x'] %}
mainframe-sysinfo:
  mgrcompat.module_run:
    - name: mainframesysinfo.read_values
    - require:
{%- if grains.get('__suse_reserved_saltutil_states_support', False) %}
      - saltutil: sync_states
{%- else %}
      - mgrcompat: sync_states
{%- endif %}
{% endif %}

{%- if grains['saltversioninfo'][0] >= 2018 %}
{% if 'mgrnet.dns_fqdns' in salt %}
dns_fqdns:
  mgrcompat.module_run:
    - name: mgrnet.dns_fqdns
    - require:
{%- if grains.get('__suse_reserved_saltutil_states_support', False) %}
      - saltutil: sync_states
{%- else %}
      - mgrcompat: sync_states
{%- endif %}
    - onlyif:
        which host || which nslookup
{% endif%}
{% if 'network.fqdns' in salt %}
fqdns:
  mgrcompat.module_run:
    - name: network.fqdns
    - require:
{%- if grains.get('__suse_reserved_saltutil_states_support', False) %}
      - saltutil: sync_states
{%- else %}
      - mgrcompat: sync_states
{%- endif %}
{% endif%}
{%- endif%}

include:
  - util.syncstates
  - util.syncmodules
