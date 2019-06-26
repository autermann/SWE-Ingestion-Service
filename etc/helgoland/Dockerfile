FROM node AS BUILD

RUN apt-get update && \
    apt-get install --assume-yes --no-install-recommends git && \
    cd /tmp/ && \
    git clone https://github.com/52North/helgoland.git && \
    cd helgoland && \
    git checkout feature/sea-data-cloud && \
    npm install && \
    npm run build

FROM nginx:alpine
# COPY ${PWD}/helgoland/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=BUILD /tmp/helgoland/dist/complete /usr/share/nginx/html
