#!/bin/bash

failed=0
pass() { echo "PASS: $*"; }
fail() { echo "FAIL: $*"; failed=1; }

check_fs() {
    trufflehog filesystem /home/node \
        --no-update \
        --exclude-paths /home/node/.trufflehog-exclude \
        --fail >/dev/null 2>&1
    case $? in
        0) pass "No secrets in filesystem" ;;
        183) fail "Secrets found in filesystem" ;;
        *) fail "Filesystem scan error" ;;
    esac
}

check_env() {
    printenv | trufflehog stdin --no-update --fail >/dev/null 2>&1
    case $? in
        0) pass "No secrets in environment" ;;
        183) fail "Secrets found in environment" ;;
        *) fail "Environment scan error" ;;
    esac
}

check_caps() {
    if [ -f /proc/1/status ]; then
        local cap
        cap=$(grep CapEff /proc/1/status | awk '{print $2}')
        [[ "$cap" =~ ^(0000003fffffffff|000001ffffffffff)$ ]] && fail "Privileged container" || pass "Not privileged"
    else
        fail "Cannot read /proc/1/status"
    fi
}

check_sudo_node() {
    if [ "$(whoami)" != "node" ]; then
        fail "Not running as user node, cannot check sudo restrictions"
        return
    fi

    local sudo_cmds cmd_count
    sudo_cmds=$(sudo -l -U node 2>/dev/null | grep -E '^\s+\(root\)' | awk '{print $NF}' | sort -u)
    cmd_count=$(echo "$sudo_cmds" | grep -v '^$' | wc -l)

    if [ "$cmd_count" -eq 1 ] && echo "$sudo_cmds" | grep -q '^/usr/local/bin/init-firewall.sh$'; then
        pass "User node can only sudo /usr/local/bin/init-firewall.sh"
    elif [ "$cmd_count" -eq 0 ]; then
        fail "User node has no sudo privileges"
    else
        fail "User node can sudo other commands: $sudo_cmds"
    fi
}

# For any mount done through standard mechanisms, the current checks will detect them.
# However, admin can intentionally mount to a non-standard path and not set the corresponding env var to evade detection and increase security risk.
check_socket() {
    [ -e "/var/run/docker.sock" ] && fail "Docker socket mounted at /var/run/docker.sock" || pass "Docker socket not mounted"
    [ -S "${SSH_AUTH_SOCK:-}" ] && [ "$SSH_AUTH_SOCK" != "$(cat /tmp/.vscode-ssh-auth-sock)" ] && fail "SSH socket at $SSH_AUTH_SOCK"
    [ -e "${GPG_AGENT_INFO%%:*}" ] && fail "GPG socket mounted at ${GPG_AGENT_INFO%%:*}" || pass "GPG socket not mounted"
    [ -S "/run/user/$(id -u)/gnupg/S.gpg-agent" ] && fail "GPG agent socket mounted" || pass "GPG agent socket not mounted"
}

check_metadata() {
    timeout 2 curl -s --max-time 1 http://169.254.169.254 &>/dev/null && fail "Metadata accessible" || pass "Metadata blocked"
}

check_non_root() {
    [ "$(id -u)" -ne 0 ] && pass "Non-root user" || fail "Running as root"
}

check_sudo_setenv() {
    echo "${SUDO_LIST:-}" | grep -q SETENV && fail "SETENV enabled" || pass "SETENV disabled"
}

check_paths() {
    local list_of_paths=(
        "/usr/local/bin/init-firewall.sh"
        "/usr/local/bin/access.sh"
        "/etc/claude-code/managed-settings.json"
        "/etc/codex/managed_config.toml"
        "/etc/opencode/managed_config.json"
        "/etc/sudoers.d/sudoers"
        "/usr/local/share/npm-global"
    )

    for path in "${list_of_paths[@]}"; do
        [ ! -e "$path" ] && return

        local owner perms parent
        owner=$(stat -c '%U:%G' "$path")
        perms=$(stat -c '%a' "$path")

        if [ "$owner" = "root:root" ] && [ -r "$path" ] && [ ! -w "$path" ]; then
            pass "$path: root:root owned, readable, not writable by others"
            if [ ! -d "$path" ]; then
                parent=$(dirname "$path")
                if [ "$(stat -c '%U:%G' "$parent")" = "root:root" ] && [ -r "$parent" ] && [ ! -w "$parent" ]; then
                    pass "$parent: root:root owned, cannot be removed"
                elif findmnt -T "$path" >/dev/null 2>&1; then
                    pass "$path: file mounted, cannot be removed"
                else
                    fail "$path: can be removed"
                fi
            fi
        else
            fail "$path: owner=$owner, perms=$perms"
        fi
    done
}

check_internet_block() {
    if [[ "${AGENT_PLATFORM_ALLOW_INTERNET}" == false ]]; then
        if curl --connect-timeout 5 https://example.com >/dev/null 2>&1; then
            fail "Was able to reach https://example.com"
        else
            pass "Unable to reach https://example.com as expected"
        fi
    fi
}

check_host_block() {
    if [[ "${AGENT_PLATFORM_ALLOW_HOST_NETWORK}" == false ]]; then
        local gw
        gw=$(ip route | grep default | awk '{print $3}')
        timeout 2 ping -c1 "$gw" >/dev/null 2>&1 && fail "Host reachable: $gw" || pass "Host blocked"
    fi
}

check_ssh_block() {
    if [[ "${AGENT_PLATFORM_ALLOW_SSH_AGENT}" == false ]]; then
        timeout 2 bash -c "exec 3<>/dev/tcp/ftp.gnu.org/22" >/dev/null 2>&1 && fail "SSH reachable" || pass "SSH blocked"
    fi
}

check_fs
check_env
check_non_root
check_sudo_setenv
check_caps
check_paths
check_socket
check_sudo_node
check_metadata
check_internet_block
check_host_block
check_ssh_block

exit "$failed"
