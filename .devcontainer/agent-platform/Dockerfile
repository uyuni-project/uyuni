FROM node:20

ARG TARGETARCH
ARG AGENT_PLATFORM_CLAUDE_CODE_VERSION
ARG AGENT_PLATFORM_CODEX_VERSION
ARG AGENT_PLATFORM_OPENCODE_VERSION
ARG AGENT_PLATFORM_BACKLOG_MD_VERSION
ARG AGENT_PLATFORM_BACKLOG_MD_MCP_VERSION
ARG AGENT_PLATFORM_BEADS_VERSION
ARG AGENT_PLATFORM_CONTEXT7_VERSION
ARG AGENT_PLATFORM_PLAYWRIGHT_MCP_VERSION
ARG AGENT_PLATFORM_ZSH_IN_DOCKER_VERSION
ARG AGENT_PLATFORM_TRUFFLEHOG_VERSION
ARG AGENT_PLATFORM_EDITOR
ARG AGENT_PLATFORM_LANGUAGE
ARG AGENT_PLATFORM_TZ
ARG AGENT_PLATFORM_OLLAMA_BASE_URL

ENV \
  NPM_CONFIG_PREFIX="/usr/local/share/npm-global" \
  PATH="/usr/local/share/npm-global/bin:/home/node/.local/bin:${PATH}" \
  SHELL="/bin/zsh" \
  EDITOR="${AGENT_PLATFORM_EDITOR}" \
  VISUAL="${AGENT_PLATFORM_EDITOR}" \
  POWERLEVEL9K_DISABLE_GITSTATUS="true" \
  TZ="${AGENT_PLATFORM_TZ}" \
  LANG="${AGENT_PLATFORM_LANGUAGE}" \
  LC_ALL="${AGENT_PLATFORM_LANGUAGE}" \
  OPENCODE_CONFIG="/etc/opencode/managed_config.json" \
  OLLAMA_HOST="${AGENT_PLATFORM_OLLAMA_BASE_URL}"

RUN \
  base="${AGENT_PLATFORM_LANGUAGE%.*}" && \
  short=$(echo "${base%_*}" | tr '[:upper:]' '[:lower:]') && \
  echo "LANGUAGE=${base}:${short}" >> /etc/environment && \
  echo "export LANGUAGE=${base}:${short}" > /etc/profile.d/agent-platform-language.sh

COPY --chmod=0555 "init-firewall.sh" "/usr/local/bin/init-firewall.sh"
COPY --chmod=0555 "access.sh" "/usr/local/bin/access.sh"
COPY --chmod=0444 "sudoers" "/etc/sudoers.d/sudoers"
COPY --chmod=0664 ".trufflehog-exclude" "/home/node/.trufflehog-exclude"
COPY --chown=node:node --chmod=0774 "config/claude" "/home/node/.claude"
COPY --chown=node:node --chmod=0774 "config/codex" "/home/node/.codex"
COPY --chown=node:node --chmod=0774 "config/opencode" "/home/node/.opencode"
COPY --chown=node:node --chmod=0774 "config/shared" "/usr/local/share/agent-platform/shared"

RUN \
  curl -fsSL https://packages.cloud.google.com/apt/doc/apt-key.gpg | gpg --dearmor -o /usr/share/keyrings/cloud.google.gpg && \
  echo "deb [signed-by=/usr/share/keyrings/cloud.google.gpg] https://packages.cloud.google.com/apt cloud-sdk main" > /etc/apt/sources.list.d/google-cloud-sdk.list && \
  apt-get update && \
  apt-get install --yes --no-install-recommends \
    google-cloud-cli \
    python3-pip \
    less \
    git \
    gh \
    procps \
    sudo \
    fzf \
    zsh \
    man-db \
    unzip \
    gnupg2 \
    iptables \
    ipset \
    iproute2 \
    dnsutils \
    aggregate \
    jq \
    nano \
    vim \
    moreutils \
    xvfb \
    chromium && \
  apt-get clean && \
  rm --recursive --force "/var/lib/apt/lists/*" && \
  mkdir --parents "/usr/local/share/npm-global" "/workspace" "/commandhistory" "/etc/claude-code/" "/etc/codex" "/etc/opencode" "/home/node/.claude" "/home/node/.codex" "/home/node/.opencode" && \
  touch "/commandhistory/.bash_history" && \
  chown --recursive node:node "/usr/local/share" "/workspace" "/commandhistory" "/home/node/.claude" "/home/node/.codex" "/home/node/.opencode" && \
  chown --recursive root:root "/etc/claude-code/" "/etc/codex" "/etc/opencode" && \
  mv "/home/node/.claude/managed-settings.json" "/etc/claude-code/" && \
  mv "/home/node/.claude/managed-mcp.json" "/etc/claude-code/" && \
  mv "/home/node/.codex/managed_config.toml" "/etc/codex/" && \
  mv "/home/node/.opencode/managed_config.json" "/etc/opencode/" && \
  if [ -n "${AGENT_PLATFORM_OLLAMA_BASE_URL}" ]; then sed -i "s|AGENT_PLATFORM_OLLAMA_BASE_URL_PLACEHOLDER|${AGENT_PLATFORM_OLLAMA_BASE_URL}|g" /etc/opencode/managed_config.json; fi && \
  chmod 0664 "/etc/claude-code/managed-settings.json" "/etc/claude-code/managed-mcp.json" "/etc/codex/managed_config.toml" "/etc/opencode/managed_config.json" && \
  chown root:root "/etc/claude-code/managed-settings.json" "/etc/claude-code/managed-mcp.json" "/etc/codex/managed_config.toml" "/etc/opencode/managed_config.json"

USER node

RUN \
  sh -c "$(wget -O- https://github.com/deluan/zsh-in-docker/releases/download/v${AGENT_PLATFORM_ZSH_IN_DOCKER_VERSION}/zsh-in-docker.sh)" -- \
    -p git \
    -p fzf \
    -a 'export OPENCODE_CONFIG_CONTENT="$(jq -c . /etc/opencode/managed_config.json 2>/dev/null || printf "{}")"' \
    -a 'source /usr/share/doc/fzf/examples/key-bindings.zsh' \
    -a 'source /usr/share/doc/fzf/examples/completion.zsh' \
    -a 'export HISTFILE=/commandhistory/.bash_history' \
    -a 'export HISTSIZE=-1' \
    -a 'export SAVEHIST=-1' \
    -a 'setopt APPEND_HISTORY' \
    -a 'setopt INC_APPEND_HISTORY' \
    -a 'setopt SHARE_HISTORY' \
    -a 'setopt HIST_IGNORE_ALL_DUPS' \
    -a 'setopt HIST_IGNORE_SPACE' \
    -x && \
  npm install --global "@anthropic-ai/claude-code@${AGENT_PLATFORM_CLAUDE_CODE_VERSION}" && \
  npm install --global "@openai/codex@${AGENT_PLATFORM_CODEX_VERSION}" && \
  npm install --global "opencode-ai@${AGENT_PLATFORM_OPENCODE_VERSION}" && \
  npm install --global "backlog.md@${AGENT_PLATFORM_BACKLOG_MD_VERSION}" && \
  npm install --global "@radleta/backlog-md-mcp@${AGENT_PLATFORM_BACKLOG_MD_MCP_VERSION}" && \
  npm install --global "@beads/bd@${AGENT_PLATFORM_BEADS_VERSION}" && \
  npm install --global "@upstash/context7-mcp@${AGENT_PLATFORM_CONTEXT7_VERSION}" && \
  npm install --global "@playwright/mcp@${AGENT_PLATFORM_PLAYWRIGHT_MCP_VERSION}" && \
  rm --recursive --force "/home/node/.claude/plugins" && \
  mkdir --parents "/home/node/.claude/plugins" && \
  CLAUDE_CODE_PLUGIN_CACHE_DIR="/home/node/.claude/plugins" claude plugin marketplace add anthropics/claude-plugins-official && \
  for plugin in frontend-design playwright feature-dev ralph-loop context7; do \
    CLAUDE_CODE_PLUGIN_CACHE_DIR="/home/node/.claude/plugins" claude plugin install "$plugin@claude-plugins-official"; \
  done && \
  shared="/usr/local/share/agent-platform/shared" && \
  for entry in claude:CLAUDE.md codex:AGENTS.md opencode:AGENTS.md; do \
    home="/home/node/.${entry%%:*}" && \
    rm --recursive --force "$home/skills" "$home/rules" "$home/${entry##*:}" && \
    mkdir --parents "$home/skills" "$home/rules" && \
    find "$shared/skills/public" "$shared/skills/private" -mindepth 1 -maxdepth 1 -type d -exec ln --symbolic --target-directory="$home/skills" {} + && \
    ln --symbolic "$shared/AGENTS.md"   "$home/${entry##*:}" && \
    ln --symbolic "$shared/TOOLS.md"    "$home/rules/tools.md" && \
    ln --symbolic "$shared/SECURITY.md" "$home/rules/security.md"; \
  done && \
  mkdir --parents "/home/node/.local/bin" && \
  if [ -z "${AGENT_PLATFORM_TRUFFLEHOG_VERSION}" ] || [ "${AGENT_PLATFORM_TRUFFLEHOG_VERSION}" = "latest" ]; then \
    AGENT_PLATFORM_TRUFFLEHOG_VERSION=$(curl -fsSL https://api.github.com/repos/trufflesecurity/trufflehog/releases/latest | jq -r '.tag_name' | sed 's/^v//'); \
  fi && \
  curl -fsSL "https://github.com/trufflesecurity/trufflehog/releases/download/v${AGENT_PLATFORM_TRUFFLEHOG_VERSION}/trufflehog_${AGENT_PLATFORM_TRUFFLEHOG_VERSION}_linux_${TARGETARCH}.tar.gz" | tar xz -C /tmp && \
  mv /tmp/trufflehog /home/node/.local/bin/trufflehog

USER root
RUN chown --recursive root:root "/usr/local/share/npm-global"

USER node
WORKDIR /workspace
