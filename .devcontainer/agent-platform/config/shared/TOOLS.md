# Tools Reference

Project conventions for tool usage. Each agent runtime already provides per-tool descriptions; this file adds the rules that aren't captured there.

## Plan / todo tool

TodoWrite on Claude; `update_plan` on Codex and Opencode.

Use when a task has 3+ distinct steps or the user provides multiple items. Skip for single, trivial, or conversational requests.

- Exactly one step/item `in_progress` at a time.
- Flip to `completed` immediately after finishing — don't batch.
- On Claude, provide both `content` (imperative) and `activeForm` (present continuous).

## Clarification

Ask when requirements are ambiguous, multiple valid approaches exist, or a design decision needs user input. Ask as many questions with as many options as needed; stop only when you have strong confidence in what to do.

- On Claude, never use `AskUserQuestion` for plan approval — use `ExitPlanMode` instead.

## Planning

Plan before starting non-trivial implementation: new features, multi-file changes, architectural choices, or unclear scope. Skip for small fixes or pure research.

- On Claude, use `EnterPlanMode` to enter plan mode, write the plan to the plan file, and exit only once it is unambiguous.
- On Codex and Opencode, create the first `update_plan` step before touching code; keep steps atomic and verifiable.

## Calling patterns

- Independent tool calls: emit in parallel in a single response.
- Dependent calls: wait for the first result; never guess parameters.
- No placeholders. If a value is unknown, fetch it first.
