{%- set scap_cache_dir = '/var/cache/salt/minion/scap' -%}

# Transfer SCAP content file from master to minion
transfer_xccdf_file:
  file.managed:
    - name: {{ scap_cache_dir }}/{{ pillar['mgr_scap_params']['xccdf_filename'] }}
    - source: salt://ssg/content/{{ pillar['mgr_scap_params']['xccdf_filename'] }}
    - makedirs: True
    - skip_verify: False
    - mode: '0644'

{%- if "tailoring_filename" in pillar.get('mgr_scap_params') %}
# Transfer tailoring file from master to minion
transfer_tailoring_file:
  file.managed:
    - name: {{ scap_cache_dir }}/{{ pillar['mgr_scap_params']['tailoring_filename'] }}
    - source: salt://tailoring-files/{{ pillar['mgr_scap_params']['tailoring_filename'] }}
    - makedirs: True
    - skip_verify: False
    - mode: '0644'
{%- endif %}

# Run SCAP scan using transferred files
mgr_scap:
  mgrcompat.module_run:
{%- if "openscap.xccdf_eval" not in salt %}
    - name: openscap.xccdf
    - params: {{ pillar.get('mgr_scap_params')['old_parameters'] }}
{%- else %}
    - name: openscap.xccdf_eval
    - xccdffile: {{ scap_cache_dir }}/{{ pillar['mgr_scap_params']['xccdf_filename'] }}
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
        {%- if "tailoring_filename" in pillar.get('mgr_scap_params') %}
        tailoring_file: {{ scap_cache_dir }}/{{ pillar['mgr_scap_params']['tailoring_filename'] }}
        {%- endif %}
    - require:
      - file: transfer_xccdf_file
      {%- if "tailoring_filename" in pillar.get('mgr_scap_params') %}
      - file: transfer_tailoring_file
      {%- endif %}
{% endif %}
