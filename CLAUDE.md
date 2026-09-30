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
