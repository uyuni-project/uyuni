#!/usr/bin/python3

import os
import re
import shutil
import yaml

# read from file
with open("/etc/uyuni/config.yaml") as source:
    config = yaml.safe_load(source)

    SSH_PUSH_KEY_FILE="id_susemanager_ssh_push"
    SSH_PUSH_USER="mgrsshtunnel"
    SSH_PUSH_USER_HOME=f"/var/lib/spacewalk/{SSH_PUSH_USER}"
    SSH_PUSH_KEY_DIR=f"{SSH_PUSH_USER_HOME}/.ssh"

    # create ssh push tunnel user
    os.system(f'groupadd -r {SSH_PUSH_USER}')
    os.system(f'useradd -r -g {SSH_PUSH_USER} -m -d {SSH_PUSH_USER_HOME} -c "susemanager ssh push tunnel" {SSH_PUSH_USER}')


    # create .ssh dir in home and set permissions
    os.makedirs(SSH_PUSH_KEY_DIR)
    os.system(f'chown {SSH_PUSH_USER}:{SSH_PUSH_USER} {SSH_PUSH_KEY_DIR}')
    os.system(f'chmod 700 {SSH_PUSH_KEY_DIR}')

    # copy the ssh push server/parent key files
    shutil.copyfile("/etc/uyuni/server_ssh_push", f"{SSH_PUSH_KEY_DIR}/{SSH_PUSH_KEY_FILE}")
    shutil.copyfile("/etc/uyuni/server_ssh_push.pub", f"{SSH_PUSH_KEY_DIR}/{SSH_PUSH_KEY_FILE}.pub")

    
    # change owner to {SSH_PUSH_USER}
    os.system(f'chown {SSH_PUSH_USER}:{SSH_PUSH_USER} {SSH_PUSH_KEY_DIR}/{SSH_PUSH_KEY_FILE}')
    os.system(f'chmod 600 {SSH_PUSH_KEY_DIR}/{SSH_PUSH_KEY_FILE}')
    os.system(f'chown {SSH_PUSH_USER}:{SSH_PUSH_USER} {SSH_PUSH_KEY_DIR}/{SSH_PUSH_KEY_FILE}.pub')
    os.system(f'chmod 644 {SSH_PUSH_KEY_DIR}/{SSH_PUSH_KEY_FILE}.pub')

    # TODO
    # copy the public key to apache's pub dir
    # cp {SSH_PUSH_KEY_DIR}/{SSH_PUSH_KEY_FILE}.pub ${HTMLPUB_DIR}/

    # Authorize the server to ssh into this container
    shutil.copyfile("/etc/uyuni/server_ssh_key.pub", f"{SSH_PUSH_KEY_DIR}/authorized_keys")

    # append to existing config file
    with open("/etc/ssh/sshd_config", "r+") as config_file:
        file_content = config_file.read()
        file_content = re.sub(r"#HostKey .*", f"HostKey {SSH_PUSH_KEY_DIR}/{SSH_PUSH_KEY_FILE}", file_content, 1)
        # writing back the content
        config_file.seek(0,0)
        config_file.write(file_content)
        config_file.truncate()
        config_file.write(f'''Match user {SSH_PUSH_USER}
        ForceCommand /usr/sbin/mgr-proxy-ssh-force-cmd
        KbdInteractiveAuthentication no
        PasswordAuthentication no
        PubkeyAuthentication yes
        X11Forwarding no
        PermitTTY no
        ''')

# Note: we don't need pam_loginuid.so module to be loaded. In the container's world users from the outside to the inside are different,
# and they do not match with the pam_loginuid module's logic behavior. The module makes the sshd process to crash if it fails itself, but
# inside the container we will have always and only the root user to start only the sshd process and nothing else. We can safely disable it
#
# References to the explanation as above:
# The bug report - https://bugs.debian.org/cgi-bin/bugreport.cgi?bug=726661#41
# The pam module - https://man7.org/linux/man-pages/man8/pam_loginuid.8.html
# Statement of "not being a bug", just a container configuration issue - https://github.com/containers/podman/issues/3651#issuecomment-549031347
# So, we configure it to disable the module
#
# edit the existing config file
with open("/etc/pam.d/sshd", "r+") as config_file:
    file_content = config_file.read()
    file_content = re.sub(r"(session( *)required( *)pam_loginuid.so)", "#\g<0>", file_content)
    config_file.seek(0,0)
    config_file.write(file_content)
    config_file.truncate()
