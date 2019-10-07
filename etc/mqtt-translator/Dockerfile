FROM node:9.10.1
COPY ./node_modules /app/node_modules/
COPY ./*.js /app/
WORKDIR /app
USER node
EXPOSE 1883
# next two lines for development only
#EXPOSE 9229
#CMD node --inspect-brk=0.0.0.0:9229 client.js
CMD node client.js