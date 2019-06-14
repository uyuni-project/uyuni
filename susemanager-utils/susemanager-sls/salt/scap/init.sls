mgr_scap:
  mgrcompat.module_run:
    - name: openscap.xccdf
    - params: {{ pillar.get('mgr_scap_params') }}