{%- set repos_to_disable = [] %}
{%- set repos = salt['pkg.list_repos']() %}
{%- for alias, data in repos.items() %}
  {%- if 'spacewalk:' in alias %}
    {%- if data.get('enabled', true) %}
      {%- set repos_to_disable = repos_to_disable.append(alias) %}
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

# disable all spacewalk:* repos
{%- set repos_disabled = {'disabled': false} %}
{%- for alias in repos_to_disable %}
disable_repo_{{ alias }}:
  module.run:
    - name: pkg.mod_repo
    - repo: {{ alias }}
    - kwargs:
        enabled: False
{%- if repos_disabled.update({'disabled': true}) %}{% endif %}
{%- endfor %}

# Remove suseRegisterInfo in a separate yum transaction to avoid being called by
# the yum plugin.
{%- if grains['os_family'] == 'RedHat' %}
remove_suse_register_info_rh:
  pkg.removed:
    - name: suseRegisterInfo
{%- endif %}
