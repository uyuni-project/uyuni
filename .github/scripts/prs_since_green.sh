#!/usr/bin/env bash
# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: Apache-2.0
#
# List the PRs merged to a branch since the last green run of a workflow.
# Usage: prs_since_green.sh <workflow-file> <branch> <repo> [failing-sha [failing-run-time]]
# With failing-run-time, only green runs created before it are considered.
# Needs gh (with GH_TOKEN) and jq. Prints Markdown.
set -euo pipefail

WF=${1:?workflow file}
BRANCH=${2:?branch}
REPO=${3:?owner/repo}
HEAD_SHA=${4:-$BRANCH}
BEFORE=${5:-}

created=()
[ -z "$BEFORE" ] || created=(--created "<$BEFORE")
runs=$(gh run list -R "$REPO" -w "$WF" -b "$BRANCH" -s success -L 100 "${created[@]}" \
  --json databaseId,headSha,createdAt,url --jq '.[] | @json')

# A green scheduler run can have skipped every test (the path filter only
# looks at the last commit), so keep the first one whose tests really ran.
green=
while read -r run; do
  [ -n "$run" ] || continue
  ran=$(gh api "repos/$REPO/actions/runs/$(jq -r .databaseId <<<"$run")/jobs?per_page=100" \
    --jq '[.jobs[] | select(.name == "Acceptance / tests") | .steps[]
           | select(.name == "Run acceptance tests" and .conclusion == "success")] | length')
  if [ "$ran" -gt 0 ]; then
    green=$run
    break
  fi
done <<<"$runs"
if [ -z "$green" ]; then
  echo "No green run of \`$WF\` with executed tests found on \`$BRANCH\`."
  exit 0
fi
good_sha=$(jq -r .headSha <<<"$green")

echo "Last green run: [$(jq -r .createdAt <<<"$green")]($(jq -r .url <<<"$green")) (\`${good_sha:0:9}\`)"
echo
echo "PRs merged since then:"
echo

gh api "repos/$REPO/compare/$good_sha...$HEAD_SHA" --paginate --jq '.commits[].sha' |
  while read -r sha; do
    gh api "repos/$REPO/commits/$sha/pulls" \
      --jq '.[] | select(.merged_at != null) | "- [#\(.number)](\(.html_url)) \(.title) (@\(.user.login))"'
  done | awk '!seen[$0]++'
