mgr_scap:
  mgrcompat.module_run:
    - name: openscap.xccdf_eval
    - xccdffile: {{ pillar['mgr_scap_params']['xccdffile'] }}
    - kwargs:
        results: results.xml
        report: report.html
        oval_results: True
        {%- if "profile" in pillar.get('mgr_scap_params') %}
        profile: {{ pillar['mgr_scap_params']['profile'] }}
        {%- endif %}
        {%- if "ovalfiles" in pillar.get('mgr_scap_params') %}
        ovalfiles: {{ pillar['mgr_scap_params']['ovalfiles'] }}
        {%- endif %}
        {%- if "rule" in pillar.get('mgr_scap_params') %}
        rule: {{ pillar['mgr_scap_params']['rule'] }}
        {%- endif %}
        {%- if "remediate" in pillar.get('mgr_scap_params') %}
        remediate: {{ pillar['mgr_scap_params']['remediate'] }}
        {%- endif %}
        {%- if "fetch_remote_resources" in pillar.get('mgr_scap_params') %}
        fetch_remote_resources: {{ pillar['mgr_scap_params']['fetch_remote_resources'] }}
        {%- endif %}
