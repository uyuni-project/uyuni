#!jinja|yaml
# Save the existing proxy config files BEFORE the RKE2 wipe, then extract
# certs for the helm chart.
#
# Config sources (tried in order):
# 1. /etc/uyuni/proxy/ — persistent copy from a previous run
# 2. Running k8s pod with an httpd container in the target namespace
#
# Whichever source succeeds, the files are ALSO saved to /etc/uyuni/proxy/
# so they survive future wipes.
#
# This state MUST run before rke2.sls wipes the cluster.

{%- set work_dir  = salt['temp.dir']('', 'rke2-proxy-') %}
{%- do salt['grains.set']('rke2_proxy:work_dir', work_dir) %}

{%- set persist   = salt['pillar.get']('rke2_proxy:config_dir', '/etc/uyuni/proxy') %}
{%- set namespace = salt['pillar.get']('rke2_proxy:namespace', 'uyuni-proxy') %}
{%- set kubectl   = '/var/lib/rancher/rke2/bin/kubectl --kubeconfig=/etc/rancher/rke2/rke2.yaml' %}

rke2_proxy_backup_config:
  cmd.run:
    - name: |
        mkdir -p {{ persist }}

        # Try 1: persistent copy from a previous run (survives wipe)
        if [ -f {{ persist }}/config.yaml ]; then
            cp {{ persist }}/config.yaml {{ work_dir }}/config.yaml
            cp {{ persist }}/httpd.yaml  {{ work_dir }}/httpd.yaml
            cp {{ persist }}/ssh.yaml    {{ work_dir }}/ssh.yaml
            echo "Using config from {{ persist }}/"
            exit 0
        fi

        # Try 2: extract from any running proxy pod with an httpd container.
        # The deployment name may differ between chart versions.
        if [ -x /var/lib/rancher/rke2/bin/kubectl ] && [ -f /etc/rancher/rke2/rke2.yaml ]; then
            pod=$({{ kubectl }} -n {{ namespace }} get pod \
                -l app.kubernetes.io/component=proxy \
                -o jsonpath='{.items[0].metadata.name}' 2>/dev/null)
            if [ -n "$pod" ]; then
                for f in config.yaml httpd.yaml ssh.yaml; do
                    {{ kubectl }} -n {{ namespace }} exec "$pod" -c httpd \
                        -- cat /etc/uyuni/$f > {{ work_dir }}/$f 2>/dev/null
                done
                if [ -s {{ work_dir }}/config.yaml ]; then
                    cp {{ work_dir }}/config.yaml {{ persist }}/config.yaml
                    cp {{ work_dir }}/httpd.yaml  {{ persist }}/httpd.yaml
                    cp {{ work_dir }}/ssh.yaml    {{ persist }}/ssh.yaml
                    chmod 600 {{ persist }}/httpd.yaml {{ persist }}/ssh.yaml
                    echo "Backed up config from pod $pod → {{ persist }}/"
                    exit 0
                fi
            fi
        fi

        echo "ERROR: no existing proxy config found"
        echo "Place config.yaml, httpd.yaml, ssh.yaml in {{ persist }}/ first"
        exit 1
    - creates: {{ work_dir }}/config.yaml

rke2_proxy_config_extracted:
  cmd.run:
    - name: |
        cd {{ work_dir }}
        python3 -c "
        import yaml
        with open('httpd.yaml') as f:
            h = yaml.safe_load(f)['httpd']
        crt = h['server_crt']
        key = h['server_key']
        for name, val in [('server.crt', crt), ('server.key', key)]:
            pem = val[val.index('-----BEGIN'):].rstrip()
            with open(name, 'w') as out:
                out.write(pem + '\n')
        with open('config.yaml') as f:
            ca = yaml.safe_load(f)['ca_crt']
        pem = ca[ca.index('-----BEGIN'):].rstrip()
        with open('ca.crt', 'w') as out:
            out.write(pem + '\n')
        "
    - creates: {{ work_dir }}/server.crt
    - require:
      - cmd: rke2_proxy_backup_config
