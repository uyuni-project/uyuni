<!--
SPDX-FileCopyrightText: 2026 SUSE LLC

SPDX-License-Identifier: Apache-2.0
-->

# Claude Code Instructions

@AGENTS.md

`AGENTS.md` is the canonical repository instruction file. Follow it for all
work in this checkout, together with any more specific `AGENTS.md` in the
subtree being changed. Update the canonical file rather than duplicating
guidance here.

- Subagent dispatch: when launching an Agent for pure lookup/exploration
  (finding files, grepping symbols, "where is X" style search — not
  implementation, review, or judgment calls), pass `model: "haiku"`.
  Reserve sonnet/opus for subagents that write code, review, or reason.
- Use `model: "sonnet"` for subagents that run builds/tests/linters and
  report results, apply already-agreed or fully specified changes
  (e.g. a reviewer's exact suggestion), or perform mechanical
  verification (diff comparisons, CI status). Keep opus for design
  decisions, schema/migration changes, evaluating review suggestions,
  git history planning, and text for reviewers or handover documents.
