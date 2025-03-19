{%- set mgrpxy_installed = salt['pkg.version']('mgrpxy') %}
{%- set mgrpxy_status_output = salt['cmd.run']('mgrpxy status 2>&1', python_shell=True) %}
{%- set mgrpxy_operation = 'install' if not mgrpxy_installed or 'Error: no installed proxy detected' in mgrpxy_status_output else 'upgrade' %}
{%- set transactional = grains['transactional'] %}

podman_installed:
  pkg.installed:
    - name: podman

mgrpxy_installed:
  pkg.installed:
    - name: mgrpxy
    - refresh: True

/etc/uyuni/proxy/config.yaml:
  file.managed:
    - name: /etc/uyuni/proxy/config.yaml
    - user: root
    - group: root
    - mode: 644
    - makedirs: True
    - template: jinja
    - contents: |
        server: {{ pillar['server'] }}
        ca_crt: |
          {{ pillar['ca_crt'] | replace('\\n', '\n') | indent(12) }}
        proxy_fqdn: {{ pillar['proxy_fqdn'] }}
        max_cache_size_mb: {{ pillar['max_cache_size_mb'] }}
        server_version: "{{ pillar['server_version'] }}"
        email: {{ pillar['email'] }}

/etc/uyuni/proxy/httpd.yaml:
  file.managed:
    - name: /etc/uyuni/proxy/httpd.yaml
    - user: root
    - group: root
    - mode: 600
    - makedirs: True
    - template: jinja
    - contents: |
        httpd:
          system_id: {{ pillar['httpd']['system_id'] }}
          server_crt: |
            {{ pillar['httpd']['server_crt'] | replace('\\n', '\n') | indent(12) }}
          server_key: |
            {{ pillar['httpd']['server_key'] | replace('\\n', '\n') | indent(12) }}

/etc/uyuni/proxy/ssh.yaml:
  file.managed:
    - name: /etc/uyuni/proxy/ssh.yaml
    - user: root
    - group: root
    - mode: 600
    - makedirs: True
    - template: jinja
    - contents: |
        ssh:
          server_ssh_key_pub: |
            {{ pillar['ssh']['server_ssh_key_pub'] | replace('\\n', '\n') | indent(12) }}
          server_ssh_push: |
            {{ pillar['ssh']['server_ssh_push'] | replace('\\n', '\n') | indent(12) }}
          server_ssh_push_pub: |
            {{ pillar['ssh']['server_ssh_push_pub'] | replace('\\n', '\n') | indent(12) }}    


{% set args = [] %}
{% if pillar['httpd_image'] is defined and pillar['httpd_tag'] is defined %}
  {% do args.append("--httpd-image " ~ pillar['httpd_image'] ~ " --httpd-tag " ~ pillar['httpd_tag']) %}
{% endif %}
{% if pillar['saltbroker_image'] is defined and pillar['saltbroker_tag'] is defined %}
  {% do args.append("--saltbroker-image " ~ pillar['saltbroker_image'] ~ " --saltbroker-tag " ~ pillar['saltbroker_tag']) %}
{% endif %}
{% if pillar['squid_image'] is defined and pillar['squid_tag'] is defined %}
  {% do args.append("--squid-image " ~ pillar['squid_image'] ~ " --squid-tag " ~ pillar['squid_tag']) %}
{% endif %}
{% if pillar['ssh_image'] is defined and pillar['ssh_tag'] is defined %}
  {% do args.append("--ssh-image " ~ pillar['ssh_image'] ~ " --ssh-tag " ~ pillar['ssh_tag']) %}
{% endif %}
{% if pillar['tftpd_image'] is defined and pillar['tftpd_tag'] is defined %}
  {% do args.append("--tftpd-image " ~ pillar['tftpd_image'] ~ " --tftpd-tag " ~ pillar['tftpd_tag']) %}
{% endif %}

{% if transactional %}

# If we're on a transactional system, we'll install mgrpxy apply as a service that
# executes the mgrpxy install/update command after next reboot
/etc/systemd/system/apply_proxy_config.service:
  file.managed:
    - name: /etc/systemd/system/apply_proxy_config.service
    - user: root
    - group: root
    - mode: 664
    - makedirs: True
    - template: jinja
    - contents: |
        [Unit]
        Description=Install/Update mgrpxy proxy
        After=network-online.target podman.service
        Requires=network-online.target podman.service

        [Service]
        Type=oneshot
        ExecStart=/bin/bash -c 'mgrpxy {{ mgrpxy_operation }} podman --logLevel debug {{ args | join(" ") }} 2>&1 | tee -a /var/log/mgrpxy_install.log'
        
        ExecStartPost=/bin/bash -c 'STATUS_OUTPUT=$(mgrpxy status 2>&1); \
            echo "$STATUS_OUTPUT" | tee -a /var/log/mgrpxy_install.log; \
            if ! echo "$STATUS_OUTPUT" | grep -q "Error: no installed proxy detected"; then \
                echo "mgrpxy was successfully {{ mgrpxy_operation }}ed. Removing apply mgrpxy service and configuration file." | tee -a /var/log/mgrpxy_install.log; \
                rm -f /etc/systemd/system/apply_proxy_config.service; \
            else \
                echo "mgrpxy status check failed. Service file will remain for troubleshooting." | tee -a /var/log/mgrpxy_install.log; \
            fi'

        [Install]
        WantedBy=multi-user.target
    - require:
      - file: /etc/uyuni/proxy/config.yaml
      - file: /etc/uyuni/proxy/httpd.yaml
      - file: /etc/uyuni/proxy/ssh.yaml
      - pkg: podman_installed
      - pkg: mgrpxy_installed

# The system will run this service to enable apply_proxy_config.service after reboot
enable_apply_proxy_config_service:
  cmd.run:
    - name: systemctl enable apply_proxy_config.service
    - require:
      - file: /etc/systemd/system/apply_proxy_config.service

{% else %}

apply_proxy_configuration:
  cmd.run:
    - name: >
        mgrpxy {{ mgrpxy_operation }} podman --logLevel debug {{ args | join(" ") }} 
        2>&1 | tee -a /var/log/mgrpxy_install.log
    - shell: /bin/bash
    - require:
      - file: /etc/uyuni/proxy/config.yaml
      - file: /etc/uyuni/proxy/httpd.yaml
      - file: /etc/uyuni/proxy/ssh.yaml
      - pkg: podman_installed
      - pkg: mgrpxy_installed

{%- endif %}
