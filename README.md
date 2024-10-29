墨云视窗 在线视频网站
---

墨云视窗是一款在线视频网站，提供视频内容的上传、观看、用户间交互，并在此基础上注重于在线教育服务，提供课程的发布与学习，相关周边商品的售卖与购买等功能。

后端系统使用Spring Cloud相关技术进行实现

## 服务配置

各服务均使用nacos作为服务注册中心和配置中心

已在各服务目录的`application.yaml`中指定了基础配置，包括端口等信息

实际开发与测试环境中，通过nacos存储并下发详细配置，包括数据库地址与账户信息等。为了区分开发和生产环境，使用环境变量或命令行参数指定`group`信息，例如：

```bash
#通过环境变量进行配置：
export SPRING_CLOUD_NACOS_SERVERADDR=114.5.1.4
export SPRING_CLOUD_NACOS_DISCOVERY_GROUP=dev
export SPRING_CLOUD_NACOS_CONFIG_GROUP=dev
java -jar service-xxx.jar
```

```bash
#通过命令行参数进行配置
java -jar service-xxx.jar \
     --spring.cloud.nacos.server-addr=114.5.1.4
     --spring.cloud.nacos.discovery.group=dev \
     --spring.cloud.nacos.config.group=dev
```

## 构建

执行maven构建

`mvn_tencent_mirror_settings.xml`中已指定腾讯云镜像，如在其他环境请参考并调整
```bash
docker run -it -v ./nekowindow-backend-v2:/nekowindow-backend-v2 maven:3.8.7-openjdk-18-slim bash
mvn -s mvn_tencent_mirror_settings.xml -f pom.xml clean package -Dmaven.test.skip=true
```

使用python脚本将各服务打包为docker镜像
```python
#打包
import os
targets=['gateway', 'service-auth', 'service-user', 'service-oss', 'service-video']

for target in targets:
    os.system("docker build --build-arg SERVICE_NAME=%s -t nekowindow-%s ." % (target,target))
```

## 部署

以下为docker-compose.yaml的参考配置：

```yaml
services:
  gateway:
    image: nekowindow-gateway
    environment:
      - SPRING_CLOUD_NACOS_SERVERADDR=10.200.0.1
      - SPRING_CLOUD_NACOS_DISCOVERY_GROUP=prod
      - SPRING_CLOUD_NACOS_CONFIG_GROUP=prod
    ports:
      - 10.200.0.10:8000:80
  service-auth:
    image: nekowindow-service-auth
    environment:
      - SPRING_CLOUD_NACOS_SERVERADDR=10.200.0.1
      - SPRING_CLOUD_NACOS_DISCOVERY_GROUP=prod
      - SPRING_CLOUD_NACOS_CONFIG_GROUP=prod
  service-user:
    image: nekowindow-service-user
    environment:
      - SPRING_CLOUD_NACOS_SERVERADDR=10.200.0.1
      - SPRING_CLOUD_NACOS_DISCOVERY_GROUP=prod
      - SPRING_CLOUD_NACOS_CONFIG_GROUP=prod
  service-video:
    image: nekowindow-service-video
    environment:
      - SPRING_CLOUD_NACOS_SERVERADDR=10.200.0.1
      - SPRING_CLOUD_NACOS_DISCOVERY_GROUP=prod
      - SPRING_CLOUD_NACOS_CONFIG_GROUP=prod
  service-oss:
    image: nekowindow-service-oss
    environment:
      - SPRING_CLOUD_NACOS_SERVERADDR=10.200.0.1
      - SPRING_CLOUD_NACOS_DISCOVERY_GROUP=prod
      - SPRING_CLOUD_NACOS_CONFIG_GROUP=prod
```