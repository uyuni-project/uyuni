{# SLES 16 Migration using Distribution Migration System (DMS) #}

{% if grains['osfullname']|upper == 'SLES' and grains['osrelease'] == '15.7' %}

{% set is_s390x = grains['osarch'] == 's390x' %}

sles16_migration_package:
  pkg.installed:
    - name: {% if is_s390x %}SLES16-Migration{% else %}suse-migration-sle16-activation{% endif %}

sles16_migration_target_repos:
  file.managed:
    - name: /etc/zypp/repos.d/susemanager:sles16-migration.repo
    - mode: '0600'
    - contents: |
        # SLES 16 target repos for DMS - managed by Uyuni
        {% for chan in pillar.get('sles16_target_channels', []) %}
        [susemanager:{{ chan['label'] }}]
        name={{ chan['name'] }}
        enabled=1
        autorefresh=1
        baseurl=https://{{ salt['pillar.get']('pkg_download_point_host', 'uyuni.local') }}/rhn/manager/download/{{ chan['label'] }}?{{ chan['token'] }}
        gpgcheck=0
        type=rpm-md

        {% endfor %}
    - require:
      - pkg: sles16_migration_package

sles16_migration_disable_sp7_repos:
  file.managed:
    - name: /etc/zypp/repos.d/susemanager:channels.repo
    - contents: |
        # SP7 repos disabled during SLES 16 migration - managed by Uyuni
    - require:
      - file: sles16_migration_target_repos

sles16_migration_suseconnect_cleanup:
  cmd.run:
    - name: suseconnect --cleanup
    - require:
      - file: sles16_migration_disable_sp7_repos

sles16_migration_config:
  file.managed:
    - name: /etc/sle-migration-service.yml
    - source: salt://distupgrade/sles16_migration_config.yml.jinja
    - template: jinja
    - require:
      - cmd: sles16_migration_suseconnect_cleanup

sles16_migration_marker:
  file.managed:
    - name: /var/lib/uyuni/sles16_migration_started
    - makedirs: True
    - contents: |
        action_id: {{ salt['pillar.get']('action_id', 'unknown') }}
        started_timestamp: {{ salt['cmd.run']('date +%s', python_shell=False) }}
        source_version: {{ grains['osrelease'] }}
        source_arch: {{ grains['osarch'] }}
    - require:
      - file: sles16_migration_config

{% if is_s390x %}
sles16_migration_execute:
  cmd.run:
    - name: run_migration
    - require:
      - pkg: sles16_migration_package
      - file: sles16_migration_config
      - file: sles16_migration_marker
{% else %}
sles16_migration_reboot:
  cmd.run:
    - name: /usr/sbin/shutdown -r +1
    - bg: True
    - require:
      - pkg: sles16_migration_package
      - file: sles16_migration_config
      - file: sles16_migration_marker
{% endif %}

{% else %}
sles16_migration_error:
  test.fail_without_changes:
    - name: "This state is only supported on SLES 15 SP7"
{% endif %}
