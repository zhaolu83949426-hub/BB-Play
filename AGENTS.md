# Agent 指南 / Agent Guide

> 本文件规则适用于本项目中的所有协作。/ These rules apply to all work in this project.

## 1. 沟通与提交流程 / Communication & Delivery

- 使用中文回复。/ Reply in Chinese.
- 阶段任务完成后不要写总结。/ Do not add a summary after finishing a stage task.
- Git Commit 说明必须使用中文。/ Git commit messages must be in Chinese.
- 未经用户明确指示，不要主动提交代码。/ Do not commit code unless the user explicitly asks.
- 最终回复不要附带后续任务建议。/ Do not propose follow-up tasks in the final reply.

## 2. 实现原则 / Implementation Rules

- 只提供一个方案，并采用最直接、最简洁的实现。/ Provide only one solution and use the most direct implementation.
- 不提供备选方案、冗余实现、通用模板或 fallback。/ Do not add alternatives, redundant implementations, generic templates, or fallbacks.
- 不做过度防御性编程，默认输入符合约束。/ Avoid over-defensive programming; assume valid input.
- 避免无助于实现的解释。/ Avoid explanations that do not directly help the implementation.
- 不要求向后兼容，也不做渐进式迁移。/ Backward compatibility and gradual migration are not required.
- 优先复用现有代码与结构，避免重复定义。/ Reuse existing code and structure whenever possible.
- 在用户明确要求开发前，不修改代码。/ Do not modify code before the user explicitly asks for development.

## 3. 技术规范 / Technical Standards

- 运行项目或测试时直接使用 `mvn` 命令，不先 `cd`。/ Use `mvn` directly to run the project or tests; do not `cd` first.
- SQL 写在 Mapper 中，使用 MyBatis Plus 写法；不要在 XML 中写 SQL，也不要使用 `@Sql`。/ Put SQL in Mapper classes with MyBatis Plus; do not write SQL in XML or use `@Sql`.
- 严格遵循现有代码风格、目录结构与分层。/ Follow the existing code style, structure, and layering.
- 逻辑删除按 MyBatis Plus 规范实现。/ Implement logical deletion with MyBatis Plus conventions.
- 新增或编辑的数据录入必须做前后端一致的长度与必填校验；前端超长时禁止继续输入，必填项需显示 `*`。/ For create and edit flows, enforce consistent frontend and backend length and required-field validation; block extra input on the frontend and mark required fields with `*`.
- 新增或修改实体类、服务方法、RPC 接口及关键业务步骤时，补充简洁准确的中文注释。/ Add concise and accurate Chinese comments for new or changed entities, service methods, RPC interfaces, and key business steps.

## 4. 注释要求 / Comment Requirements

- 实体类、DTO、VO、DO 需要类注释，说明业务用途。/ Entities, DTOs, VOs, and DOs need class comments describing business purpose.
- 方法名不足以表达业务意图时，补充方法注释说明职责、输入输出或关键约束。/ Add method comments when the name alone does not clearly express intent, inputs/outputs, or constraints.
- 对核心判断、跨服务调用、状态流转、批量处理、幂等控制等关键步骤添加必要注释。/ Add necessary comments for core decisions, cross-service calls, state transitions, batch processing, and idempotency control.
- 注释必须贴合当前实现，不能空泛，也不要逐行翻译代码。/ Comments must match the current implementation, stay concrete, and must not translate code line by line.

## 5. 编码与乱码处理 / Encoding & Mojibake

- 读取源码时始终显式使用 UTF-8。/ Always read source files explicitly as UTF-8.
- 发现疑似乱码时，先确认是终端显示问题还是文件内容损坏，再决定是否修改。/ If text looks garbled, first verify whether it is a terminal rendering issue or actual file corruption before editing.
- 修复前需提供验证证据（命令 + 结论）。/ Before fixing, provide verification evidence: command plus conclusion.
- 乱码修复必须最小化、按行处理，禁止整文件批量替换。/ Mojibake fixes must be minimal and line-scoped; bulk replacement is forbidden.
- 修复后需做 UTF-8 回归检查，并在结果中区分“真实损坏已修复”和“仅终端显示异常但文件正常”。/ After fixing, run a UTF-8 regression check and clearly separate truly corrupted files from files that only looked garbled in terminal output.

## 6. 调试优先 / Debug First

- 不为“先跑起来”而增加边界限制、降级逻辑或静默 fallback。/ Do not add guardrails, degradation, or silent fallbacks just to make things run.
- 不写 mock 成功路径，不吞错误。/ Do not add fake success paths or swallow errors.
- 让问题显式暴露，优先修复根因。/ Let failures surface clearly and fix root causes first.
- 如确需边界规则或 fallback，必须显式、可关闭、有文档，并事先获得用户同意。/ If a fallback is truly necessary, it must be explicit, documented, easy to disable, and agreed by the user beforehand.

## 7. 工程质量 / Engineering Quality

- 遵循 SOLID、DRY、关注点分离与 YAGNI。/ Follow SOLID, DRY, separation of concerns, and YAGNI.
- 命名清晰，抽象务实，仅在必要处加注释。/ Use clear names and pragmatic abstractions; comment only where necessary.
- 变更行为时，删除无用代码和过时兼容路径，除非用户明确要求保留。/ Remove dead code and obsolete compatibility paths unless the user explicitly requires them.
- 关注复杂度、IO、内存与边界条件，不隐藏失败。/ Consider complexity, IO, memory, and edge cases; do not hide failures.

## 8. 代码硬性限制 / Code Limits

- 函数不超过 50 行；超出立即拆分。/ Functions must stay within 50 lines; extract helpers when exceeded.
- 文件不超过 300 行；超出按职责拆分。/ Files must stay within 300 lines; split by responsibility when exceeded.
- 嵌套不超过 3 层；优先用提前返回降低深度。/ Nesting depth must stay within 3 levels; prefer early returns.
- 位置参数不超过 3 个；更多时改为配置对象。/ Use no more than 3 positional parameters; switch to an options object if needed.
- 单函数圈复杂度不超过 10；超出即拆分逻辑。/ Keep cyclomatic complexity within 10 per function; decompose branching when exceeded.
- 禁止魔法数字，提取为具名常量。/ Do not use magic numbers; extract named constants.

## 9. 解耦与安全 / Decoupling & Security

- 业务逻辑通过参数或接口注入依赖，不直接 `new` 具体实现。/ Inject dependencies through parameters or interfaces; do not instantiate concrete implementations inside business logic.
- 优先不可变设计，不修改函数参数或全局状态。/ Prefer immutable design; do not mutate function parameters or global state.
- 禁止在源码中硬编码密钥、凭证或敏感信息。/ Never hardcode secrets, credentials, or sensitive values in source code.
- 所有数据库访问必须参数化，禁止拼接用户输入。/ Use parameterized database access; never concatenate user input.
- 在系统边界校验并清洗外部输入。/ Validate and sanitize external input at system boundaries.
- 用户在对话中提供密钥用于排查时，不要反复提示泄漏；只有写入源码时才报警。/ Do not warn repeatedly when a user shares a key in conversation for debugging; only alert if it is written into source code.

## 10. 测试与验证 / Testing & Validation

- 保持代码可测试，并尽量用自动化方式验证。/ Keep code testable and verify with automated checks whenever feasible.
- 后端单元测试超时时间固定为 60 秒。/ Backend unit tests must use a 60-second timeout.
- 优先可复现的静态检查、格式化和定向验证。/ Prefer reproducible static checks, formatting, and targeted verification.
- TypeScript/Vue 类型检查禁止在项目根目录执行全量 `vue-tsc` 或 `tsc`；仅允许针对已修改文件执行检查。/ Never run full-project `vue-tsc` or `tsc` in the project root; only run checks against modified files.
- 推荐命令：`npx vue-tsc --noEmit --skipLibCheck <changed-files>`。/ Recommended command: `npx vue-tsc --noEmit --skipLibCheck <changed-files>`.

## 11. Skills 使用规则 / Skill Usage

- 开始任务前先扫描可用 skills；命中时读取对应 `SKILL.md` 并遵循。/ Scan available skills before starting; if one matches, read its `SKILL.md` and follow it.
- 使用 skill 时要明确告知。/ Announce which skill is being used.
- 对于 3 步及以上、且会产生文件修改的任务，默认优先使用 `taskmaster`。/ Default to `taskmaster` for tasks with 3 or more ordered steps that produce file changes.

### Taskmaster / `taskmaster`

- 适用场景：多步骤任务跟踪与自主执行。/ Use for multi-step task tracking and autonomous execution.
- `taskmaster` v5 支持 `Single`、`Epic`、`Batch`，具体形态以 `SKILL.md` 为准。/ `taskmaster` v5 supports `Single`, `Epic`, and `Batch`; follow `SKILL.md` for shape selection.
- 行级批处理优先使用 `spawn_agents_on_csv`。/ Prefer `spawn_agents_on_csv` for homogeneous row-level batch work.
- 任务跟踪 CSV 与批处理 CSV 必须分开。/ Keep task-tracking CSV separate from batch-worker CSV.
