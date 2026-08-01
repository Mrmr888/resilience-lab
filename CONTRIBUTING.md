# Contributing

感谢你愿意改进 ResilienceLab。小而清晰的 Issue 和 Pull Request 最容易被审阅与合并。

## 开始之前

1. 搜索现有 Issue，避免重复工作。
2. 行为变化先创建 Issue，说明场景、预期结果和影响范围。
3. 不要提交真实个人数据、凭据、`.env`、构建产物或来源不明的数据集。

## 开发流程

```bash
git checkout -b feat/short-description
mvn test
cd frontend && npm ci && npm run build
```

提交信息建议使用 `feat:`、`fix:`、`test:`、`docs:`、`refactor:` 等前缀。一个 PR 聚焦一个问题，并补充对应测试或说明为什么不需要测试。

## Pull Request 清单

- 行为变化有自动化测试
- `mvn test` 与 `npm run build` 通过
- 文档和配置示例同步更新
- 没有引入秘密、遥测或未经说明的外部网络请求
- UI 变化附桌面端与移动端截图

提交代码即表示你同意按本仓库 MIT License 发布贡献。

