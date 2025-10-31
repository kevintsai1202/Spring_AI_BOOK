# 使用 Spring AI 打造企業 RAG 知識庫【33】- 企業級部署和配置優化

## 準備上線的最後一哩路

![Enterprise Deployment](https://example.com/enterprise-deployment.jpg)

經過前面的詳細介紹，我們已經了解了 Spring AI 的各種功能和最佳實踐。今天來談談如何將整個 RAG 系統部署到生產環境，包含容器化、監控、安全配置、效能調優等企業級考量。

## 容器化部署

### 1. 多階段 Docker 建置

使用最新的 Spring AI 1.1-SNAPSHOT，建立高效的 Docker 映像：

`Dockerfile`

```dockerfile
# 多階段建置 - Build stage
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app
COPY pom.xml .
COPY src ./src

# 建置應用程式
RUN mvn clean package -DskipTests

# Runtime stage  
FROM eclipse-temurin:21-jre

# 建立應用程式使用者
RUN addgroup --system spring && adduser --system spring --ingroup spring

# 安裝必要工具
RUN apt-get update && apt-get install -y \
    curl \
    jq \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# 複製 JAR 檔案
COPY --from=builder /app/target/*.jar app.jar

# 建立必要目錄
RUN mkdir -p /app/logs /app/data && \
    chown -R spring:spring /app

# 切換到非 root 使用者
USER spring:spring

# 健康檢查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# JVM 優化參數
ENV JAVA_OPTS="-Xms512m -Xmx2g -XX:+UseG1GC -XX:+UseStringDeduplication -XX:+OptimizeStringConcat"

# 啟動應用程式
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar"]

EXPOSE 8080
```

### 2. Docker Compose 配置

`docker-compose.yml`

```yaml
version: '3.8'

services:
  spring-ai-app:
    build: .
    container_name: spring-ai-rag
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=production
      - OPENAI_API_KEY=${OPENAI_API_KEY}
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/springai
      - SPRING_DATASOURCE_USERNAME=springai
      - SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}
      - SPRING_AI_VECTORSTORE_NEO4J_URI=bolt://neo4j:7687
      - SPRING_AI_VECTORSTORE_NEO4J_USERNAME=neo4j
      - SPRING_AI_VECTORSTORE_NEO4J_PASSWORD=${NEO4J_PASSWORD}
      - REDIS_HOST=redis
      - REDIS_PORT=6379
    depends_on:
      postgres:
        condition: service_healthy
      neo4j:
        condition: service_healthy
      redis:
        condition: service_healthy
    volumes:
      - app-logs:/app/logs
      - app-data:/app/data
    networks:
      - spring-ai-network
    restart: unless-stopped
    deploy:
      resources:
        limits:
          memory: 3G
          cpus: "2"
        reservations:
          memory: 1G
          cpus: "1"

  postgres:
    image: postgres:16-alpine
    container_name: spring-ai-postgres
    environment:
      - POSTGRES_DB=springai
      - POSTGRES_USER=springai
      - POSTGRES_PASSWORD=${DB_PASSWORD}
    volumes:
      - postgres-data:/var/lib/postgresql/data
      - ./init-scripts:/docker-entrypoint-initdb.d
    ports:
      - "5432:5432"
    networks:
      - spring-ai-network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U springai"]
      interval: 10s
      timeout: 5s
      retries: 5
    restart: unless-stopped

  neo4j:
    image: neo4j:5.15
    container_name: spring-ai-neo4j
    environment:
      - NEO4J_AUTH=neo4j/${NEO4J_PASSWORD}
      - NEO4J_PLUGINS=["apoc"]
      - NEO4J_dbms_security_procedures_unrestricted=apoc.*
      - NEO4J_dbms_memory_heap_initial__size=512m
      - NEO4J_dbms_memory_heap_max__size=1G
    volumes:
      - neo4j-data:/data
      - neo4j-logs:/logs
    ports:
      - "7474:7474"
      - "7687:7687"
    networks:
      - spring-ai-network
    healthcheck:
      test: ["CMD", "cypher-shell", "-u", "neo4j", "-p", "${NEO4J_PASSWORD}", "RETURN 1"]
      interval: 10s
      timeout: 5s
      retries: 5
    restart: unless-stopped

  redis:
    image: redis:7-alpine
    container_name: spring-ai-redis
    command: redis-server --appendonly yes --requirepass ${REDIS_PASSWORD}
    volumes:
      - redis-data:/data
    ports:
      - "6379:6379"
    networks:
      - spring-ai-network
    healthcheck:
      test: ["CMD", "redis-cli", "-a", "${REDIS_PASSWORD}", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5
    restart: unless-stopped

  prometheus:
    image: prom/prometheus:latest
    container_name: spring-ai-prometheus
    ports:
      - "9090:9090"
    volumes:
      - ./monitoring/prometheus.yml:/etc/prometheus/prometheus.yml
      - prometheus-data:/prometheus
    networks:
      - spring-ai-network
    restart: unless-stopped

  grafana:
    image: grafana/grafana:latest
    container_name: spring-ai-grafana
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=${GRAFANA_PASSWORD}
    volumes:
      - grafana-data:/var/lib/grafana
      - ./monitoring/grafana/dashboards:/etc/grafana/provisioning/dashboards
      - ./monitoring/grafana/datasources:/etc/grafana/provisioning/datasources
    networks:
      - spring-ai-network
    restart: unless-stopped

  nginx:
    image: nginx:alpine
    container_name: spring-ai-nginx
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx/nginx.conf:/etc/nginx/nginx.conf
      - ./nginx/ssl:/etc/nginx/ssl
    depends_on:
      - spring-ai-app
    networks:
      - spring-ai-network
    restart: unless-stopped

volumes:
  postgres-data:
  neo4j-data:
  neo4j-logs:
  redis-data:
  app-logs:
  app-data:
  prometheus-data:
  grafana-data:

networks:
  spring-ai-network:
    driver: bridge
```

## 生產環境配置

### 1. 核心配置檔案

`application-production.yml`

```yaml
server:
  port: 8080
  servlet:
    encoding:
      charset: UTF-8
      enabled: true
      force: true
  compression:
    enabled: true
    mime-types: application/json,application/xml,text/html,text/xml,text/plain,text/css,text/javascript,application/javascript
  http2:
    enabled: true

spring:
  profiles:
    active: production
    
  application:
    name: spring-ai-rag-enterprise
    
  # 資料庫配置
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 20000
      idle-timeout: 300000
      max-lifetime: 1200000
      leak-detection-threshold: 60000
      
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: false
        use_sql_comments: false
        jdbc:
          batch_size: 25
          order_inserts: true
          order_updates: true
        cache:
          use_second_level_cache: true
          use_query_cache: true
          region:
            factory_class: org.hibernate.cache.jcache.JCacheRegionFactory

  # Spring AI 配置
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      base-url: https://api.openai.com
      chat:
        options:
          model: gpt-4o-mini
          temperature: 0.7
          max-tokens: 2000
          frequency-penalty: 0.0
          presence-penalty: 0.0
        enabled: true
      embedding:
        options:
          model: text-embedding-3-small
          dimensions: 1536
        enabled: true
    retry:
      max-attempts: 3
      backoff:
        delay: 1000ms
        multiplier: 2.0
        max-delay: 10000ms
    
    vectorstore:
      neo4j:
        uri: ${SPRING_AI_VECTORSTORE_NEO4J_URI}
        username: ${SPRING_AI_VECTORSTORE_NEO4J_USERNAME}
        password: ${SPRING_AI_VECTORSTORE_NEO4J_PASSWORD}
        database-name: neo4j
        embedding-dimension: 1536
        connection-pool-size: 10
        connection-timeout: 30s

  # Redis 配置
  data:
    redis:
      host: ${REDIS_HOST}
      port: ${REDIS_PORT}
      password: ${REDIS_PASSWORD}
      timeout: 2000ms
      lettuce:
        pool:
          max-active: 20
          max-idle: 10
          min-idle: 5
          max-wait: 2000ms

  # 快取配置
  cache:
    type: redis
    redis:
      time-to-live: 3600000 # 1小時
      cache-null-values: false
      
  # Jackson 配置
  jackson:
    serialization:
      write-dates-as-timestamps: false
    deserialization:
      fail-on-unknown-properties: false
    default-property-inclusion: non_null

# 監控配置
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,env,configprops
      base-path: /actuator
  endpoint:
    health:
      show-details: when_authorized
      probes:
        enabled: true
    metrics:
      enabled: true
    prometheus:
      enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
    tags:
      application: ${spring.application.name}
      environment: production
  health:
    defaults:
      enabled: true
    diskspace:
      enabled: true
      threshold: 1GB

# 日誌配置
logging:
  level:
    root: INFO
    org.springframework.ai: INFO
    org.springframework.security: WARN
    org.hibernate.SQL: WARN
    org.hibernate.type.descriptor.sql.BasicBinder: WARN
  pattern:
    console: "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{50} - %msg%n"
  file:
    name: /app/logs/spring-ai-rag.log
  logback:
    rollingpolicy:
      max-file-size: 100MB
      max-history: 30
      total-size-cap: 3GB

# 應用程式特定配置
app:
  security:
    jwt:
      secret: ${JWT_SECRET}
      expiration: 86400000 # 24小時
    cors:
      allowed-origins: ${ALLOWED_ORIGINS:https://yourdomain.com}
      allowed-methods: GET,POST,PUT,DELETE,OPTIONS
      allowed-headers: "*"
      allow-credentials: true
  
  vectorstore:
    batch-size: 100
    max-content-length: 8000
    similarity-threshold: 0.7
    
  embedding:
    cache:
      enabled: true
      ttl: 3600 # 1小時
    retry:
      max-attempts: 3
      delay: 1000ms
      
  function-calling:
    timeout: 30s
    max-concurrent: 10
```

### 2. 安全配置

`SecurityConfig.java`

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(jwtAccessDeniedHandler))
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/actuator/**").hasRole("ADMIN")
                .requestMatchers("/ai/public/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/ai/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/ai/**").hasAnyRole("USER", "ADMIN")
                .anyRequest().authenticated())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .headers(headers -> headers
                .frameOptions().deny()
                .contentTypeOptions().and()
                .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                    .maxAgeInSeconds(31536000)
                    .includeSubdomains(true)))
            .build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("https://*.yourdomain.com"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### 3. JWT 認證實現

`JwtAuthenticationFilter.java`

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String token = getTokenFromRequest(request);
        
        if (token != null && tokenProvider.validateToken(token)) {
            String username = tokenProvider.getUsernameFromToken(token);
            
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    
    @Value("${app.security.jwt.secret}")
    private String jwtSecret;
    
    @Value("${app.security.jwt.expiration}")
    private int jwtExpirationInMs;
    
    public String generateToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Date expiryDate = new Date(System.currentTimeMillis() + jwtExpirationInMs);
        
        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }
    
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }
    
    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException | MalformedJwtException | ExpiredJwtException | 
                 UnsupportedJwtException | IllegalArgumentException ex) {
            log.error("Invalid JWT token: {}", ex.getMessage());
        }
        return false;
    }
}
```

## 監控和觀察性

### 1. 自定義指標

`AIMetricsConfiguration.java`

```java
@Configuration
@RequiredArgsConstructor
public class AIMetricsConfiguration {
    
    private final MeterRegistry meterRegistry;
    
    @Bean
    public AIMetricsCollector aiMetricsCollector() {
        return new AIMetricsCollector(meterRegistry);
    }
}

@Component
@RequiredArgsConstructor
public class AIMetricsCollector {
    
    private final MeterRegistry meterRegistry;
    private final Counter aiRequestCounter;
    private final Timer aiRequestTimer;
    private final Gauge activeConnectionsGauge;
    
    @PostConstruct
    public void initializeMetrics() {
        this.aiRequestCounter = Counter.builder("ai.requests.total")
            .description("Total number of AI requests")
            .tag("status", "unknown")
            .register(meterRegistry);
            
        this.aiRequestTimer = Timer.builder("ai.request.duration")
            .description("AI request duration")
            .register(meterRegistry);
            
        this.activeConnectionsGauge = Gauge.builder("ai.connections.active")
            .description("Active AI connections")
            .register(meterRegistry, this, AIMetricsCollector::getActiveConnections);
    }
    
    public void recordAIRequest(String operation, String status, Duration duration) {
        Counter.builder("ai.requests.total")
            .tag("operation", operation)
            .tag("status", status)
            .register(meterRegistry)
            .increment();
            
        Timer.builder("ai.request.duration")
            .tag("operation", operation)
            .register(meterRegistry)
            .record(duration);
    }
    
    private double getActiveConnections() {
        // 實現獲取活躍連接數的邏輯
        return 0.0;
    }
}
```

### 2. 健康檢查增強

`CustomHealthIndicators.java`

```java
@Component
@RequiredArgsConstructor
public class AIServiceHealthIndicator implements HealthIndicator {
    
    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    
    @Override
    public Health health() {
        try {
            // 測試 ChatClient
            String testResponse = chatClient.prompt()
                .user("test")
                .call()
                .content();
                
            if (testResponse == null || testResponse.isEmpty()) {
                return Health.down()
                    .withDetail("chat-client", "No response")
                    .build();
            }
            
            // 測試 VectorStore
            List<Document> testResults = vectorStore.similaritySearch(
                SearchRequest.query("test").withTopK(1)
            );
            
            return Health.up()
                .withDetail("chat-client", "OK")
                .withDetail("vector-store", "OK")
                .withDetail("vector-documents", testResults.size())
                .build();
                
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .withException(e)
                .build();
        }
    }
}

@Component
public class DatabaseHealthIndicator implements HealthIndicator {
    
    @Autowired
    private DataSource dataSource;
    
    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(1)) {
                return Health.up()
                    .withDetail("database", "PostgreSQL")
                    .withDetail("validationQuery", "SELECT 1")
                    .build();
            }
        } catch (Exception e) {
            return Health.down()
                .withDetail("database", "PostgreSQL")
                .withDetail("error", e.getMessage())
                .build();
        }
        
        return Health.down()
            .withDetail("database", "Connection invalid")
            .build();
    }
}
```

### 3. Prometheus 配置

`monitoring/prometheus.yml`

```yaml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

rule_files:
  - "alert_rules.yml"

scrape_configs:
  - job_name: 'spring-ai-app'
    static_configs:
      - targets: ['spring-ai-app:8080']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 15s
    
  - job_name: 'postgres'
    static_configs:
      - targets: ['postgres:5432']
    
  - job_name: 'neo4j'
    static_configs:
      - targets: ['neo4j:2004']
    metrics_path: '/metrics'
    
  - job_name: 'redis'
    static_configs:
      - targets: ['redis:6379']

alerting:
  alertmanagers:
    - static_configs:
        - targets:
          - alertmanager:9093
```

## 效能調優

### 1. JVM 調優

`startup.sh`

```bash
#!/bin/bash

# JVM 記憶體設定
export JAVA_OPTS="$JAVA_OPTS -Xms1g -Xmx3g"

# 垃圾回收器設定
export JAVA_OPTS="$JAVA_OPTS -XX:+UseG1GC"
export JAVA_OPTS="$JAVA_OPTS -XX:MaxGCPauseMillis=200"
export JAVA_OPTS="$JAVA_OPTS -XX:G1HeapRegionSize=16m"

# 效能優化
export JAVA_OPTS="$JAVA_OPTS -XX:+UseStringDeduplication"
export JAVA_OPTS="$JAVA_OPTS -XX:+OptimizeStringConcat"
export JAVA_OPTS="$JAVA_OPTS -XX:+UseFastAccessorMethods"

# 除錯和監控
export JAVA_OPTS="$JAVA_OPTS -XX:+HeapDumpOnOutOfMemoryError"
export JAVA_OPTS="$JAVA_OPTS -XX:HeapDumpPath=/app/logs/"
export JAVA_OPTS="$JAVA_OPTS -XX:+PrintGCDetails"
export JAVA_OPTS="$JAVA_OPTS -XX:+PrintGCTimeStamps"
export JAVA_OPTS="$JAVA_OPTS -Xloggc:/app/logs/gc.log"

# 安全設定
export JAVA_OPTS="$JAVA_OPTS -Djava.security.egd=file:/dev/./urandom"

# 啟動應用程式
java $JAVA_OPTS -jar app.jar
```

### 2. 連接池調優

`DatabaseConfig.java`

```java
@Configuration
@RequiredArgsConstructor
public class DatabaseConfig {
    
    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.hikari")
    public HikariConfig hikariConfig() {
        HikariConfig config = new HikariConfig();
        
        // 連接池設定
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(20000);
        config.setIdleTimeout(300000);
        config.setMaxLifetime(1200000);
        config.setLeakDetectionThreshold(60000);
        
        // 效能設定
        config.setConnectionTestQuery("SELECT 1");
        config.setValidationTimeout(5000);
        config.setInitializationFailTimeout(1);
        
        // 快取設定
        config.setCachePrepStmts(true);
        config.setPrepStmtCacheSize(250);
        config.setPrepStmtCacheSqlLimit(2048);
        config.setUseServerPrepStmts(true);
        config.setUseLocalSessionState(true);
        config.setRewriteBatchedStatements(true);
        config.setCacheResultSetMetadata(true);
        config.setCacheServerConfiguration(true);
        config.setElideSetAutoCommits(true);
        config.setMaintainTimeStats(false);
        
        return config;
    }
    
    @Bean
    @Primary
    public DataSource dataSource() {
        return new HikariDataSource(hikariConfig());
    }
}
```

## Kubernetes 部署

### 1. Deployment 配置

`k8s/deployment.yaml`

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: spring-ai-rag
  namespace: production
  labels:
    app: spring-ai-rag
    version: v1
spec:
  replicas: 3
  strategy:
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0
    type: RollingUpdate
  selector:
    matchLabels:
      app: spring-ai-rag
  template:
    metadata:
      labels:
        app: spring-ai-rag
        version: v1
    spec:
      serviceAccountName: spring-ai-rag
      containers:
      - name: spring-ai-rag
        image: spring-ai-rag:latest
        imagePullPolicy: IfNotPresent
        ports:
        - containerPort: 8080
          name: http
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "production"
        - name: OPENAI_API_KEY
          valueFrom:
            secretKeyRef:
              name: spring-ai-secrets
              key: openai-api-key
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: spring-ai-secrets
              key: db-password
        resources:
          requests:
            memory: "1Gi"
            cpu: "500m"
          limits:
            memory: "3Gi"
            cpu: "2"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
          timeoutSeconds: 5
          failureThreshold: 3
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 3
        volumeMounts:
        - name: app-logs
          mountPath: /app/logs
        - name: app-config
          mountPath: /app/config
      volumes:
      - name: app-logs
        emptyDir: {}
      - name: app-config
        configMap:
          name: spring-ai-config
      affinity:
        podAntiAffinity:
          preferredDuringSchedulingIgnoredDuringExecution:
          - weight: 100
            podAffinityTerm:
              labelSelector:
                matchExpressions:
                - key: app
                  operator: In
                  values:
                  - spring-ai-rag
              topologyKey: kubernetes.io/hostname
```

### 2. Service 和 Ingress

`k8s/service.yaml`

```yaml
apiVersion: v1
kind: Service
metadata:
  name: spring-ai-rag-service
  namespace: production
spec:
  selector:
    app: spring-ai-rag
  ports:
  - port: 80
    targetPort: 8080
    protocol: TCP
    name: http
  type: ClusterIP

---
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: spring-ai-rag-ingress
  namespace: production
  annotations:
    kubernetes.io/ingress.class: nginx
    cert-manager.io/cluster-issuer: letsencrypt-prod
    nginx.ingress.kubernetes.io/rate-limit: "100"
    nginx.ingress.kubernetes.io/rate-limit-window: "1m"
spec:
  tls:
  - hosts:
    - api.yourdomain.com
    secretName: spring-ai-rag-tls
  rules:
  - host: api.yourdomain.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: spring-ai-rag-service
            port:
              number: 80
```

## 監控 Dashboard

### 1. Grafana Dashboard 配置

`monitoring/grafana/dashboards/spring-ai-dashboard.json`

```json
{
  "dashboard": {
    "id": null,
    "title": "Spring AI RAG System",
    "tags": ["spring-ai", "rag"],
    "timezone": "browser",
    "panels": [
      {
        "title": "AI Request Rate",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(ai_requests_total[5m])",
            "legendFormat": "{{operation}}"
          }
        ]
      },
      {
        "title": "AI Request Duration",
        "type": "graph",
        "targets": [
          {
            "expr": "histogram_quantile(0.95, rate(ai_request_duration_bucket[5m]))",
            "legendFormat": "95th percentile"
          }
        ]
      },
      {
        "title": "Vector Store Performance",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(vectorstore_search_total[5m])",
            "legendFormat": "Search Rate"
          }
        ]
      }
    ],
    "time": {
      "from": "now-1h",
      "to": "now"
    },
    "refresh": "30s"
  }
}
```

## 部署自動化

### 1. GitHub Actions CI/CD

`.github/workflows/deploy.yml`

```yaml
name: Deploy to Production

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v4
    - name: Set up JDK 21
      uses: actions/setup-java@v4
      with:
        java-version: '21'
        distribution: 'temurin'
    - name: Cache Maven packages
      uses: actions/cache@v3
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
    - name: Run tests
      run: mvn clean test

  build-and-push:
    needs: test
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    steps:
    - uses: actions/checkout@v4
    - name: Build Docker image
      run: docker build -t spring-ai-rag:${{ github.sha }} .
    - name: Push to Registry
      run: |
        echo ${{ secrets.DOCKER_PASSWORD }} | docker login -u ${{ secrets.DOCKER_USERNAME }} --password-stdin
        docker push spring-ai-rag:${{ github.sha }}

  deploy:
    needs: build-and-push
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    steps:
    - name: Deploy to Kubernetes
      run: |
        kubectl set image deployment/spring-ai-rag spring-ai-rag=spring-ai-rag:${{ github.sha }}
        kubectl rollout status deployment/spring-ai-rag
```

## 回顧

今天學到了什麼？

- Docker 多階段建置和容器化最佳實踐
- Docker Compose 完整的微服務架構配置
- 生產環境的安全配置和 JWT 認證
- 企業級監控和指標收集
- JVM 和資料庫連接池調優
- Kubernetes 部署配置和 Ingress 設定
- Grafana Dashboard 和告警配置
- CI/CD 自動化部署流程

企業級部署需要考慮安全性、可擴展性、監控性和維護性等多個面向，每個環節都需要精心設計和優化。

## Source Code

程式碼下載: [https://github.com/kevintsai1202/SpringBoot-AI-Day33-Updated.git](https://github.com/kevintsai1202/SpringBoot-AI-Day33-Updated.git)