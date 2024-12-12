#!/bin/bash
#
# This script create the necessary files to setup a "git_pillar" environment
# together with Uyuni in order to provide some extra pillar data.
#
# Use as:
#   salt_git_pillar_setup.sh setup -> Prepare environment
#   salt_git_pillar_setup.sh clean -> Clean up environment
#
# It prepares a GIT repository containing the following files:
#   top.sls
#   test_pillar.sls
#
# This provides "git_pillar_foobar: 12345" as part of the pillar data
# for all minions and master.
#

GIT_REPO="/tmp/test_salt_git_pillar.git"
MARKER_FILE="/tmp/sshd.pid"

if [ "$1" == "setup" ]; then
	echo "Setting up git_pillar environment and restarting Salt master and Salt API"
	zypper in -y git-core
	mkdir $GIT_REPO
	cd $GIT_REPO
	git init

	cat << 'EOF' > top.sls
base:
  '*':
    - test_pillar
EOF

	cat << 'EOF' > test_pillar.sls
git_pillar_foobar: 12345
EOF

	git add top.sls test_pillar.sls
	git commit -m "initial commit"

	# Store Salt SSH key as authorized for user root
	cp /root/.ssh/authorized_keys /root/.ssh/authorized_keys_backup_gitpillar
	cat /var/lib/salt/.ssh/mgr_ssh_id.pub >> /root/.ssh/authorized_keys

  # Start sshd if not already running
  if ! pgrep -x "sshd" > /dev/null; then
      ssh-keygen -A
      /usr/sbin/sshd -D &
      SSHD_PID=$!
      echo $SSHD_PID > "$MARKER_FILE"
  fi

	cat << 'EOF' > /etc/salt/master.d/zz-testing-gitpillar.conf
ext_pillar:
  - suma_minion: True
  - git:
    - __env__ ssh://root@localhost/tmp/test_salt_git_pillar.git:
      - all_saltenvs: master
      - pubkey: /var/lib/salt/.ssh/mgr_ssh_id.pub
      - privkey: /var/lib/salt/.ssh/mgr_ssh_id
EOF

	systemctl restart salt-master salt-api
fi

if [ "$1" == "clean" ]; then
	echo "Cleaning git_pillar environment and restarting Salt master and Salt API"
	rm -rf $GIT_REPO
	rm /etc/salt/master.d/zz-testing-gitpillar.conf
	cp /root/.ssh/authorized_keys_backup_gitpillar /root/.ssh/authorized_keys
	rm /root/.ssh/authorized_keys_backup_gitpillar
  # Check if we started sshd and stop it
  if [ -f "$MARKER_FILE" ]; then
      SSHD_PID=$(cat "$MARKER_FILE")
      if ps -p "$SSHD_PID" > /dev/null 2>&1; then
          kill "$SSHD_PID"
      fi
      rm -f "$MARKER_FILE"
  else
      echo "No marker file found. sshd might not have been started by this script."
  fi
	systemctl restart salt-master salt-api
fi
