# 腾讯云 2 GB 试用机部署

本配置面向单人、低流量的简历演示环境，不等同于高可用生产部署。服务器建议使用 Ubuntu 24.04 与 Docker CE，只向公网开放 SSH、HTTP 和后续需要的 HTTPS 端口。

## 资源策略

- 宿主机创建 2 GB Swap，`vm.swappiness` 设置为 `10`。
- Java 服务使用 Serial GC、固定 Metaspace 上限和容器内存限制。
- PostgreSQL 限制共享缓冲区、连接数和工作内存。
- 容器日志最多保留 3 个 10 MB 文件。
- 服务器只拉取 GitHub Container Registry 镜像，不在 2 GB 主机上执行 Maven 或 npm 构建。

## 发布镜像

在 GitHub Actions 中手动运行 `Publish container images`。首次发布后，在个人主页的 Packages 中打开 `resilience-lab`，进入 Package settings，将可见性改为 Public。公开 GHCR 包允许服务器匿名拉取镜像。

## 服务器启动

```bash
git clone https://github.com/Mrmr888/resilience-lab.git
cd resilience-lab
cp .env.production.example .env
```

编辑 `.env`，生成独立的 PostgreSQL 长密码，并把 `PUBLIC_ORIGIN` 改为服务器公网访问地址。随后验证并启动：

```bash
docker compose --env-file .env -f docker-compose.low-memory.yml config --quiet
docker compose --env-file .env -f docker-compose.low-memory.yml pull
docker compose --env-file .env -f docker-compose.low-memory.yml up -d
docker compose --env-file .env -f docker-compose.low-memory.yml ps
```

等待服务注册后访问 `/healthz` 和首页，再执行正常订单、错误场景与恢复场景验证。

## 更新与回滚

每次正式发布都会同时生成 `latest` 和 `sha-<commit>` 标签。更新前备份数据库，再拉取并重建容器。需要回滚时，将 `.env` 中的 `IMAGE_VERSION` 改为目标 `sha-<commit>`，重新执行 `pull` 与 `up -d`。

不要执行 `docker compose down -v`，除非确认不再需要 PostgreSQL 数据。
