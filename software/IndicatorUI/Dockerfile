#
# build master image with `docker build -t indicator-ui:master .`
#

FROM node:12-alpine AS build-stage

WORKDIR /webapp

# copy packages
COPY package*.json /webapp/
# install packages if needed
RUN npm install --loglevel verbose
#copy app data
COPY . /webapp/
# build app
RUN /webapp/node_modules/@angular/cli/bin/ng build  --prod --configuration=prod

# serve
FROM nginx:stable-alpine AS serve-stage

RUN sed -i '/root.*\/usr\/share\/nginx\/html;/ a\ \ttry_files $uri /index.html;' /etc/nginx/conf.d/default.conf

WORKDIR /opt/webapp

RUN rm -r /usr/share/nginx/html/*

COPY --from=build-stage /webapp/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]

