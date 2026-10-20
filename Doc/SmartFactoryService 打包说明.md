# SmartFactoryService 打包说明

## 打包命令

在 `SmartFactoryService` 目录下执行：

```powershell
./mvnw.cmd clean package -DskipTests
```

## 命令说明

| 阶段 | 说明 |
|------|------|
| `clean` | 清理 target 目录 |
| `compile` | 编译 Java 源码（157 个源文件，Java 17） |
| `testCompile` | 编译测试源码 |
| `surefire:test` | 跳过测试（`-DskipTests`） |
| `jar:jar` | 打包为 jar |
| `spring-boot:repackage` | Spring Boot 重新打包，将依赖打入 BOOT-INF/ |

## 打包产物

| 文件 | 路径 | 说明 |
|------|------|------|
| `SmartFactoryService-0.0.1-SNAPSHOT.jar` | `target/` | 可执行 Spring Boot jar |
| `application.yml` | `target/` | 外部配置文件（需手动从 `src/main/resources/` 复制，pom.xml 已排除打入 jar） |

## 补充操作

打包后需将配置文件复制到 target 目录：

```powershell
Copy-Item "src\main\resources\application.yml" "target\application.yml"
```

## 运行

```powershell
java -jar target\SmartFactoryService-0.0.1-SNAPSHOT.jar
```
