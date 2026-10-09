FROM node:22-alpine AS build

WORKDIR /app
COPY PrismUI/package*.json ./
RUN npm ci
COPY PrismUI/ ./
RUN npm run build

FROM nginxinc/nginx-unprivileged:stable-alpine

COPY PrismUI/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 8080