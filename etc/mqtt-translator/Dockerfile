FROM node:9.10.1
COPY ./*.js /app/
COPY ./*.json /app/
RUN cd /app/ && \
    npm install
WORKDIR /app
USER node
EXPOSE 1883
# next two lines for development only
#EXPOSE 9229
#CMD node --inspect-brk=0.0.0.0:9229 client.js
CMD node client.js

