#!/bin/bash
set -x

if [[ "$(uname)" == "Darwin" ]]; then
  PODMAN_CMD="podman"
else
  PODMAN_CMD="sudo -i podman"
fi

if [ $# -ne 1 ];
then
	echo "Usage: ${0} server_id"
	echo "server_id is used for creating a unique folder"
	exit 1
fi

src_dir=$(cd $(dirname "$0")/../.. && pwd -P)
server_id=${1}
log_dir=/tmp/testing/server-logs/${server_id}

rm -rfv "${log_dir}"
mkdir -p "${log_dir}"

sudo -i journalctl > "${log_dir}/journalctl.log" || true

# Collect logs from the var-log volume (works even if container is stopped)
VOLUME_NAME=$($PODMAN_CMD volume ls --format '{{.Name}}' 2>/dev/null | grep -E '^var-log$' | head -1 || true)
if [ -n "$VOLUME_NAME" ]; then
  VOLUME_BASE=$($PODMAN_CMD volume inspect "$VOLUME_NAME" --format '{{.Mountpoint}}' 2>/dev/null || true)
  if [ -n "$VOLUME_BASE" ] && sudo test -d "$VOLUME_BASE"; then
    echo "Collecting logs from volume '$VOLUME_NAME' at: $VOLUME_BASE"
    if sudo test -f "$VOLUME_BASE/rhn/rhn_web_ui.log"; then
      sudo cp "$VOLUME_BASE/rhn/rhn_web_ui.log" "${log_dir}/rhn_web_ui.log" || true
    fi
    for d in tomcat rhn; do
      if sudo test -d "$VOLUME_BASE/$d"; then
        mkdir -p "${log_dir}/$d"
        sudo cp -r "$VOLUME_BASE/$d/." "${log_dir}/$d/" || true
      fi
    done
  fi
fi

# Also try collecting from the container directly (works on stopped containers)
$PODMAN_CMD cp server:/var/log/rhn/rhn_web_ui.log "${log_dir}/rhn_web_ui.log" 2>/dev/null || true
$PODMAN_CMD cp server:/var/log/tomcat/. "${log_dir}/tomcat/" 2>/dev/null || true

# prevent supportconfig exit code 1 on missing logger bsc#1245667
$PODMAN_CMD exec server bash -c "ln -sf /usr/bin/echo /usr/bin/logger" || true
$PODMAN_CMD exec server bash -c "supportconfig -R /tmp/server-logs/${server_id} && chmod 644 /tmp/server-logs/${server_id}/*.txz*" || true

# Make everything readable
sudo chown -R "$(id -u):$(id -g)" "${log_dir}" 2>/dev/null || true
sudo chmod -R a+rX "${log_dir}" 2>/dev/null || true

echo "Log collection complete: ${log_dir}/"
ls -la "${log_dir}/" || true
