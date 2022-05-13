include:
  - util.syncstates

{%- if grains['os_family'] == 'RedHat' %}

{%- set yum_version = salt['pkg.version']("yum") %}
{%- set is_yum = yum_version and salt['pkg.version_cmp'](yum_version, "4") < 0 %}
{%- set is_dnf = salt['pkg.version']("dnf") %}

{%- if is_dnf %}
mgrchannels_susemanagerplugin_dnf:
  file.managed:
    - name: /usr/lib/python{{ grains['pythonversion'][0] }}.{{ grains['pythonversion'][1] }}/site-packages/dnf-plugins/susemanagerplugin.py
    - source:
      - salt://channels/dnf-susemanager-plugin/susemanagerplugin.py
    - user: root
    - group: root
    - mode: 644

mgrchannels_susemanagerplugin_conf_dnf:
  file.managed:
    - name: /etc/dnf/plugins/susemanagerplugin.conf
    - source:
      - salt://channels/dnf-susemanager-plugin/susemanagerplugin.conf
    - user: root
    - group: root
    - mode: 644

mgrchannels_enable_dnf_plugins:
  file.replace:
    - name: /etc/dnf/dnf.conf
    - pattern: plugins=.*
    - repl: plugins=1
{#- default is '1' when option is not specififed #}
    - onlyif: grep -e 'plugins=0' -e 'plugins=False' -e 'plugins=no' /etc/dnf/dnf.conf
{%- endif %}

{%- if is_yum %}
mgrchannels_susemanagerplugin_yum:
  file.managed:
    - name: /usr/share/yum-plugins/susemanagerplugin.py
    - source:
      - salt://channels/yum-susemanager-plugin/susemanagerplugin.py
    - user: root
    - group: root
    - mode: 644

mgrchannels_susemanagerplugin_conf_yum:
  file.managed:
    - name: /etc/yum/pluginconf.d/susemanagerplugin.conf
    - source:
      - salt://channels/yum-susemanager-plugin/susemanagerplugin.conf
    - user: root
    - group: root
    - mode: 644

mgrchannels_enable_yum_plugins:
  file.replace:
    - name: /etc/yum.conf
    - pattern: plugins=.*
    - repl: plugins=1
    - onlyif: grep plugins=0 /etc/yum.conf

{%- endif %}
{%- endif %}

mgrchannels_repo:
  file.managed:
{%- if grains['os_family'] == 'Suse' %}
    - name: "/etc/zypp/repos.d/susemanager:channels.repo"
{%- elif grains['os_family'] == 'RedHat' %}
    - name: "/etc/yum.repos.d/susemanager:channels.repo"
{%- elif grains['os_family'] == 'Debian' %}
    - name: "/etc/apt/sources.list.d/susemanager:channels.list"
{%- endif %}
    - source:
      - salt://channels/channels.repo
    - template: jinja
    - user: root
    - group: root
    - mode: 644
{%- if grains['os_family'] == 'RedHat' %}
    - require:
{%- if is_dnf %}
       - file: mgrchannels_susemanagerplugin_dnf
       - file: mgrchannels_susemanagerplugin_conf_dnf
{%- endif %}
{%- if is_yum %}
       - file: mgrchannels_susemanagerplugin_yum
       - file: mgrchannels_susemanagerplugin_conf_yum
{%- endif %}
{%- endif %}

{%- set apt_version = salt['pkg.version']("apt") %}
{%- set apt_support_acd = grains['os_family'] == 'Debian' and apt_version and salt['pkg.version_cmp'](apt_version, "1.6.10") > 0 %}

{%- if apt_support_acd %}
aptauth_conf:
  file.managed:
    - name: "/etc/apt/auth.conf.d/susemanager.conf"
    - source:
      - salt://channels/aptauth.conf
    - template: jinja
    - user: _apt
    - group: root
    - mode: 600
{%- endif %}

{%- if grains['os_family'] == 'RedHat' %}
{%- if is_dnf %}
mgrchannels_dnf_clean_all:
  cmd.run:
    - name: /usr/bin/dnf clean all
    - runas: root
    - onchanges:
       - file: "/etc/yum.repos.d/susemanager:channels.repo"
    -  unless: "/usr/bin/dnf repolist | grep \"repolist: 0$\""
{%- endif %}
{%- if is_yum %}
mgrchannels_yum_clean_all:
  cmd.run:
    - name: /usr/bin/yum clean all
    - runas: root
    - onchanges: 
       - file: "/etc/yum.repos.d/susemanager:channels.repo"
    -  unless: "/usr/bin/yum repolist | grep \"repolist: 0$\""
{%- endif %}
{%- endif %}

{%- if not salt['pillar.get']('susemanager:distupgrade:dryrun', False) %}
{%- if grains['os_family'] == 'Suse' and grains['osmajorrelease']|int > 11 and not grains['oscodename'] == 'openSUSE Leap 15.3' %}
mgrchannels_install_products:
  product.all_installed:
    - require:
      - file: mgrchannels_*
{%- if grains.get('__suse_reserved_saltutil_states_support', False) %}
      - saltutil: sync_states
{%- else %}
      - mgrcompat: sync_states
{%- endif %}
{%- endif %}

{% include 'channels/gpg-keys.sls' %}
{%- endif %}
