# Agent orchestration

Family Moments defines four specialized agent roles — **Developer**, **QA**, **Architect**, and **Security Manager** — usable from Claude Code. Each role is defined once, canonically, in this directory; `.claude/agents/<role>.md` is a thin frontmatter wrapper that points back to the same canonical instructions so the two can't drift apart.

## File map

| Role | Canonical instructions | Claude Code subagent |
|---|---|---|
| Developer | [`developer.md`](./developer.md) | [`.claude/agents/developer.md`](../../.claude/agents/developer.md) |
| QA | [`qa.md`](./qa.md) | [`.claude/agents/qa.md`](../../.claude/agents/qa.md) |
| Architect | [`architect.md`](./architect.md) | [`.claude/agents/architect.md`](../../.claude/agents/architect.md) |
| Security Manager | [`security-manager.md`](./security-manager.md) | [`.claude/agents/security-manager.md`](../../.claude/agents/security-manager.md) |

Claude Code discovers subagents by scanning `.claude/agents/`. It doesn't support a cross-file `include`, so each wrapper's body tells the agent to read its canonical file at the start of the session — that's the "thin wrapper" part. The frontmatter (tool access, model, delegation permissions) still lives per-wrapper.

## Who delegates to whom

Architect and Security Manager are the two orchestrator roles: they're the ones expected to plan/review and then hand work off rather than doing all the typing themselves. Developer and QA are workers — they don't spawn other agents.

```
Architect ──┬──> Developer   (implement the design)
            └──> QA          (define/verify the test strategy)

Security Manager ──┬──> Developer   (remediate a finding)
                    └──> QA         (add a regression test proving the fix holds)
```

Only `architect` and `security-manager` get the `Agent` tool in their frontmatter, so only they can spawn `developer`/`qa`/each other via the `Agent` tool, `@agent-<name>` mentions, or plain natural language ("use the qa subagent to..."). `developer` and `qa` omit `Agent` entirely — they're leaves.

## Invoking a role

- Natural language: "Use the architect subagent to plan this feature module."
- Explicit: `@agent-architect ...`
- Whole session as that role: `claude --agent architect`

## Keeping the wrapper in sync

Edit `docs/agents/<role>.md` for anything about *what the role should do*. Only touch `.claude/agents/<role>.md` for tool-specific concerns: which tools/model to grant, delegation edges. The `description` frontmatter field summarizes the role for delegation matching — when you change a role's purpose enough that the one-line summary goes stale, update it there too.
