{%- set mgrpxy_installed = salt['pkg.version']('mgrpxy') %}
{%- set mgrpxy_status_output = salt['cmd.run']('mgrpxy status 2>&1', python_shell=True) %}
{%- set mgrpxy_operation = 'install' if not mgrpxy_installed or 'Error: no installed proxy detected' in mgrpxy_status_output else 'upgrade' %}
{%- set transactional = grains['transactional'] %}
{%- set installPackages = not (pillar.get('registries') is mapping and pillar.get('registries') | length > 0) %}

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
          {{ pillar['ca_crt'] | replace('\\n', '\n') | indent(10) }}
        proxy_fqdn: {{ pillar['proxy_fqdn'] }}
        max_cache_size_mb: {{ pillar['max_cache_size_mb']|int }}
        server_version: "{{ pillar['server_version'] }}"
        email: {{ pillar['email'] }}
        replace_fqdns: {{ pillar['replace_fqdns'] }}

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

{% if installPackages %}

{%- set matched_pkgs_regex = salt['pkg.search']('suse-multi-linux-manager-*proxy*-image', regex=True) or {} %}
{%- set pkg_names = matched_pkgs_regex.keys() | list %}

install_proxy_packages:
  pkg.installed:
    - pkgs:
      {%- for pkg in pkg_names %}
      - {{ pkg }}
      {%- endfor %}
    - refresh: True

{% endif %}

{% set args = [] %}
{% if salt['pillar.get']('registries:proxy-httpd:url') and salt['pillar.get']('registries:proxy-httpd:tag') %}
  {% do args.append("--httpd-image " ~ salt['pillar.get']('registries:proxy-httpd:url') ~ " --httpd-tag " ~ salt['pillar.get']('registries:proxy-httpd:tag')) %}
{% endif %}
{% if salt['pillar.get']('registries:proxy-salt-broker:url') and salt['pillar.get']('registries:proxy-salt-broker:tag') %}
  {% do args.append("--saltbroker-image " ~ salt['pillar.get']('registries:proxy-salt-broker:url') ~ " --saltbroker-tag " ~ salt['pillar.get']('registries:proxy-salt-broker:tag')) %}
{% endif %}
{% if salt['pillar.get']('registries:proxy-squid:url') and salt['pillar.get']('registries:proxy-squid:tag') %}
  {% do args.append("--squid-image " ~ salt['pillar.get']('registries:proxy-squid:url') ~ " --squid-tag " ~ salt['pillar.get']('registries:proxy-squid:tag')) %}
{% endif %}
{% if salt['pillar.get']('registries:proxy-ssh:url') and salt['pillar.get']('registries:proxy-ssh:tag') %}
  {% do args.append("--ssh-image " ~ salt['pillar.get']('registries:proxy-ssh:url') ~ " --ssh-tag " ~ salt['pillar.get']('registries:proxy-ssh:tag')) %}
{% endif %}
{% if salt['pillar.get']('registries:proxy-tftpd:url') and salt['pillar.get']('registries:proxy-tftpd:tag') %}
  {% do args.append("--tftpd-image " ~ salt['pillar.get']('registries:proxy-tftpd:url') ~ " --tftpd-tag " ~ salt['pillar.get']('registries:proxy-tftpd:tag')) %}
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
        ExecStart=/bin/bash -c '/usr/bin/mgrpxy {{ mgrpxy_operation }} podman --logLevel debug {{ args | join(" ") }} 2>&1 | /usr/bin/tee -a /var/log/mgrpxy_install.log'
        
        ExecStartPost=/bin/bash -c 'STATUS_OUTPUT=$(mgrpxy status 2>&1); \
            /usr/bin/echo "$STATUS_OUTPUT" | /usr/bin/tee -a /var/log/mgrpxy_install.log; \
            if ! /usr/bin/echo "$STATUS_OUTPUT" | /usr/bin/grep -q "Error: no installed proxy detected"; then \
                /usr/bin/echo "mgrpxy was successfully {{ mgrpxy_operation }}ed. Removing apply mgrpxy service and configuration file." | /usr/bin/tee -a /var/log/mgrpxy_install.log; \
                /usr/bin/rm -f /etc/systemd/system/apply_proxy_config.service; \
            else \
                /usr/bin/echo "mgrpxy status check failed. Service file will remain for troubleshooting." | /usr/bin/tee -a /var/log/mgrpxy_install.log; \
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
    - name: /usr/bin/systemctl enable apply_proxy_config.service
    - require:
      - file: /etc/systemd/system/apply_proxy_config.service

{% else %}

apply_proxy_configuration:
  cmd.run:
    - name: >
        /usr/bin/mgrpxy {{ mgrpxy_operation }} podman --logLevel debug {{ args | join(" ") }} 
        2>&1 | /usr/bin/tee -a /var/log/mgrpxy_install.log
    - shell: /bin/bash
    - require:
      - file: /etc/uyuni/proxy/config.yaml
      - file: /etc/uyuni/proxy/httpd.yaml
      - file: /etc/uyuni/proxy/ssh.yaml
      - pkg: podman_installed
      - pkg: mgrpxy_installed

{%- endif %}
