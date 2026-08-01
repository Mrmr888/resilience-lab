# 架构说明

## 核心调用链

一次订单演练的路径如下：

1. 浏览器向 `/api/orders/simulate` 发送合成 SKU 与数量。
2. Nginx 将 API 请求反向代理到 Spring Cloud Gateway。
3. 网关生成或透传 `X-Trace-Id`，再通过 Eureka 找到 Order Service。
4. Order Service 通过 OpenFeign 和客户端负载均衡调用 Inventory Service。
5. Resilience4j 统计下游失败率；超过阈值后短路调用并执行降级逻辑。
6. 成功、业务拒绝和下游故障都作为事件写入数据库，再由控制台展示。

## 模块职责

| 模块 | 端口 | 职责 |
| --- | ---: | --- |
| `service-registry` | 8761 | Eureka 服务注册与发现 |
| `api-gateway` | 8080 | 统一入口、路由、Trace ID、公开演练限流 |
| `order-service` | 8081 | 调用编排、熔断降级、事件统计与持久化 |
| `inventory-service` | 8082 | 合成库存、受控故障注入 |
| `frontend` | 5173 / 80 | Vue 运维控制台、Nginx 静态托管与反向代理 |

## 关键设计选择

### 为什么不是随机异常代码

故障由显式场景状态机控制，参数有硬上限。这样测试可重复，也避免公开演示时制造无限延迟或不受控资源消耗。

### 为什么业务拒绝不计为下游故障

库存不足属于正常业务结果，记录为 `ORDER_REJECTED`，但不应该推动熔断器开启。只有连接失败、503 或已短路的调用记录为 `DOWNSTREAM_FAILURE`。

### 为什么本地和容器使用不同数据库

本地默认 H2，降低首次运行成本；Docker Compose 使用 PostgreSQL，展示真实持久化与迁移路径。两者共享同一份 Flyway schema。

### 当前可观测边界

MVP 使用应用事件和 Trace ID 形成可解释的调用时间线。它不是完整 APM：跨进程 span、指标长期存储和日志聚合计划通过 OpenTelemetry、Prometheus 与 Grafana 补齐。

## 熔断参数

- 滑动窗口：8 次调用
- 最小调用数：4
- 失败率阈值：50%
- 开启后等待：8 秒
- 半开探测：2 次调用

这些参数为了在演示中快速观察状态变化，不能直接视作生产推荐值。真实系统应结合 SLO、流量分布和下游容量压测后配置。

