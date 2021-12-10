mgr_scap:
  mgrcompat.module_run:
{%- if "openscap.xccdf_eval" not in salt %}
    - name: openscap.xccdf
    - params: {{ pillar.get('mgr_scap_params')['old_parameters'] }}
{%- else %}
    - name: openscap.xccdf_eval
    - xccdffile: {{ pillar['mgr_scap_params']['xccdffile'] }}
    {%- if "ovalfiles" in pillar.get('mgr_scap_params') %}
    - ovalfiles:
      {%- for oval in pillar['mgr_scap_params']['ovalfiles'] %}
        - {{ oval }}
      {%- endfor %}
    {%- endif %}
    - kwargs:
        results: results.xml
        report: report.html
        oval_results: True
        {%- if "profile" in pillar.get('mgr_scap_params') %}
        profile: {{ pillar['mgr_scap_params']['profile'] }}
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
        {%- if "tailoring_file" in pillar.get('mgr_scap_params') %}
        tailoring_file: {{ pillar['mgr_scap_params']['tailoring_file'] }}
        {%- endif %}
        {%- if "tailoring_id" in pillar.get('mgr_scap_params') %}
        tailoring_id: {{ pillar['mgr_scap_params']['tailoring_id'] }}
        {%- endif %}
{% endif %}
