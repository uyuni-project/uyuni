#!/bin/bash

# This check should be performed outside container in this way:
#   EXPECTED_SKILLS="$(find .devcontainer/config/shared/skills/public -mindepth 2 -maxdepth 2 -name SKILL.md -printf '%h\n' | xargs -n1 basename | sort | paste -sd' ')"
#   docker run \
#     --rm \
#     -e EXPECTED_SKILLS="$EXPECTED_SKILLS" \
#     -v "${{ github.workspace }}/.devcontainer/capability-check.sh:/usr/local/bin/capability-check.sh:ro" \
#     "<devcontainer image>" \
#     bash /usr/local/bin/capability-check.sh

set -o pipefail

expected_mcp_claude="$(jq -r '.mcpServers // {} | keys | join(" ")' /etc/claude-code/managed-mcp.json 2>/dev/null)"
expected_plugins_claude="$(jq -r '(.enabledPlugins // .plugins // {}) | keys | join(" ")' /etc/claude-code/managed-settings.json)"
expected_mcp_codex="$(grep -oE '^\[mcp_servers\.[^]]+\]' /etc/codex/managed_config.toml | sed -E 's/^\[mcp_servers\.(.+)\]$/\1/' | paste -sd' ')"
expected_mcp_opencode="$(jq -r '.mcp // {} | keys | join(" ")' /etc/opencode/managed_config.json)"

rc=0

check() {
    agent=$1 kind=$2
    shift 2
    for name in "$@"; do
        case "$kind" in
            mcp)    "$agent" mcp list 2>&1 | grep -qi "$name" ;;
            plugin) "$agent" plugin list 2>&1 | grep -qi "$name" ;;
            skill)  [[ -f "${HOME}/.${agent}/skills/${name}/SKILL.md" ]] ;;
        esac && echo "PASS: ${agent} ${kind} ${name} available" || { echo "FAIL: ${agent} ${kind} ${name} not available"; rc=1; }
    done
}

for agent in claude codex opencode; do
    expected_mcps="expected_mcp_${agent}"
    check "$agent" mcp ${!expected_mcps}
    check "$agent" skill ${EXPECTED_SKILLS}
done
check claude plugin ${expected_plugins_claude}

exit "$rc"
