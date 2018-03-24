mgr_scap:
  module.run:
    - name: openscap.xccdf
    - params: {{ pillar.get('mgr_scap_params') }}