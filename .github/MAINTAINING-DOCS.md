# Maintaining AI Agent Instructions

This guide explains the documentation structure for multiple AI agents (Claude, Copilot, Gemini) and how to maintain consistency across them.

## Overview

The project maintains instruction files for three AI agents:
- **CLAUDE.md** — Claude Code on claude.ai (brief, links to shared content)
- **GEMINI.md** — Google Gemini (brief, links to shared content)
- **.github/copilot-instructions.md** — GitHub Copilot CLI (comprehensive, expanded guidance)

## Documentation Strategy: Shared Core + Agent-Specific Overlays

### Shared Core: `.github/SHARED.md`

Contains all **common, non-duplicated content** that applies to all agents:
- Technology stack
- Architecture (CRUDL framework, package layout, security model)
- Build and test commands
- Testing conventions and structure
- Feature addition workflow
- Versioning strategy
- Code conventions (var, DI, mappers, validators, Lombok)

**Update frequency:** Whenever architecture, stack, or conventions change

**Edit:** If you're updating information about architecture, tests, or general development practices → edit `.github/SHARED.md`

### Agent-Specific Overlays

Each agent file contains:
1. **Header** — Reference to SHARED.md
2. **Quick start** — Agent-relevant commands
3. **Agent-specific sections** — Customized guidance, tool-specific tips
4. **Reference links** — Links back to SHARED.md sections

**CLAUDE.md:**
- Concise format suitable for Claude Code context window
- Dev seed credentials (Claude-specific)
- Common tasks and testing workflows

**GEMINI.md:**
- Project overview section
- Gemini-specific development workflow guidance
- Testing best practices reformatted for Gemini's preferences

**.github/copilot-instructions.md:**
- Comprehensive guidance for GitHub Copilot CLI
- Unique sections: Environment Setup, Security Configuration, Database Migrations, CI/CD & Deployment
- Production-oriented details (Docker, GitHub Actions, SonarQube)

## Maintenance Workflow

### When to Edit Each File

| Change Type | Where to Edit | Why |
|-------------|---------------|-----|
| Stack version changes (Spring Boot, Java, dependencies) | `.github/SHARED.md` | All agents need current stack info |
| Architecture changes (CRUDL, package layout) | `.github/SHARED.md` | Shared across all agents |
| New code conventions | `.github/SHARED.md` | Common to all developers using any agent |
| New commands or build process | `.github/SHARED.md` | All agents need accurate commands |
| Testing convention changes | `.github/SHARED.md` | Used by all agents |
| Copilot-specific CI/CD guidance | `.github/copilot-instructions.md` | GitHub Copilot CLI only |
| Claude-specific credentials or workflows | `CLAUDE.md` | Claude Code only |
| Gemini-specific tips or patterns | `GEMINI.md` | Gemini only |

### Checklist: Updating Architecture

When architecture or conventions change:

1. **Update `.github/SHARED.md`** with the change (e.g., new CRUDL pattern, different security model)
2. **Verify agent files** still make sense:
   - Check if agent-specific sections reference outdated SHARED.md sections
   - Update agent file reference links if needed
3. **Test locally:**
   ```bash
   # Verify files are valid Markdown
   cat CLAUDE.md GEMINI.md .github/copilot-instructions.md .github/SHARED.md
   ```
4. **Commit** with message: `docs: update architecture in SHARED.md`

### Checklist: Adding New Agent

If adding instructions for a new agent (e.g., `.windsurfrules` for Windsurf):

1. Create new file (e.g., `.windsurfrules`)
2. **Always start with:**
   ```markdown
   # Windsurf Instructions
   
   For comprehensive architecture and convention documentation, see [.github/SHARED.md](.github/SHARED.md).
   ```
3. Add only **Windsurf-specific sections** (e.g., keyboard shortcuts, IDE-specific features)
4. Link back to SHARED.md for common content
5. Reference other agent files if helpful

### Example: Updating Stack Version

**Scenario:** Spring Boot updated from 4.0.5 to 4.0.6

**Process:**
1. Edit `.github/SHARED.md`:
   ```markdown
   - **Spring Boot 4.0.6** ← updated from 4.0.5
   ```
2. Verify CLAUDE.md's "Quick Start" section still says "Spring Boot 4.0.5" if it copies the version (update if needed)
3. Verify GEMINI.md's "Stack" section still says "Spring Boot 4.0.5" if it copies (update if needed)
4. Note: `.github/copilot-instructions.md` references "currently `1.0.1`" for budget-buddy-contracts version — this is a separate change, not stack version

**Actual changes needed:** Just 1-3 lines across all files (very minimal!)

## File Sizes & Maintenance Load

**Current state (after reorganization):**

| File | Lines | Purpose | Maintenance |
|------|-------|---------|-------------|
| `.github/SHARED.md` | ~230 | Common content | Low (single point of edit) |
| `CLAUDE.md` | ~60 | Claude-specific | Very low (mostly links) |
| `GEMINI.md` | ~70 | Gemini-specific | Very low (mostly links) |
| `.github/copilot-instructions.md` | ~343 | Copilot-specific (CI/CD, Docker, etc.) | Low (unique content) |
| **Total** | **~703** | **All agents** | **Reduced 40-50% vs. before** |

**Before reorganization:** ~493 lines with 40-50% duplication = ~240 lines of redundant content

**After reorganization:** ~703 lines with zero duplication = saved 240 lines of maintenance effort per update

## When NOT to Use SHARED.md

Avoid SHARED.md for:
- Tool-specific configurations (e.g., "Use Copilot with --no-cache flag")
- IDE-specific tips (e.g., "IntelliJ keyboard shortcuts")
- Marketing or disclaimers specific to one agent
- Security warnings for specific tools

These belong in agent-specific files.

## Links & References

- **How to write good Markdown:** See existing files for style examples
- **Cross-referencing:** Use relative paths (e.g., `[.github/SHARED.md](.github/SHARED.md)`) so links work in GitHub UI, IDE renderers, and clone repos
- **Testing links:** Push to a branch and verify all links render correctly on GitHub

## Questions?

If unsure where to edit or how to update:
1. Check the table above ("When to Edit Each File")
2. Search for the topic in all four files
3. Ask: "Will this change affect all agents or just one?" → all = SHARED.md, one = agent file
