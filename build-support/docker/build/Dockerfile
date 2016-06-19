FROM java:7

ARG CURRENT_USER_ID
ARG CURRENT_GROUP_ID

RUN addgroup --gid "${CURRENT_GROUP_ID}" builder
RUN adduser --uid "${CURRENT_USER_ID}" --gid "${CURRENT_GROUP_ID}" --disabled-password --disabled-login builder

USER builder

WORKDIR /project
VOLUME /project
VOLUME /home/builder/.gradle
