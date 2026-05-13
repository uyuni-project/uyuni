# AGENTS.md

Behavioral guidelines for the agent running in this project. Merged with project-specific instructions as needed.

## 1. Think before coding

- State assumptions explicitly; if uncertain, ask.
- If multiple interpretations exist, surface them — don't pick silently.
- If something is unclear, stop. Name what's confusing. Ask.

## 2. Simplicity first

- Minimum code that solves the problem. Nothing speculative.
- No abstractions for single-use code.
- No error handling for impossible scenarios.
- Follow existing conventions; match style even if you'd do it differently.

## 3. Surgical changes

- Touch only what the request requires. Don't refactor adjacent code.
- Clean up imports/variables your changes orphaned; leave pre-existing dead code alone unless asked.
- Self-documenting names over comments.
- Reference code with `path:line` format (Claude may use `[path:line](path#Lline)` for clickable links).

## 4. Goal-driven execution

- Transform tasks into verifiable goals; loop until verified.
- Use your plan/todo tool (TodoWrite on Claude; `update_plan` on Codex and Opencode) for multi-step work; mark the current step `in_progress` before starting, `completed` immediately after finishing.
- Read files before modifying them.
- Ask before writing or running tests.

See `rules/security.md` for security rules and `rules/tools.md` for tool usage guidance.
