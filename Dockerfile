FROM mhart/alpine-node:12.14.1

RUN mkdir -p /app
WORKDIR /app
COPY package.json .
RUN npm install

COPY . .
EXPOSE 8506

CMD [ "npm", "run-script", "prod" ]
