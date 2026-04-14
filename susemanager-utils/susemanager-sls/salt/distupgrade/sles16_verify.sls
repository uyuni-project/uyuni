{# SLES 16 Migration Verification State #}

{% if salt['file.file_exists']('/var/lib/uyuni/sles16_migration_started') %}

  {% set migration_log = '' %}
  {% if salt['file.file_exists']('/var/log/distro_migration.log') %}
    {% set migration_log = salt['cmd.run']('tail -200 /var/log/distro_migration.log', python_shell=False) %}
  {% endif %}

  {% if grains['osfullname']|upper == 'SLES' and grains['osmajorrelease']|int == 16 %}

sles16_migration_success:
  test.configurable_test_state:
    - name: sles16_migration_success
    - result: True
    - changes: True
    - comment: |
        SLES 16 migration completed successfully
        Current OS: {{ grains['osfullname'] }} {{ grains['osrelease'] }}
        Architecture: {{ grains['osarch'] }}
        migration_log: {{ migration_log | yaml_encode }}

  {% else %}

    {% set issue_content = '' %}
    {% if salt['file.file_exists']('/etc/issue') %}
      {% set issue_content = salt['file.read']('/etc/issue') %}
    {% endif %}

sles16_migration_failed:
  test.configurable_test_state:
    - name: sles16_migration_failed
    - result: False
    - changes: True
    - comment: |
        SLES 16 migration may have failed
        Current OS: {{ grains['osfullname'] }} {{ grains['osrelease'] }}
        Expected: SLES 16.x
        migration_log: {{ migration_log | yaml_encode }}
        issue_content: {{ issue_content | yaml_encode }}

  {% endif %}

{#  CLEANUP #}

sles16_migration_cleanup_marker:
  file.absent:
    - name: /var/lib/uyuni/sles16_migration_started

sles16_migration_cleanup_target_repos:
  file.absent:
    - name: /etc/zypp/repos.d/susemanager:sles16-migration.repo

sles16_migration_cleanup_config:
  file.absent:
    - name: /etc/sle-migration-service.yml

{% endif %} {# end marker file check #}
