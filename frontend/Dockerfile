# Build stage
FROM node:22-alpine AS build
WORKDIR /app

# Accept Vite env vars at build time
ARG VITE_API_URL
ARG VITE_API_TIMEOUT_MS
ARG VITE_API_WITH_CREDENTIALS
ENV VITE_API_URL=$VITE_API_URL
ENV VITE_API_TIMEOUT_MS=$VITE_API_TIMEOUT_MS
ENV VITE_API_WITH_CREDENTIALS=$VITE_API_WITH_CREDENTIALS

COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

# Production stage
FROM nginx:alpine
# Use custom nginx config that supports SPA routing
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]