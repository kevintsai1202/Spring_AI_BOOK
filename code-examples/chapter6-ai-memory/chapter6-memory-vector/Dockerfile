# 使用 Java 21 作為基礎映像
FROM eclipse-temurin:21-jdk-alpine AS builder

# 設定工作目錄
WORKDIR /app

# 複製 Maven 配置文件
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# 下載依賴 (利用 Docker 緩存)
RUN ./mvnw dependency:go-offline -B

# 複製源代碼
COPY src ./src

# 構建應用
RUN ./mvnw clean package -DskipTests

# 運行階段
FROM eclipse-temurin:21-jre-alpine

# 安裝 curl 用於健康檢查
RUN apk add --no-cache curl

# 創建非 root 用戶
RUN addgroup -g 1001 -S spring && adduser -u 1001 -S spring -G spring

# 設定工作目錄
WORKDIR /app

# 從構建階段複製 JAR 文件
COPY --from=builder /app/target/*.jar app.jar

# 更改所有權
RUN chown -R spring:spring /app

# 切換到非 root 用戶
USER spring

# 暴露端口
EXPOSE 8080

# 健康檢查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# 啟動應用
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
