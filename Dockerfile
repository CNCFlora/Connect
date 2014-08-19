FROM dockerfile/java

RUN apt-get install supervisor ruby -y

RUN mkdir /var/log/supervisord 
RUN mkdir /var/lib/floraconnect

RUN wget http://repo1.maven.org/maven2/org/eclipse/jetty/jetty-runner/9.2.0.M0/jetty-runner-9.2.0.M0.jar -O /root/jetty.jar

RUN gem sources -r http://rubygems.org/ && gem sources -a https://rubygems.org/ && gem install small-ops -v 0.0.30

ADD start.sh /root/start.sh
RUN chmod +x /root/start.sh

ADD target/flora-connect-0.2.6-standalone.war /root/connect.war

ADD supervisord.conf /etc/supervisor/conf.d/proxy.conf

EXPOSE 8080
EXPOSE 9001

VOLUME ["/var/floraconnect"]
CMD ["supervisord"]

