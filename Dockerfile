FROM nickstenning/java7 

RUN apt-get update -y && apt-get upgrade -y

RUN wget http://repo1.maven.org/maven2/org/eclipse/jetty/jetty-runner/9.2.0.M0/jetty-runner-9.2.0.M0.jar -O /root/jetty.jar
RUN mkdir /var/lib/floraconnect

ADD target/flora-connect-0.1.0-standalone.war /root/connect.war
ADD start.sh /root/start.sh
RUN chmod +x /root/start.sh

EXPOSE 8080

VOLUME ["/var/floraconnect"]
CMD ["/root/start.sh"]

