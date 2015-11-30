FROM diogok/java8

RUN mkdir /var/lib/floraconnect

RUN curl https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-runner/9.3.3.v20150827/jetty-runner-9.3.3.v20150827.jar -o /root/jetty.jar

EXPOSE 80

ENV CONTEXT /
ENV JAVA_OPTS -server -XX:+UseConcMarkSweepGC -XX:+UseCompressedOops -XX:+DoEscapeAnalysis
ENV PORT 80

VOLUME ["/var/floraconnect"]
CMD ["/root/start.sh"]

ADD start.sh /root/start.sh
RUN chmod +x /root/start.sh

ADD target/flora-connect-0.3.0-standalone.war /root/connect.war

