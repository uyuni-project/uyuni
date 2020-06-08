{%- if salt['pillar.get']('caasp:management_node', False) %}
mgr_install_caasp_management_stack:
    pkg.installed:
        - pkgs:
          - patterns-caasp-Management
{%- endif %}
