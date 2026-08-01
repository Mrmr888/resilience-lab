# 部署手册

本文给出单机 Docker Compose 上线方式，适合个人作品集和低流量演示环境。生产业务需要进一步评估高可用、备份恢复和安全基线。

## 1. 服务器准备

- 2 核 4 GB 内存起步
- Docker 24+ 与 Docker Compose v2
- 已解析到服务器的域名
- 只对公网开放 `80/443`，数据库和 Java 服务不映射公网端口

## 2. 配置与启动

```bash
git clone <your-repository-url> resilience-lab
cd resilience-lab
cp .env.example .env
```

在 `.env` 中设置一个随机长密码，不要提交该文件。随后执行：

```bash
docker compose up --build -d
docker compose ps
docker compose logs --tail=100 api-gateway order-service
```

## 3. HTTPS

建议在 Compose 外层使用云负载均衡、Caddy 或宿主机 Nginx 终止 TLS，再代理到 `127.0.0.1:3000`。证书应开启自动续期，HTTP 永久跳转 HTTPS。

如果使用宿主机防火墙，应确保 `3000` 仅允许本机或可信反向代理访问。

## 4. 数据与恢复

事件数据位于 `postgres-data` volume。至少定期执行逻辑备份，并实际演练一次恢复流程：

```bash
docker compose exec -T postgres pg_dump -U resilience resilience_lab > resilience_lab.sql
```

备份文件可能包含运行记录，应放在非 Web 目录并限制访问权限。

## 5. 发布检查

- `docker compose config` 无错误，镜像构建成功
- `/healthz`、首页、故障切换、订单演练均可访问
- 仓库和镜像中没有 `.env`、令牌、私钥或真实个人数据
- 云安全组未开放 PostgreSQL、Eureka、Gateway 和内部服务端口
- 设置日志轮转、磁盘告警与数据库备份
- 为 GitHub 仓库开启 Dependabot 与私密漏洞报告

## 6. 公开展示边界

项目只生成虚构 SKU 和库存，不需要收集手机号、邮箱、身份证件、定位或支付信息。不要为了“更像真实业务”加入来源不明的数据集或爬虫数据。

若网站部署在中国大陆服务器并向公众提供服务，域名实名、ICP备案、公安备案或其他手续是否适用，应按云厂商指引和你的实际服务形态确认；本项目文档不构成法律意见。上线前同时准备清晰的隐私说明，即使当前版本声明不收集个人信息。

公开演练端点已经限制写请求频率和最大故障延迟。若流量扩大，应增加管理端鉴权、Redis 分布式限流和 WAF，不应仅依赖当前单实例内存限流。

## 7. 回滚

发布前保留上一版本镜像标签和数据库备份。应用回滚与数据库回滚应分开设计；Flyway migration 一旦执行，不要通过手工删表回退。

