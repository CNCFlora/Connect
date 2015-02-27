FROM cncflora/java8

RUN mkdir /var/lib/floraconnect

RUN curl http://repo1.maven.org/maven2/org/eclipse/jetty/jetty-runner/9.2.2.v20140723/jetty-runner-9.2.2.v20140723.jar -o /root/jetty.jar

EXPOSE 80
ENV PORT 80

VOLUME ["/var/floraconnect"]
CMD ["/root/start.sh"]

ADD start.sh /root/start.sh
RUN chmod +x /root/start.sh

ADD target/flora-connect-0.2.7-standalone.war /root/connect.war

