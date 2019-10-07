FROM node:9.10.1
COPY ./node_modules /app/node_modules/
COPY ./weather.js /app/
WORKDIR /app
USER node
EXPOSE 1883
EXPOSE 9229
#CMD node --inspect weather.js generate redis 5000
CMD node weather.js generate redis 5000
