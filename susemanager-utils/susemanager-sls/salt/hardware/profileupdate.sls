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
  module.run:
    - name: grains.items
    - require:
{%- if grains.get('__suse_reserved_saltutil_states_support', False) %}
      - saltutil: sync_states
{%- else %}
      - mgrcompat: sync_states
{%- endif %}
cpuinfo:
  module.run:
    - name: status.cpuinfo
    - require:
{%- if grains.get('__suse_reserved_saltutil_states_support', False) %}
      - saltutil: sync_states
{%- else %}
      - mgrcompat: sync_states
{%- endif %}
udev:
  module.run:
    - name: udev.exportdb
    - require:
{%- if grains.get('__suse_reserved_saltutil_states_support', False) %}
      - saltutil: sync_states
{%- else %}
      - mgrcompat: sync_states
{%- endif %}
network-interfaces:
  module.run:
    - name: network.interfaces
    - require:
{%- if grains.get('__suse_reserved_saltutil_states_support', False) %}
      - saltutil: sync_states
{%- else %}
      - mgrcompat: sync_states
{%- endif %}
network-ips:
  module.run:
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
  module.run:
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
  module.run:
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
  module.run:
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
  module.run:
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
  module.run:
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
  module.run:
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
  module.run:
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
  module.run:
    - name: mgrnet.dns_fqdns
    - require:
{%- if grains.get('__suse_reserved_saltutil_states_support', False) %}
      - saltutil: sync_states
{%- else %}
      - mgrcompat: sync_states
{%- endif %}
    - onlyif:
        /usr/bin/which host || /usr/bin/which nslookup
{% endif%}
{% if 'network.fqdns' in salt %}
fqdns:
  module.run:
    - name: network.fqdns
    - require:
{%- if grains.get('__suse_reserved_saltutil_states_support', False) %}
      - saltutil: sync_states
{%- else %}
      - mgrcompat: sync_states
{%- endif %}
{% endif%}
{%- endif%}

{% if grains['os_family'] == 'Suse' %}
sap_workloads:
  module.run:
    - name: sap.get_workloads
    - require:
{%- if grains.get('__suse_reserved_saltutil_states_support', False) %}
      - saltutil: sync_modules
{%- else %}
      - mgrcompat: sync_modules
{%- endif %}
{% endif %}

uname:
  cmd.run:
    - name: /usr/bin/uname -r -v

container_runtime:
  module.run:
    - name: container_runtime.get_container_runtime
    - require:
{%- if grains.get('__suse_reserved_saltutil_states_support', False) %}
      - saltutil: sync_modules
{%- else %}
      - mgrcompat: sync_modules
{%- endif %}

proxy_info:
  module.run:
    - name: proxy.info
    - require:
{%- if grains.get('__suse_reserved_saltutil_states_support', False) %}
      - saltutil: sync_modules
{%- else %}
      - mgrcompat: sync_modules
{%- endif %}

include:
  - util.syncstates
  - util.syncmodules
