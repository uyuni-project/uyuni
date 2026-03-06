#!/bin/bash
set -xe

# When we run docker inside podman, we will use the docker daemon from
# the ubuntu runner.
# So, when we run docker build or docker pull, this commands will
# contact the docker daemon in the ubuntu runner.
# From the ubuntu runner, we can't access the registries, because
# they are in the "podman network".
# Thus, we need to expose the ports from the registries into "localhost"
# and connect to localhost. This is why we need the registry hostnames to resolve
# into localhost.
# Moreover, because of a known bug in python-docker, we can't use the port
# number when doing a docker build. This is why we have an nginx, to act as a proxy
# and redirect the requests to the port, depending on the hostname.

# OS detection
OS="unknown"
if [[ "$(uname)" == "Darwin" ]]; then
    OS="darwin"
elif [[ -f /etc/os-release ]]; then
    . /etc/os-release
    if [[ "$ID" == "ubuntu" ]]; then
        OS="ubuntu"
    elif [[ "$ID" == "opensuse-leap" || "$ID" == "opensuse-tumbleweed" || "$ID_LIKE" == *"suse"* ]]; then
        OS="opensuse"
    fi
fi

function install_nginx() {
    if ! command -v nginx &>/dev/null; then
        case "$OS" in
            darwin)
                brew install nginx
                ;;
            ubuntu)
                sudo apt -y install nginx
                ;;
            opensuse)
                sudo zypper -n install nginx
                ;;
            *)
                echo "Unsupported OS for nginx installation"
                exit 1
                ;;
        esac
    fi
}

function configure_nginx() {
    if ! grep -q "authregistry.lab" /etc/hosts; then
        echo "127.0.0.1 authregistry.lab" | sudo tee -a /etc/hosts
    fi
    if ! grep -q "noauthregistry.lab" /etc/hosts; then
        echo "127.0.0.1 noauthregistry.lab" | sudo tee -a /etc/hosts
    fi

    SUFFIX=""
    case "$OS" in
        darwin)
            NGINX_SITES=/opt/homebrew/etc/nginx/servers
            ;;
        ubuntu)
            NGINX_SITES=/etc/nginx/sites-enabled
            ;;
        opensuse)
            NGINX_SITES="/etc/nginx/vhosts.d"
            SUFFIX=".conf"
            ;;
        *)
            # Fallback logic if OS detection failed but we are on Linux
            NGINX_SITES=/etc/nginx/sites-enabled
            if [[ ! -d "$NGINX_SITES" ]]; then
                NGINX_SITES="/etc/nginx/vhosts.d"
                SUFFIX=".conf"
            fi
            ;;
    esac

    sudo mkdir -p "$NGINX_SITES"

    sudo tee "$NGINX_SITES/registry$SUFFIX" <<EOF
server {
        listen 80;
        server_name authregistry.lab;
        
        location / {
                proxy_pass http://127.0.0.1:5001;
                proxy_set_header Host \$host;
        }
        client_max_body_size 0;
        proxy_read_timeout 300;
        proxy_connect_timeout 300;
}
server {
        listen 443 ssl;
        server_name authregistry.lab;
        ssl_certificate_key /tmp/testing/server-nginx.key;
        ssl_certificate /tmp/testing/server-nginx.crt;

        location / {
                proxy_pass http://127.0.0.1:5001;
                proxy_set_header Host \$host;
                proxy_set_header X-Real-IP \$remote_addr;
                proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
                proxy_set_header X-Forwarded-Proto \$scheme;
                proxy_set_header X-Forwarded-Port 443;
                proxy_ssl_server_name on;
                proxy_ssl_verify off;
                proxy_http_version 1.1;
                proxy_set_header Connection "";
        }
        client_max_body_size 0;
        proxy_read_timeout 300;
        proxy_connect_timeout 300;
}

server {
        listen 80;
        server_name noauthregistry.lab;
        
        location / {
                proxy_pass http://127.0.0.1:5002;
                proxy_set_header Host \$host;
        }
        client_max_body_size 0;
        proxy_read_timeout 300;
        proxy_connect_timeout 300;
}
server {
        listen 443 ssl;
        server_name noauthregistry.lab;
        ssl_certificate_key /tmp/testing/server-nginx.key;
        ssl_certificate /tmp/testing/server-nginx.crt;

        location / {
                proxy_pass http://127.0.0.1:5002;
                proxy_set_header Host \$host;
                proxy_set_header X-Real-IP \$remote_addr;
                proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
                proxy_set_header X-Forwarded-Proto \$scheme;
                proxy_set_header X-Forwarded-Port 443;
                proxy_ssl_server_name on;
                proxy_ssl_verify off;
                proxy_http_version 1.1;
                proxy_set_header Connection "";
        }
        client_max_body_size 0;
        proxy_read_timeout 300;
        proxy_connect_timeout 300;
}
EOF
}

function setup_systemd_override() {
    if [[ "$OS" == "opensuse" || "$OS" == "ubuntu" ]]; then
        # On OpenSUSE, we need to ensure nginx can access certs in /tmp/testing
        # PrivateTmp=false is needed.
        sudo mkdir -p /etc/systemd/system/nginx.service.d
        sudo tee /etc/systemd/system/nginx.service.d/override.conf <<EOF
[Service]
PrivateTmp=false
EOF
        sudo systemctl daemon-reload
    fi
}

function start_nginx() {
    case "$OS" in
        darwin)
            brew services restart nginx
            ;;
        *)
            sudo systemctl restart nginx || {
                sudo systemctl status nginx.service
                sudo journalctl -xeu nginx.service
                exit 1
            }
            ;;
    esac
}

# Main
install_nginx
configure_nginx
setup_systemd_override
start_nginx
