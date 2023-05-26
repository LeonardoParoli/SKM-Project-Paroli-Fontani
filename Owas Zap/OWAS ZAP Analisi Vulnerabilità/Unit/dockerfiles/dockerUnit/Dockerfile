FROM jboss/wildfly:25.0.0.Final

LABEL mantainer="Giovanni Fontani giovanni.fontani1@stud.unifi.it" 

ENV WILDFLY_USER igor
ENV WILDFLY_PASS frederich1
ENV JBOSS_CLI /opt/jboss/wildfly/bin/jboss-cli.sh
ENV DEPLOYMENT_DIR /opt/jboss/wildfly/standalone/deployments/
ENV DB_NAME unit
ENV DB_USER javaclient
ENV DB_PASS password1!
ENV DB_URI db4unit:3306
ENV MYSQL_VERSION 8.0.29

USER root
RUN yum install iptables-services su -y
RUN systemctl enable iptables
USER jboss
RUN cd wildfly/standalone/configuration/ && \
mkdir certs && \
cd certs && \
keytool -genkeypair -outfile wildfly.jks -alias wildfly -keyalg RSA -keystore wildfly.jks -validity 365 -keypass progettobellini2023 -storepass progettobellini2023 -dname "CN=Leovanni_Parontani,OU=cocoa,O=tacchini_group,L=Firenze,ST=Toscana,C=IT"


RUN echo "Building wildfly25"
RUN echo "=> Adding administrator user"
RUN $JBOSS_HOME/bin/add-user.sh -u $WILDFLY_USER -p $WILDFLY_PASS --silent

# Configurazione di Wildfly, una volta attivato si usa il JbossCLI
# per aggiungere i settaggi necessari ad eseguire l'applicazione che sarà deployata
# Le azioni sono spiegate negli "echo"
RUN echo "=> Starting WildFly server" && \
      bash -c '$JBOSS_HOME/bin/standalone.sh &' && \
    echo "=> Waiting for the server to boot" && \
      bash -c 'until `$JBOSS_CLI -c ":read-attribute(name=server-state)" 2> /dev/null | grep -q running`; do echo `$JBOSS_CLI -c ":read-attribute(name=server-state)" 2> /dev/null`; sleep 1; done' && \
    echo "=> Downloading MySQL driver" && \
      curl --location --output /tmp/mysql-connector-java-${MYSQL_VERSION}.jar --url http://search.maven.org/remotecontent?filepath=mysql/mysql-connector-java/${MYSQL_VERSION}/mysql-connector-java-${MYSQL_VERSION}.jar && \
    echo "=> Adding MySQL module" && \
      $JBOSS_CLI --connect --command="module add --name=com.mysql --resources=/tmp/mysql-connector-java-${MYSQL_VERSION}.jar --dependencies=javax.api,javax.transaction.api" && \
    echo "=> Adding MySQL driver" && \
      $JBOSS_CLI --connect --command="/subsystem=datasources/jdbc-driver=mysql:add(driver-name=mysql,driver-module-name=com.mysql)" && \
    echo "=> Adding main Datasource" && \
      $JBOSS_CLI --connect --command="data-source add \
        --name=${DB_NAME}DS \
        --jndi-name=java:jboss/datasources/${DB_NAME}DS \
        --user-name=${DB_USER} \
        --password=${DB_PASS} \
        --driver-name=mysql \
        --connection-url=jdbc:mysql://db4unit:3306/unitDS?serverTimezone=UTC \
        --use-ccm=false \
        --blocking-timeout-wait-millis=5000 \
        --enabled=true" && \
    echo "=>Cyclic Deployment Scanner" && \
      $JBOSS_CLI --connect --command="/subsystem=deployment-scanner/scanner=default/:write-attribute(name=scan-interval,value=5000)" && \
    echo "=> Setting up HTTPS: wildflyKeyStore" && \
      $JBOSS_CLI --connect --command="/subsystem=elytron/key-store=wildflyKS:add(path=certs/wildfly.jks, relative-to=jboss.server.config.dir,credential-reference={clear-text="progettobellini2023"})" && \
    echo "=> Setting up HTTPS: wildflyKeyManager" && \
      $JBOSS_CLI --connect --command="/subsystem=elytron/key-manager=wildflyKM:add(key-store=wildflyKS, algorithm="SunX509", credential-reference={clear-text="progettobellini2023"})" && \
    echo "=> Setting up HTTPS: Server SSL Context SSC" && \
      $JBOSS_CLI --connect --command="/subsystem=elytron/server-ssl-context=httpsSSC:add(key-manager=wildflyKM, protocols=["TLSv1.2"])" && \
    echo "=> Setting up HTTPS: Server https-listener" && \
      $JBOSS_CLI --connect --command="/subsystem=undertow/server=default-server/https-listener=https:read-attribute(name=security-realm)" && \
    echo "=> Setting up HTTPS: Server https-listener" && \
      $JBOSS_CLI --connect --command="/subsystem=undertow/server=default-server/https-listener=https:undefine-attribute(name=security-realm)" && \
    echo "=> Setting up HTTPS: Server https-listener" && \
      $JBOSS_CLI --connect --command="/subsystem=undertow/server=default-server/https-listener=https:write-attribute(name=ssl-context, value=httpsSSC)" && \
    echo "=> Shutting down WildFly and Cleaning up" && \
      $JBOSS_CLI --connect --command=":shutdown" && \
      rm -rf $JBOSS_HOME/standalone/configuration/standalone_xml_history/ $JBOSS_HOME/standalone/log/* && \
      rm -f /tmp/*.jar

# Si espongono le porte tramite cui si può interagire con il contenuto del container
# 8080 per l'applicazione, 9990 è la console admin di Wildfly, 
# 5005 per consentier il Debug da remoto - questa porta andrà indicata nell IDE da cui si intende debuggare l'applicazione
EXPOSE 443 9990 5005 

#echo "=> Restarting WildFly"
# Questo è il comando che sarà eseguito ogni volta che si avvia il container. Tutto ciò che viene prima è eseguito solo in fase di inizializzazione
# Quello che si fa è avviare Wildfly in modalità standalone ed aprire le interfacce per accedere ad applicazione, pagina admin del server, e debug
CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0", "--debug", "*:5005"]
USER root