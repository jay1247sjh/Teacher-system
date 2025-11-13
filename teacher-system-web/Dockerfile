# Build stage
FROM ox26n5h75wajyn.xuanyuan.run/node:20-alpine AS build

WORKDIR /app

# Copy package files
COPY teacher-system-web/package*.json ./

# Install dependencies (including devDependencies for build)
RUN npm ci && npm cache clean --force

# Copy source code
COPY teacher-system-web/ ./

# Build the application
RUN npm run build

# Production stage
FROM ox26n5h75wajyn.xuanyuan.run/nginx:alpine AS runtime

# Install curl for healthcheck
RUN apk add --no-cache curl

# Copy custom nginx configuration
COPY teacher-system-web/nginx.conf /etc/nginx/conf.d/default.conf

# Copy built files from build stage
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD curl -fsS http://localhost:80/ || exit 1

CMD ["nginx", "-g", "daemon off;"]

