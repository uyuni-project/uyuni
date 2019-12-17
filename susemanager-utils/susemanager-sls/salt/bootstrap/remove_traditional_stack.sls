# disable all spacewalk:* repos
{% set repos_disabled = {'match_str': 'spacewalk:', 'matching': true} %}
{%- include 'channels/disablelocalrepos.sls' %}

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

remove_traditional_stack_all:
  pkg.removed:
    - pkgs:
      - spacewalk-check
      - spacewalk-client-setup
      - osad
      - osa-common
      - mgr-osad
      - spacewalksd
      - mgr-daemon
      - rhnlib
      - rhnmd
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
{%- if repos_disabled.count > 0 %}
    - require:
      - module: disable_repo*
{%- endif %}

remove_traditional_stack:
  pkg.removed:
    - pkgs:
      - spacewalk-client-tools
      - rhncfg
      - mgr-cfg
{%- if repos_disabled.count > 0 %}
    - require:
      - module: disable_repo*
{%- endif %}
    - unless: rpm -q spacewalk-proxy-common

# Remove suseRegisterInfo in a separate yum transaction to avoid being called by
# the yum plugin.
{%- if grains['os_family'] == 'RedHat' %}
remove_suse_register_info_rh:
  pkg.removed:
    - name: suseRegisterInfo
{%- endif %}
