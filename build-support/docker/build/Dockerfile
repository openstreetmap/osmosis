FROM java:8

ARG CURRENT_USER_ID
ARG CURRENT_GROUP_ID

RUN mkdir /gradle
RUN chown "${CURRENT_USER_ID}":"${CURRENT_GROUP_ID}" /gradle
ENV GRADLE_USER_HOME /gradle

# Fix the broken PS1 prompt caused by the runtime UID not existing in the container.
RUN echo "PS1=\"[osmosis-dev \\w]\\$ \"" >> /etc/bash.bashrc

USER "${CURRENT_USER_ID}":"${CURRENT_GROUP_ID}"

WORKDIR /project
VOLUME /project
VOLUME /home/builder/.gradle
