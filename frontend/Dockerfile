FROM node:20-alpine AS build

WORKDIR /app

COPY package*.json ./
RUN npm install

COPY . .

RUN npm run build

FROM node:20-alpine

WORKDIR /app

COPY --from=build /app/build ./


ENV HOST=0.0.0.0 PORT=3000

EXPOSE 3000

CMD ["node", "index.js"]
