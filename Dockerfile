ARG APP_INSIGHTS_AGENT_VERSION=3.5.4

# Application image

FROM hmctspublic.azurecr.io/base/java:21-distroless

COPY lib/applicationinsights.json /opt/app/
COPY build/libs/wa-task-monitor.jar /opt/app/

EXPOSE 8077
CMD [ "wa-task-monitor.jar" ]
