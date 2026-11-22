#!/bin/bash
set -euo pipefail
dnf install -y docker nginx unzip python3
systemctl enable --now docker amazon-ssm-agent
mkdir -p /opt/foros-s5/private
chmod 700 /opt/foros-s5/private
docker network inspect foros-s5 >/dev/null 2>&1 || docker network create foros-s5
if ! command -v aws >/dev/null; then
  curl -fsSL https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip -o /tmp/awscliv2.zip
  unzip -q /tmp/awscliv2.zip -d /tmp/aws-cli-install
  /tmp/aws-cli-install/aws/install
fi
cat > /etc/nginx/conf.d/foros-s5.conf <<'NGINX'
server {
    listen 80 default_server;
    server_name _;
    server_tokens off;
    location /api/usuarios/ {
        proxy_pass http://127.0.0.1:8081/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
    location /api/foro/ {
        proxy_pass http://127.0.0.1:8082/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
    location / {
        proxy_pass http://127.0.0.1:4300;
        proxy_set_header Host $host;
    }
}
NGINX
# Amazon Linux incluye un servidor predeterminado; usamos solo el del proyecto.
cat > /etc/nginx/nginx.conf <<'NGINX'
user nginx;
worker_processes auto;
error_log /var/log/nginx/error.log;
pid /run/nginx.pid;
events { worker_connections 1024; }
http {
    include /etc/nginx/mime.types;
    default_type application/octet-stream;
    access_log /var/log/nginx/access.log;
    sendfile on;
    include /etc/nginx/conf.d/*.conf;
}
NGINX
nginx -t
systemctl enable --now nginx
touch /opt/foros-s5/bootstrap-completo
