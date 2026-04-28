#!/bin/bash

if [[ "${AGENT_PLATFORM_ALLOW_INTERNET}" == false ]] \
   && iptables -S OUTPUT 2>/dev/null | grep -q '^-P OUTPUT DROP'; then
  echo "Firewall already in restricted mode, skipping execution."
  exit 0
fi

set -euo pipefail
IFS=$'\n\t'
DOCKER_DNS_RULES=$(iptables-save -t nat | grep "127\.0\.0\.11" || true)

get_host_network() {
    local host_ip
    host_ip=$(ip route | grep default | cut -d" " -f3)
    if [ -z "$host_ip" ]; then
        echo "ERROR: Getting host IP from default route"
        exit 1
    fi
    echo "$host_ip" | sed "s/\.[0-9]*$/.0\/24/"
}

echo "Flushing existing rules and delete existing ipsets..."
iptables -F
iptables -X
iptables -t nat -F
iptables -t nat -X
iptables -t mangle -F
iptables -t mangle -X
ipset destroy allowed-domains 2>/dev/null || true

echo "Restoring Docker DNS rules if any..."
if [ -n "$DOCKER_DNS_RULES" ]; then
    iptables -t nat -N DOCKER_OUTPUT 2>/dev/null || true
    iptables -t nat -N DOCKER_POSTROUTING 2>/dev/null || true
    echo "$DOCKER_DNS_RULES" | xargs -L 1 iptables -t nat
else
    echo "No Docker DNS rules to restore"
fi

echo "Allowing inbound traffic..."
iptables -A INPUT -p udp --sport 53 -j ACCEPT
iptables -A INPUT -i lo -j ACCEPT
iptables -A INPUT -p tcp --sport 22 -m state --state ESTABLISHED -j ACCEPT
iptables -A INPUT -s "$(get_host_network)" -j ACCEPT
iptables -A INPUT -m state --state ESTABLISHED,RELATED -j ACCEPT

echo "Setting default INPUT/FORWARD policies to DROP..."
iptables -P INPUT DROP
iptables -P FORWARD DROP

if [[ "${AGENT_PLATFORM_ALLOW_INTERNET}" == false ]]; then

    echo "Creating ipset with CIDR support to add domains..."
    ipset create allowed-domains hash:net

    echo "Fetching GitHub IP ranges..."
    gh_ranges=$(curl -s https://api.github.com/meta)
    if [ -z "$gh_ranges" ]; then
        echo "ERROR: Failed to fetch GitHub IP ranges"
        exit 1
    fi

    if ! echo "$gh_ranges" | jq -e '.web and .api and .git' >/dev/null; then
        echo "ERROR: GitHub API response missing required fields"
        exit 1
    fi

    echo "Processing GitHub IPs..."
    while read -r cidr; do
        if [[ ! "$cidr" =~ ^[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}/[0-9]{1,2}$ ]]; then
            echo "ERROR: Invalid CIDR range from GitHub meta: $cidr"
            exit 1
        fi
        echo "Adding GitHub range $cidr"
        ipset add allowed-domains "$cidr"
    done < <(echo "$gh_ranges" | jq -r '(.web + .api + .git)[]' | aggregate -q)

    echo "Fetching ChatGPT IP addresses..."
    chatgpt_ips=$(getent ahostsv4 chatgpt.com | awk '{print $1}' | sort -u)
    if [ -z "$chatgpt_ips" ]; then
        echo "ERROR: Failed to resolve chatgpt.com"
        exit 1
    fi

    echo "Processing ChatGPT IPs..."
    while read -r ip; do
        if [[ ! "$ip" =~ ^[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}$ ]]; then
            echo "ERROR: Invalid IP from ChatGPT resolution: $ip"
            exit 1
        fi
        echo "Adding ChatGPT IP $ip"
        ipset add -! allowed-domains "$ip/32"
    done < <(echo "$chatgpt_ips")

    echo "Resolving and adding other allowed domains..."
    for domain in \
        "api.openai.com" \
        "platform.openai.com" \
        "auth.openai.com" \
        "registry.npmjs.org" \
        "api.anthropic.com" \
        "sentry.io" \
        "statsig.com" \
        "marketplace.visualstudio.com" \
        "vscode.blob.core.windows.net" \
        "update.code.visualstudio.com" \
        "deb.debian.org" \
        "security.debian.org"; do
        echo "Resolving $domain..."
        ips=$(dig +noall +answer A "$domain" | awk '$4 == "A" {print $5}')
        if [ -z "$ips" ]; then
            echo "ERROR: Failed to resolve $domain"
            exit 1
        fi

        while read -r ip; do
            if [[ ! "$ip" =~ ^[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}$ ]]; then
                echo "ERROR: Invalid IP from DNS for $domain: $ip"
                exit 1
            fi
            echo "Adding $ip for $domain"
            ipset add allowed-domains "$ip"
        done < <(echo "$ips")
    done

    echo "Setting default OUTPUT policies to DROP..."
    iptables -P OUTPUT DROP

    echo "Allowing outbound DNS, localhost, and allowed domains traffic..."
    iptables -A OUTPUT -p udp --dport 53 -j ACCEPT
    iptables -A OUTPUT -o lo -j ACCEPT
    iptables -A OUTPUT -m set --match-set allowed-domains dst -j ACCEPT

    echo "Allowing established outbound connections for already approved traffic..."
    iptables -A OUTPUT -m state --state ESTABLISHED,RELATED -j ACCEPT

    if [[ "${AGENT_PLATFORM_ALLOW_HOST_NETWORK}" == true ]]; then
        echo "Allowing host network outbound access..."
        iptables -I OUTPUT -d "$(get_host_network)" -j ACCEPT
    else
        echo "Blocking host network outbound access..."
        iptables -I OUTPUT -d "$(get_host_network)" -j REJECT --reject-with icmp-admin-prohibited
    fi

    if [[ "${AGENT_PLATFORM_ALLOW_SSH_AGENT}" == true ]]; then
        echo "Allowing SSH agent outbound access..."
        iptables -A OUTPUT -p tcp --dport 22 -j ACCEPT
    else
        echo "Blocking SSH agent outbound access..."
        iptables -I OUTPUT -p tcp --dport 22 -j REJECT --reject-with icmp-admin-prohibited
    fi

    echo "Rejecting all other outbound traffic for immediate feedback..."
    iptables -A OUTPUT -j REJECT --reject-with icmp-admin-prohibited

    echo "Disabling WebFetch and WebSearch permissions in Claude settings..."
    jq '.permissions |= (. // {}) | .permissions.allow |= (. // []) - (["WebFetch","WebSearch"] - (. // []))' /etc/claude-code/managed-settings.json | sponge /etc/claude-code/managed-settings.json
    jq '.permissions |= (. // {}) | .permissions.deny |= (. // []) + (["WebFetch","WebSearch"] - (. // []))' /etc/claude-code/managed-settings.json | sponge /etc/claude-code/managed-settings.json

    echo "Disabling network and web search permissions in Codex settings..."
    sed -i 's/^network_access = true/network_access = false/' /etc/codex/managed_config.toml
    sed -i 's/^web_search = "live"/web_search = "disabled"/' /etc/codex/managed_config.toml

    echo "Disabling web permissions in Opencode settings..."
    jq '.permission |= (. // {}) | .permission.webfetch = "deny" | .permission.websearch = "deny"' /etc/opencode/managed_config.json | sponge /etc/opencode/managed_config.json
else
    echo "Setting default OUTPUT policies to ALLOW..."
    iptables -P OUTPUT ACCEPT

    if [[ "${AGENT_PLATFORM_ALLOW_HOST_NETWORK}" == false ]]; then
        iptables -A OUTPUT -d "$(get_host_network)" -j REJECT --reject-with icmp-admin-prohibited
    fi

    if [[ "${AGENT_PLATFORM_ALLOW_SSH_AGENT}" == false ]]; then
        iptables -A OUTPUT -p tcp --dport 22 -j REJECT --reject-with icmp-admin-prohibited
    fi

    echo "Enabling WebFetch and WebSearch permissions in Claude settings..."
    jq '.permissions |= (. // {}) | .permissions.allow |= (. // []) + (["WebFetch","WebSearch"] - (. // []))' /etc/claude-code/managed-settings.json | sponge /etc/claude-code/managed-settings.json
    jq '.permissions |= (. // {}) | .permissions.deny |= (. // []) - (["WebFetch","WebSearch"] - (. // []))' /etc/claude-code/managed-settings.json | sponge /etc/claude-code/managed-settings.json

    echo "Enabling network and web search permissions in Codex settings..."
    sed -i 's/^network_access = false/network_access = true/' /etc/codex/managed_config.toml
    sed -i 's/^web_search = "disabled"/web_search = "live"/' /etc/codex/managed_config.toml

    echo "Enabling web permissions in Opencode settings..."
    jq '.permission |= (. // {}) | .permission.webfetch = "allow" | .permission.websearch = "allow"' /etc/opencode/managed_config.json | sponge /etc/opencode/managed_config.json
fi
