{% if grains.get('is_mgr_server', False) and grains.get('has_report_db', False) %}
{% if pillar.get('report_db_user', '') != '' and pillar.get('report_db_password', '') != '' %}
mgr_set_report_db_user:
  reportdb_user.present:
    - name: {{ pillar['report_db_user'] }}
    - password: {{ pillar['report_db_password'] }}
{% endif %}
{% endif %}
