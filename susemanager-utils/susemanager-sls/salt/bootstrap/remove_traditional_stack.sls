{%- set repos_to_disable = {} %}
{%- set repos = salt['pkg.list_repos']() %}
{%- for alias, data in repos.items() %}
  {%- if 'spacewalk:' in alias %}
    {%- if grains['os_family'] == 'Debian' %}
    {# Debian returns array of dict #}
      {%- for d in data %}
        {%- if d['disabled'] == false %}
          {%- do repos_to_disable.update({alias: d['line']}) %}
        {%- endif %}
      {%- endfor %}
    {%- else %}
      {%- if data.get('enabled', true) %}
        {%- do repos_to_disable.update({alias: alias}) %}
      {%- endif %}
    {%- endif %}
  {%- endif %}
{%- endfor %}

disable_spacewalksd:
  service.dead:
    - name: rhnsd
    - enable: False

disable_spacewalk-update-status:
  service.dead:
    - name: spacewalk-update-status
    - enable: False

disable_osad:
  service.dead:
    - name: osad
    - enable: False

remove_traditional_stack:
  pkg.removed:
    - pkgs:
      - spacewalk-check
      - spacewalk-client-setup
      - spacewalk-client-tools
{%- if grains['os_family'] == 'Suse' %}
      - zypp-plugin-spacewalk
      - suseRegisterInfo
{%- elif grains['os_family'] == 'RedHat' %}
      - yum-rhn-plugin
      - rhnsd
      - rhn-check
      - rhn-setup
      - rhn-client-tools
{%- elif grains['os_family'] == 'Debian' %}
      - apt-transport-spacewalk
{%- endif %}
      - osad
      - osa-common
      - spacewalksd
      - rhncfg
      - rhnlib
      - rhnmd
{%- if repos_to_disable|length > 0 %}
    - require:
      - module: disable_repo*
{%- endif %}
    - unless: rpm -q spacewalk-proxy-common

# disable all spacewalk:* repos
{%- for alias, line in repos_to_disable.items() %}
{%- if grains['os_family'] == 'Debian' %}
disable_repo_{{ line | replace(' ', '_') }}:
  module.run:
    - name: pkg.del_repo
    - repo: {{ line }}
{%- else %}
disable_repo_{{ alias }}:
  module.run:
    - name: pkg.mod_repo
    - repo: {{ alias }}
    - kwargs:
        enabled: False
{%- endif %}
{%- endfor %}

# Remove suseRegisterInfo in a separate yum transaction to avoid being called by
# the yum plugin.
{%- if grains['os_family'] == 'RedHat' %}
remove_suse_register_info_rh:
  pkg.removed:
    - name: suseRegisterInfo
{%- endif %}
