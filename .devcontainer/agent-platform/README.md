# Agent Platform

Agent Platform is a devcontainer-based runtime for safely configuring and running AI agents (Claude Code, OpenAI Codex, and Opencode) in an isolated environment.

This devcontainer has been derived from: https://github.com/szachovy/agent-platform, run this script to update this devcontainer to the latest version:

<details>

```bash
SOURCE_REPO="https://github.com/szachovy/agent-platform.git"
SOURCE_BRANCH="${SOURCE_BRANCH:-master}"
TARGET_BRANCH="${TARGET_BRANCH:-update-devcontainer-from-agent-platform}"

tmpdir="$(mktemp -d)"
trap 'rm -rf "$tmpdir"' EXIT

git checkout -b "$TARGET_BRANCH" 2>/dev/null || git checkout "$TARGET_BRANCH"

git clone --depth=1 --branch "$SOURCE_BRANCH" "$SOURCE_REPO" "$tmpdir/agent-platform"

rsync -a --delete \
  --exclude 'README.md' \
  "$tmpdir/agent-platform/.devcontainer/" \
  "$PWD/.devcontainer/agent-platform"

git add .devcontainer

if git diff --cached --quiet; then
  echo "No changes."
  exit 0
fi

git commit -m "Update devcontainer from agent-platform"
git push -u origin "$TARGET_BRANCH"

if command -v gh >/dev/null 2>&1 && gh auth status >/dev/null 2>&1; then
  gh pr create \
    --base master \
    --head "$TARGET_BRANCH" \
    --title "Update devcontainer from agent-platform" \
    --body "Updates .devcontainer from ${SOURCE_REPO} branch ${SOURCE_BRANCH}."
else
  echo "GitHub CLI not available or not authenticated; skipping PR creation."
  exit 0
fi

```

</details>

## Prerequisites

- Docker or a compatible container runtime.
- A devcontainer-capable environment (VS Code Dev Containers, `devcontainer` CLI, or compatible tooling).
- `git` and `gh` (GitHub CLI) for running the update script above.

## Deploying

[Export configuration options _(optionally)_](#configuration), then create and start the devcontainer environment.

```bash
export <configuration_option>=<value>
devcontainer up \
  --workspace-folder <workspace-path> \
  --config <path-to-devcontainer.json-file> \
  --build-no-cache
```

## How to use it

Please take a look at the internal Confluence page under Training / How-to Articles.

### Configuration

In addition to the upstream configuration options documented at [agent-platform configuration reference](https://github.com/szachovy/agent-platform/blob/master/README.md#configuration), the following variables are available:

| Variable | Default Value | Description | Required | Type |
| --- | --- | --- | --- | --- |
| `AGENT_PLATFORM_EDITOR` | `nano` | Default editor inside the devcontainer (sets `EDITOR` and `VISUAL`). | No | String (e.g. `nano`, `vim`, `vi`) |
| `AGENT_PLATFORM_LANGUAGE` | `en_US.UTF-8` | Locale for the devcontainer (sets `LANG` and `LC_ALL`). | No | String (locale name, e.g. `en_US.UTF-8` or `es_ES.UTF-8`) |
| `AGENT_PLATFORM_OLLAMA_BASE_URL` | | Ollama API base URL for OpenCode provider and `OLLAMA_HOST`. Please find this in the internal AI Coding Assistance Policy – Permitted Tools and Configuration Requirements document. | No | String (URL, e.g. `http://host:port/v1`) |

Sanity checks fail in case of security violations, but you can still use container by reloading the window.

## Plugins & MCP & Skills installed

From any agent invoke `/instructions` to get the information on what and how to use installed extensions. For manual lookup see [SKILL.md](config/shared/skills/public/instructions/SKILL.md).

## References & Documentation

- [Agent-platform main repository](https://github.com/szachovy/agent-platform)
- [Anthropic Claude-Code devcontainer main](https://github.com/anthropics/claude-code/tree/main/.devcontainer)
- [Claude Code: Best practices for agentic coding](https://www.anthropic.com/engineering/claude-code-best-practices)
- [Anthropic Claude-Code developer's guide](https://platform.claude.com/docs/en/home)
