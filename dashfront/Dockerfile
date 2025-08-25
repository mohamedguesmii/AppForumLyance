# Étape build
FROM node:20 AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build --prod

# Étape run
FROM nginx:alpine
COPY --from=build /app/dist/dashfront /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
