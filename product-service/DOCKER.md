# Docker Configuration for Product Service

## Overview

This document explains the Docker setup for the Product Service, including the multi-stage build process and the benefits of using Alpine-based JRE images.

## Multi-Stage Dockerfile Architecture

### Stage 1: Build Stage
```dockerfile
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
```

**Purpose**: Compile the Spring Boot application

**Key Features**:
- Uses Maven Alpine image for smaller build layer
- Downloads dependencies separately (layer caching optimization)
- Skips tests with `-DskipTests` for faster builds
- Produces the final JAR artifact

**Size**: ~400MB (not included in final image)

### Stage 2: Runtime Stage
```dockerfile
FROM eclipse-temurin:17-jre-alpine
```

**Purpose**: Run the compiled application

**Key Features**:
- Uses JRE-only image (no compilation tools)
- Alpine Linux base for minimal footprint
- Non-root user execution
- Optimized JVM settings

**Size**: ~150-200MB (final image size)

## Alpine JRE vs Full JDK: Benefits Analysis

### 1. **Image Size Reduction** 🎯

| Image Type | Base OS | Approximate Size | Use Case |
|------------|---------|------------------|----------|
| `eclipse-temurin:17-jdk` | Ubuntu/Debian | ~450-500MB | Development, requires compilation tools |
| `eclipse-temurin:17-jdk-alpine` | Alpine Linux | ~330-350MB | Build stage, smaller footprint |
| `eclipse-temurin:17-jre` | Ubuntu/Debian | ~250-300MB | Runtime with standard libs |
| `eclipse-temurin:17-jre-alpine` | Alpine Linux | **~150-200MB** | ✅ **Production runtime** |

**Savings**: ~60-70% smaller than full JDK images

### 2. **Security Benefits** 🔒

**Alpine Linux Advantages**:
- **Minimal attack surface**: Contains only essential packages
- **Fewer vulnerabilities**: Smaller codebase = fewer CVEs to patch
- **Regular security updates**: Alpine team maintains security patches
- **No unnecessary tools**: No compilers, debuggers, or build tools in production

**JRE vs JDK Security**:
- **JRE**: Contains only runtime libraries (fewer components to secure)
- **JDK**: Includes javac, jdb, and other development tools (unnecessary in production)
- **Principle of Least Privilege**: Only include what's needed to run the app

### 3. **Performance Benefits** ⚡

**Faster Image Pulls**:
- Smaller images download faster from registries
- Quick deployments in Kubernetes/container orchestras
- Reduced bandwidth costs in multi-region deployments

**Reduced Resource Usage**:
- Less disk space per container
- Lower memory footprint on host machine
- More containers per host in high-density deployments

**Startup Optimization**:
```dockerfile
-Djava.security.egd=file:/dev/./urandom
```
- Uses `/dev/urandom` for faster random number generation
- Reduces startup time in containerized environments

### 4. **Cost Optimization** 💰

**Storage Costs**:
- Registry storage: Pay for less data stored
- Edge locations: Cheaper CDN distribution
- Backup costs: Smaller backup sizes

**Network Costs**:
- ~60-70% less data transfer
- Important in multi-region deployments
- Reduced egress costs from cloud providers

**Compute Costs**:
- More containers per VM/node
- Better resource utilization
- Lower infrastructure costs

### 5. **Operational Benefits** 🚀

**CI/CD Pipeline**:
- Faster build times (smaller layers to cache)
- Quicker test deployments
- Reduced pipeline execution costs

**Container Orchestration**:
- Faster pod scheduling in Kubernetes
- Quicker rolling updates
- Better use of image pull policies

**Disaster Recovery**:
- Faster recovery times
- Quicker image restoration
- Reduced RTO (Recovery Time Objective)

## JVM Memory Configuration

### Default Settings
```dockerfile
ENTRYPOINT ["java", "-Xmx384m", "-Xms128m", ...]
```

**Explanation**:
- `-Xms128m`: Initial heap size (128MB)
- `-Xmx384m`: Maximum heap size (384MB)
- **Reasoning**: Prevents OOM kills in resource-constrained environments

### Custom Settings
Override via environment variables:
```bash
docker run -e JAVA_OPTS="-Xmx512m -Xms256m" product-service:latest
```

### Memory Calculation Formula
```
Container Memory = Heap (-Xmx) + MetaSpace + Native + Overhead
Recommended: Container Memory ≈ 1.5 × Heap Size
```

**Example**:
- Heap: 384MB
- Recommended container limit: ~600MB

## Security Best Practices Implemented

### 1. **Non-Root User Execution**
```dockerfile
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser
```

**Benefits**:
- Prevents privilege escalation attacks
- Complies with security policies (PCI-DSS, SOC2)
- Container escapes have limited impact

### 2. **Read-Only Filesystem** (Optional Enhancement)
```bash
docker run --read-only --tmpfs /tmp product-service:latest
```

### 3. **Minimal Base Image**
- Alpine Linux has minimal packages pre-installed
- Uses musl libc instead of glibc (smaller, simpler)
- Regular security updates from Alpine team

## Comparison: Traditional vs Multi-Stage Build

### Traditional Single-Stage Build
```dockerfile
FROM eclipse-temurin:17-jdk
# Contains: JDK + Maven + Source + Dependencies
# Final Size: ~600-700MB
```

**Problems**:
- ❌ Includes build tools in production
- ❌ Source code in final image
- ❌ Large image size
- ❌ Security vulnerabilities from build tools

### Multi-Stage Build (Current Implementation)
```dockerfile
# Stage 1: Builder (discarded)
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
# Stage 2: Runtime (final image)
FROM eclipse-temurin:17-jre-alpine
# Final Size: ~150-200MB
```

**Benefits**:
- ✅ Only JAR file in final image
- ✅ No build tools or source code
- ✅ 70% smaller image
- ✅ Better security posture

## Build and Run Commands

### Build Image
```bash
docker build -t product-service:1.0.0 .
```

### Run Container (Development)
```bash
docker run -d \
  --name product-service \
  -p 8081:8081 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/stock_db \
  -e SPRING_DATASOURCE_USERNAME=admin \
  -e SPRING_DATASOURCE_PASSWORD=adminpassword \
  product-service:1.0.0
```

### Run Container (Production)
```bash
docker run -d \
  --name product-service \
  --restart unless-stopped \
  --memory="600m" \
  --cpus="1.0" \
  --read-only \
  --tmpfs /tmp \
  -p 8081:8081 \
  -e SPRING_DATASOURCE_URL=${DB_URL} \
  -e SPRING_DATASOURCE_USERNAME=${DB_USER} \
  -e SPRING_DATASOURCE_PASSWORD=${DB_PASS} \
  -e SPRING_PROFILES_ACTIVE=prod \
  product-service:1.0.0
```

## Layer Caching Optimization

### Dependency Caching Strategy
```dockerfile
# Step 1: Copy only pom.xml
COPY pom.xml .
# Step 2: Download dependencies (cached layer)
RUN mvn dependency:go-offline -B
# Step 3: Copy source code
COPY src ./src
# Step 4: Build (only rebuilt if source changes)
RUN mvn clean package -DskipTests -B
```

**Benefits**:
- Dependencies downloaded once, cached
- Source code changes don't trigger dependency re-download
- 10x faster subsequent builds

## Alpine Linux Trade-offs

### Advantages ✅
- Minimal size (~5MB base)
- Security-focused
- Fast package manager (apk)
- Regular updates

### Considerations ⚠️
- Uses `musl libc` instead of `glibc`
- Some native libraries may require recompilation
- Different package names than Debian/Ubuntu
- Smaller community than mainstream Linux distros

### For Java Applications
- ✅ **Excellent choice**: JVM abstracts OS differences
- ✅ Pure Java apps work perfectly
- ⚠️ Native dependencies (if any) may need Alpine-specific versions

## Monitoring and Debugging

### Check Container Logs
```bash
docker logs -f product-service
```

### Execute Commands Inside Container
```bash
docker exec -it product-service sh
```

### Health Check
```bash
curl http://localhost:8081/actuator/health
```

### Memory Usage
```bash
docker stats product-service
```

## Recommended Next Steps

1. **Add Health Check**:
```dockerfile
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8081/actuator/health || exit 1
```

2. **Image Scanning**:
```bash
docker scan product-service:1.0.0
```

3. **Registry Push**:
```bash
docker tag product-service:1.0.0 your-registry/product-service:1.0.0
docker push your-registry/product-service:1.0.0
```

4. **Container Orchestration**:
- Create Kubernetes deployment manifests
- Configure horizontal pod autoscaling
- Set resource limits and requests

## Conclusion

The Alpine-based JRE approach provides:
- **70% smaller images** compared to full JDK
- **Better security** through minimal attack surface
- **Cost savings** in storage, network, and compute
- **Faster deployments** and better CI/CD performance
- **Production-ready** with non-root user and signal handling

This configuration balances size, security, and performance for production Spring Boot deployments.
