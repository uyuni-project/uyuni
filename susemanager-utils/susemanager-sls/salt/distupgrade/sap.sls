{% set sles_release_installed = (salt['pkg.info_installed']('sles-release', attr='version', failhard=False).get('sles-release', {}).get('version') != None) %}
{% if sles_release_installed and pillar.get('susemanager:distupgrade:targetbaseproduct:name', '')|lower == 'sles_sap' %}

mgr_remove_release_package:
  cmd.run:
    - name: "rpm -e --nodeps sles-release"

mgr_remove_flavor_package_dvd:
  cmd.run:
    - name: "rpm -e --nodeps sles-release-DVD"
    - onlyif: rpm -q sles-release-DVD

mgr_remove_flavor_package_pool:
  cmd.run:
    - name: "rpm -e --nodeps sles-release-POOL"
    - onlyif: rpm -q sles-release-POOL

{% set default_modules = ['SLES_SAP', 'sle-module-basesystem', 'sle-module-desktop-applications', 'sle-module-server-applications', 'sle-ha', 'sle-module-sap-applications'] %}

{% for module in default_modules %}
mgr_install_product_{{ module }}:
  cmd.run:
    - name: zypper --no-refresh --non-interactive install --no-recommends --auto-agree-with-product-licenses -t product {{ module }}
    - require:
      - cmd: mgr_remove_release_package
      - cmd: mgr_remove_flavor_package_dvd
      - cmd: mgr_remove_flavor_package_pool
{% endfor %}

spmigration:
 test.nop:
   - require:
{%- for module in default_modules %}
     - mgr_install_product_{{ module }}
{%- endfor %}
{% endif %}
