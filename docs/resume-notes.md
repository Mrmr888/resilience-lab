# 简历与面试笔记

## 简历描述模板

> 独立设计并上线 ResilienceLab 微服务韧性演练平台，基于 Spring Cloud Gateway、Eureka、OpenFeign 与 Resilience4j 构建订单调用链，实现可控故障注入、熔断降级、Trace ID 追踪和 Vue 可视化控制台；使用 Docker Compose、PostgreSQL、Nginx 与 GitHub Actions 完成部署和持续集成。

上线并做过真实测试后，可增加一条量化描述：

> 在 `[并发数]` 并发、`[持续时间]` 压测下完成 `[请求总数]` 次请求；故障注入后熔断器于 `[实测时间]` 内开启，降级响应 P95 从 `[故障前] ms` 变化为 `[故障后] ms`，恢复后成功率回到 `[数值]%`。

方括号必须替换为真实记录，不要把示例数字写进简历。

## 上线证据清单

- GitHub 仓库有连续提交记录、CI 绿灯、Release 与 Issue/PR
- 可访问的 HTTPS 演示地址
- 首页截图和一次从故障注入到恢复的 GIF
- 压测命令、原始结果和环境规格
- 一次真实问题复盘：现象、定位证据、根因、修复和验证
- 数据库备份与恢复演练记录

## 面试追问准备

### 为什么要有网关

统一外部入口，把路由、跨域、Trace ID 和限流从业务服务中剥离。内部服务不映射公网端口，减少暴露面。

### Eureka、OpenFeign 和负载均衡如何协作

服务启动后向 Eureka 注册实例；OpenFeign 使用逻辑服务名发起调用；Spring Cloud LoadBalancer 从注册表实例列表选择目标地址。

### 熔断和超时有什么区别

超时限制单次调用等待时间；熔断根据一段时间内的失败统计，主动短路后续调用，给下游恢复空间。只配熔断不配超时，慢请求仍可能长期占用资源。

### 为什么库存不足不能触发熔断

它是可预期的业务结果，不代表下游不可用。若把业务拒绝算作技术失败，热门缺货场景可能错误触发熔断。

### 这个项目离生产还差什么

至少包括身份认证与权限、分布式限流、OpenTelemetry 全链路追踪、Prometheus/Grafana、集中日志、告警、密钥管理、数据库高可用、备份恢复验证和分阶段发布。

## 建议的首批 Issue

1. `feat: add OpenTelemetry trace propagation`
2. `feat: export Prometheus business metrics`
3. `feat: protect fault controls with JWT and RBAC`
4. `test: add k6 reproducible load test`
5. `docs: publish first deployment postmortem`

把后续迭代拆成 Issue 再通过 PR 合并，比一次提交“全部完成”更能体现真实开源协作过程。

