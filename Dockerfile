FROM cncflora/java8

RUN apt-get install supervisor ruby -y

RUN mkdir /var/lib/floraconnect

RUN wget http://repo1.maven.org/maven2/org/eclipse/jetty/jetty-runner/9.2.0.M0/jetty-runner-9.2.0.M0.jar -O /root/jetty.jar

ADD start.sh /root/start.sh
RUN chmod +x /root/start.sh

ADD supervisord.conf /etc/supervisor/conf.d/connect.conf

EXPOSE 8080

VOLUME ["/var/floraconnect"]

ADD target/flora-connect-0.2.7-standalone.war /root/connect.war

