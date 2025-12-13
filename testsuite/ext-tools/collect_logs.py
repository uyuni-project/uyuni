#!/usr/bin/env python3

# Copyright (c) 2025 SUSE LLC
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.

import paramiko
import argparse
import os
import sys
import re

def get_os_family(client):
    """
    Connects to the remote machine and extracts the OS family.

    Args:
        client: A Paramiko SSHClient object.

    Returns:
        A string representing the OS family, or None if it cannot be determined.
    """
    try:
        _stdin, _stdout, _stderr = client.exec_command('grep "^ID=" /etc/os-release')
        os_family_raw = _stdout.read().decode().strip()
        if os_family_raw:
            os_family = os_family_raw.split('=')[1].replace('"', '')
            return os_family.lower()

    except Exception as e:
        print(f"Error getting OS family: {e}")
        return None
    return None

def is_transactional_system(client):
    """
    Checks if the remote system is a transactional one (SLE Micro or similar).

    Args:
        client: A Paramiko SSHClient object.

    Returns:
        A boolean indicating if the system is transactional.
    """
    try:
        _stdin, _stdout, _stderr = client.exec_command('command -v transactional-update')
        if _stdout.read().decode().strip():
            return True

        os_family = get_os_family(client)
        if os_family:
            if 'sle-micro' in os_family or 'suse-microos' in os_family:
                return True

    except Exception as e:
        print(f"Error checking for transactional system: {e}")
    return False

def extract_logs_from_node(fqdn, username, password, key_filename):
    """
    Connects to a remote node, collects logs, and transfers them locally.

    Args:
        fqdn (str): The fully qualified domain name of the remote host.
        username (str): The username for the SSH connection.
        password (str): The password for the SSH connection.
        key_filename (str): The path to the SSH private key file.
    """
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())

    try:
        ssh_config_file = os.path.expanduser("~/.ssh/config")
        hostname = fqdn
        port = 22

        if os.path.exists(ssh_config_file):
            config = paramiko.SSHConfig()
            with open(ssh_config_file) as f:
                config.parse(f)
            host_config = config.lookup(fqdn)

            if host_config:
                hostname = host_config.get('hostname', fqdn)
                port = int(host_config.get('port', 22))

                user_from_config = host_config.get('user')
                if isinstance(user_from_config, list):
                    username = user_from_config[0]
                elif user_from_config:
                    username = user_from_config

                key_from_config = host_config.get('identityfile')
                if isinstance(key_from_config, list):
                    key_filename = os.path.expanduser(key_from_config[0])
                elif key_from_config:
                    key_filename = os.path.expanduser(key_from_config)

            print(f"Connecting to {hostname} (port {port}) as user '{username}' using SSH config...")
            ssh.connect(hostname, port=port, username=username, password=password, key_filename=key_filename)
        else:
            print(f"No SSH config file found. Connecting to {fqdn} with provided credentials...")
            ssh.connect(fqdn, port=port, username=username, password=password, key_filename=key_filename)

        os_family = get_os_family(ssh)
        is_transactional = is_transactional_system(ssh)

        _stdin, stdout, _stderr = ssh.exec_command('command -v tar')
        if not stdout.read().decode().strip():
            print("'tar' command not found. Attempting to install...")
            install_command = None

            if os_family and ('suse' in os_family or 'opensuse' in os_family) and not is_transactional:
                install_command = 'sudo zypper --non-interactive install tar'
            elif is_transactional:
                install_command = 'sudo transactional-update --continue -n pkg install tar'
            elif os_family and ('debian' in os_family or 'ubuntu' in os_family):
                install_command = 'sudo apt-get update && sudo apt-get install -y tar'
            elif os_family and ('centos' in os_family or 'rocky' in os_family or 'almalinux' in os_family or 'redhat' in os_family):
                install_command = 'sudo dnf install -y tar'
            else:
                print(f"Warning: Cannot determine package manager for OS family '{os_family}'. Skipping log collection for {fqdn}.")
                return

            if install_command:
                _stdin, _stdout, _stderr = ssh.exec_command(install_command)
                _stdin, stdout, _stderr = ssh.exec_command('command -v tar')
                if not stdout.read().decode().strip():
                    print(f"Warning: Failed to install 'tar' on {fqdn}. Skipping log collection for this host.")
                    return

        local_logs_dir = 'logs'
        if not os.path.exists(local_logs_dir):
            os.makedirs(local_logs_dir)

        sftp = ssh.open_sftp()
        try:
            _stdin, stdout, _stderr = ssh.exec_command('command -v mgradm')
            mgradm_path_check = stdout.read().decode().strip()
            if mgradm_path_check:
                print("Uyuni/MLM Server found. Running 'mgradm support config'...")

                # Execute command and wait for it to finish
                _stdin, stdout, _stderr = ssh.exec_command(f'sudo mgradm support config')
                exit_status = stdout.channel.recv_exit_status()

                if exit_status == 0:
                    # List files and search for the one with the correct pattern
                    remote_files = sftp.listdir('.')
                    supportconfig_filename = None
                    for file in remote_files:
                        if re.match(r'scc_.*\.tar\.gz', file):
                            supportconfig_filename = file
                            break

                    if supportconfig_filename:
                        try:
                            local_mgradm_path = os.path.join(local_logs_dir, f"{supportconfig_filename}")
                            print(f"Downloading mgradm support config file: {supportconfig_filename} to {local_mgradm_path}...")
                            sftp.get(supportconfig_filename, local_mgradm_path)
                            print(f"mgradm support config file successfully downloaded.")
                            ssh.exec_command(f'sudo rm "{supportconfig_filename}"')
                        except FileNotFoundError:
                            print(f"Error: supportconfig file not found after command finished. Skipping download and cleanup.")
                    else:
                        print("Warning: Could not find the supportconfig filename on the remote host. Skipping download.")
                else:
                    print(f"Warning: 'mgradm support config' command failed with exit status {exit_status}. Skipping download.")
            else:
                print("Collecting node logs...")
                _stdin, stdout, _stderr = ssh.exec_command('sudo journalctl > /var/log/messages')
                stdout.channel.recv_exit_status()
                _stdin, stdout, _stderr = ssh.exec_command('sudo venv-salt-call --local grains.items | sudo tee -a /var/log/salt_grains')
                stdout.channel.recv_exit_status()

                remote_tar_path = f"/tmp/{fqdn}-logs.tar.xz"
                _stdin, stdout, _stderr = ssh.exec_command(f'sudo rm -f "{remote_tar_path}"')
                stdout.channel.recv_exit_status()

                print(f"Creating logs archive at {remote_tar_path}...")
                tar_command = f'sudo tar cfvJP "{remote_tar_path}" --exclude="/var/log/journal" /var/log/ || [[ $? -eq 1 ]]'
                _stdin, stdout, _stderr = ssh.exec_command(tar_command)
                stdout.channel.recv_exit_status()

                local_tar_path = os.path.join(local_logs_dir, f"{fqdn}-logs.tar.xz")
                print(f"Downloading logs from {remote_tar_path} to {local_tar_path}...")
                sftp.get(remote_tar_path, local_tar_path)

                print(f"Logs archive successfully downloaded to {local_tar_path}")
                ssh.exec_command(f'sudo rm "{remote_tar_path}"')

        except Exception as e:
            print(f"An error occurred while collecting logs: {e}")

    except Exception as e:
        print(f"An error occurred with {fqdn}: {e}", file=sys.stderr)
    finally:
        if 'sftp' in locals():
            sftp.close()
        if 'ssh' in locals() and ssh.get_transport() and ssh.get_transport().is_active():
            ssh.close()
            print(f"Connection to {fqdn} closed.")

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Collect logs from remote nodes via SSH.")
    parser.add_argument("fqdns", nargs='+', help="List of Fully Qualified Domain Names of the nodes.")
    parser.add_argument("-u", "--user", default='root', help="The SSH username. Defaults to current user if SSH config is not used.")
    parser.add_argument("-p", "--password", default='linux', help="The SSH password (used if key-based auth fails).")
    parser.add_argument("-k", "--key", default=os.path.expanduser("~/.ssh/id_ed25519"), help="Path to the SSH private key (defaults to ~/.ssh/id_ed25519).")

    args = parser.parse_args()

    if not args.fqdns:
        print("Please provide at least one FQDN to collect logs from.")
        sys.exit(1)

    for fqdn in args.fqdns:
        extract_logs_from_node(fqdn, args.user, args.password, args.key)
