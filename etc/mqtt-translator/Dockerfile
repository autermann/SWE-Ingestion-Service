FROM node:9.10.1
COPY ./node_modules /app/node_modules/
COPY ./client.js /app/
WORKDIR /app
USER node
EXPOSE 1883
EXPOSE 9229
CMD node --inspect-brk=0.0.0.0:9229 client.js
#CMD node weather.js generate redis 5000
