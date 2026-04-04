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

echo "127.0.0.1 authregistry.lab" | sudo tee -a /etc/hosts
echo "127.0.0.1 noauthregistry.lab" | sudo tee -a /etc/hosts

if ! command -v nginx &>/dev/null; then
  if [[ "$(uname)" == "Darwin" ]]; then
    brew install nginx
  else
    sudo apt -y install nginx
  fi
fi

if [[ "$(uname)" == "Darwin" ]]; then
  NGINX_SITES=/opt/homebrew/etc/nginx/servers
else
  NGINX_SITES=/etc/nginx/sites-enabled
fi

sudo tee $NGINX_SITES/registry <<EOF
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

if [[ "$(uname)" == "Darwin" ]]; then
  brew services restart nginx
else
  sudo systemctl restart nginx || systemctl status nginx.service && journalctl -xeu nginx.service
fi