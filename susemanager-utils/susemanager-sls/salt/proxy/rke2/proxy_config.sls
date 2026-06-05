#!jinja|yaml
# Save the existing proxy config files BEFORE the RKE2 wipe, then extract
# certs for the helm chart.
#
# Config sources (tried in order):
# 1. /etc/uyuni/proxy/ — persistent copy from a previous run
# 2. Running k8s pod (k3s or RKE2, using the pillar namespace)
#
# Whichever source succeeds, the files are ALSO saved to /etc/uyuni/proxy/
# so they survive future wipes.
#
# This state MUST run before rke2.sls wipes the cluster.

{%- set work_dir   = salt['temp.dir']('', 'rke2-proxy-') %}
{%- do salt['grains.set']('proxy:rke2:work_dir', work_dir) %}

{%- set persist    = salt['pillar.get']('proxy:rke2:config_dir', '/etc/uyuni/proxy') %}
{%- set namespace  = salt['pillar.get']('proxy:rke2:namespace', 'uyuni-proxy') %}

proxy_rke2_backup_config:
  cmd.run:
    - name: |
        mkdir -p {{ persist }}

        # Try 1: persistent copy from a previous run (survives wipe)
        if [ -f {{ persist }}/config.yaml ] && [ -s {{ persist }}/config.yaml ] \
           && [ -s {{ persist }}/httpd.yaml ] && [ -s {{ persist }}/ssh.yaml ]; then
            cp {{ persist }}/config.yaml {{ work_dir }}/config.yaml
            cp {{ persist }}/httpd.yaml  {{ work_dir }}/httpd.yaml
            cp {{ persist }}/ssh.yaml    {{ work_dir }}/ssh.yaml
            echo "Using config from {{ persist }}/"
            exit 0
        fi

        # Try 2: extract from k8s cluster.
        KUBECTL=""
        if [ -x /var/lib/rancher/rke2/bin/kubectl ] && [ -f /etc/rancher/rke2/rke2.yaml ]; then
            KUBECTL="/var/lib/rancher/rke2/bin/kubectl --kubeconfig=/etc/rancher/rke2/rke2.yaml"
        elif [ -f /etc/rancher/k3s/k3s.yaml ]; then
            KUBECTL="k3s kubectl"
        fi
        if [ -n "$KUBECTL" ]; then
            # Try 2a: k8s resources directly (5.2 — ConfigMap + Secrets).
            # Works even when pods are crashing.
            $KUBECTL -n {{ namespace }} get configmap proxy-configmap \
                -o jsonpath='{.data.config\.yaml}' > {{ work_dir }}/config.yaml 2>/dev/null
            $KUBECTL -n {{ namespace }} get secret httpd \
                -o jsonpath='{.data.httpd\.yaml}' 2>/dev/null | base64 -d > {{ work_dir }}/httpd.yaml
            $KUBECTL -n {{ namespace }} get secret ssh \
                -o jsonpath='{.data.ssh\.yaml}' 2>/dev/null | base64 -d > {{ work_dir }}/ssh.yaml

            # Try 2b: pod exec fallback (5.1 — different resource layout).
            # httpd container has config.yaml + httpd.yaml, ssh container has ssh.yaml.
            if [ ! -s {{ work_dir }}/config.yaml ] || [ ! -s {{ work_dir }}/httpd.yaml ] || [ ! -s {{ work_dir }}/ssh.yaml ]; then
                pod=""
                for label in app.kubernetes.io/component=proxy app=uyuni-proxy; do
                    pod=$($KUBECTL -n {{ namespace }} get pod \
                        -l "$label" \
                        -o jsonpath='{.items[0].metadata.name}' 2>/dev/null)
                    [ -n "$pod" ] && break
                done
                if [ -n "$pod" ]; then
                    for f in config.yaml httpd.yaml; do
                        [ -s {{ work_dir }}/$f ] && continue
                        $KUBECTL -n {{ namespace }} exec "$pod" -c httpd \
                            -- cat /etc/uyuni/$f > {{ work_dir }}/$f 2>/dev/null
                    done
                    if [ ! -s {{ work_dir }}/ssh.yaml ]; then
                        $KUBECTL -n {{ namespace }} exec "$pod" -c ssh \
                            -- cat /etc/uyuni/ssh.yaml > {{ work_dir }}/ssh.yaml 2>/dev/null
                    fi
                fi
            fi

            # Validate all three files are non-empty
            ok=true
            for f in config.yaml httpd.yaml ssh.yaml; do
                if [ ! -s {{ work_dir }}/$f ]; then
                    echo "WARNING: $f is empty after extraction"
                    ok=false
                fi
            done
            if $ok; then
                cp {{ work_dir }}/config.yaml {{ persist }}/config.yaml
                cp {{ work_dir }}/httpd.yaml  {{ persist }}/httpd.yaml
                cp {{ work_dir }}/ssh.yaml    {{ persist }}/ssh.yaml
                chmod 600 {{ persist }}/httpd.yaml {{ persist }}/ssh.yaml
                echo "Backed up config → {{ persist }}/"
                exit 0
            fi
        fi

        echo "ERROR: no existing proxy config found"
        echo "Place config.yaml, httpd.yaml, ssh.yaml in {{ persist }}/ first"
        exit 1
    - creates: {{ work_dir }}/config.yaml

proxy_rke2_config_extracted:
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
      - cmd: proxy_rke2_backup_config
