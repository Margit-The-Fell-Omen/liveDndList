# ----- Build Stage -----
FROM node:26-alpine@sha256:e88a35be04478413b7c71c455cd9865de9b9360e1f43456be5951032d7ac1a66 AS builder

WORKDIR /app

COPY package*.json .

RUN --mount=type=cache,target=/root/.npm npm ci

COPY . .

RUN npm run build

# ----- Runtime Stage -----
FROM nginx:1.31-alpine@sha256:54f2a904c251d5a34adf545a72d32515a15e08418dae0266e23be2e18c66fefa

COPY nginx.conf /etc/nginx/conf.d/live-dnd-list.duckdns.org.conf

COPY --from=builder /app/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
