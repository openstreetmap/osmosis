FROM java:7

ARG CURRENT_USER_ID
ARG CURRENT_GROUP_ID

RUN addgroup --gid 1000 builder
RUN adduser --uid 1000 --gid 1000 --disabled-password --disabled-login builder

USER builder

WORKDIR /project
VOLUME /project
VOLUME /home/builder/.gradle
