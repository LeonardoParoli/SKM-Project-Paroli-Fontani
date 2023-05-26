sh /opt/jboss/
sh /opt/jboss/keycloak/bin/standalone.sh -b 0.0.0.0 & sleep 35 && 
sh /opt/jboss/keycloak/bin/kcadm.sh config credentials --server http://localhost:8080/auth --realm master --user admin --password admin &&
sh /opt/jboss/keycloak/bin/kcadm.sh set-password -r master --username admin --new-password ${BELLINI_PROJECT_PASSWORD} --temporary=false &&
sh /opt/jboss/keycloak/bin/kcadm.sh create realms -s realm=cocoa-infra -s enabled=true && 
sh /opt/jboss/keycloak/bin/kcadm.sh create clients -r cocoa-infra -s clientId=unit -s enabled=true -s secret=tallalinotallaliniUnit -s directAccessGrantsEnabled=true -s 'redirectUris=["https://localhost:8443/*"]' &&
UNIT_CLIENT_ID=$(sh /opt/jboss/keycloak/bin/kcadm.sh get realms/cocoa-infra/clients?clientId=unit -r cocoa-infra --fields id | sed -n 's/.*"id" : "\(.*\)".*/\1/p') &&
sh /opt/jboss/keycloak/bin/kcadm.sh create clients/$UNIT_CLIENT_ID/protocol-mappers/models -r cocoa-infra -b '{"name": "realm roles", "protocol": "openid-connect", "protocolMapper": "oidc-usermodel-realm-role-mapper", "consentRequired": false, "config": {"access.token.claim": "true", "claim.name": "realm_access.roles", "id.token.claim": "true", "jsonType.label": "String", "multivalued": true, "user.attribute": "foo"}}' &&
sh /opt/jboss/keycloak/bin/kcadm.sh create clients -r cocoa-infra -s clientId=catalog -s enabled=true -s secret=tallalinotallalinicatalog -s directAccessGrantsEnabled=true -s 'redirectUris=["https://localhost:8443/*"]' &&
sh /opt/jboss/keycloak/bin/kcadm.sh create clients -r cocoa-infra -s clientId=contractor -s enabled=true -s secret=tallalinotallalinicontractor -s directAccessGrantsEnabled=true -s 'redirectUris=["https://localhost:8443/*"]' &&
sh /opt/jboss/keycloak/bin/kcadm.sh create clients -r cocoa-infra -s clientId=optimization -s enabled=true -s secret=tallalinotallalinioptimization  -s directAccessGrantsEnabled=true -s 'redirectUris=["https://localhost:8443/*"]' &&
sh /opt/jboss/keycloak/bin/kcadm.sh create clients -r cocoa-infra -s clientId=parties -s enabled=true -s secret=tallalinotallaliniparties -s directAccessGrantsEnabled=true -s 'redirectUris=["https://localhost:8443/*"]' &&
sh /opt/jboss/keycloak/bin/kcadm.sh create clients -r cocoa-infra -s clientId=production -s enabled=true -s secret=tallalinotallaliniproduction -s directAccessGrantsEnabled=true -s 'redirectUris=["https://localhost:8443/*"]' &&
sh /opt/jboss/keycloak/bin/kcadm.sh create clients -r cocoa-infra -s clientId=warehouse -s enabled=true -s secret=tallalinotallaliniwarehouse -s directAccessGrantsEnabled=true -s 'redirectUris=["https://localhost:8443/*"]' &&
sh /opt/jboss/keycloak/bin/kcadm.sh create roles -r cocoa-infra -s name=ADMINISTRATOR -s description="Realm administrator role" -s composite=true -s clientRole=false -s containerId=cocoa-infra &&
sh /opt/jboss/keycloak/bin/kcadm.sh create users -r cocoa-infra -s username=admin -s enabled=true &&
sh /opt/jboss/keycloak/bin/kcadm.sh set-password -r cocoa-infra --username admin --new-password ${BELLINI_PROJECT_PASSWORD} &&
sh /opt/jboss/keycloak/bin/kcadm.sh add-roles -r cocoa-infra --uusername admin --rolename ADMINISTRATOR &&
sh /opt/jboss/keycloak/bin/kcadm.sh create roles -r cocoa-infra -s name=customer -s description="Customer realm role" -s containerId=cocoa-infra &&
sh /opt/jboss/keycloak/bin/kcadm.sh create roles -r cocoa-infra -s name=manager -s description="Manager realm role" -s containerId=cocoa-infra &&
sh /opt/jboss/keycloak/bin/kcadm.sh create roles -r cocoa-infra -s name=owner -s description="Owner realm role" -s containerId=cocoa-infra &&
sh /opt/jboss/keycloak/bin/kcadm.sh update realms/cocoa-infra -s "enabled=true" &&
PARENT_ID=$(sh /opt/jboss/keycloak/bin/kcadm.sh get realms/cocoa-infra -r cocoa-infra --fields id | sed -n 's/.*"id" : "\(.*\)".*/\1/p') &&
COMMAND_PASS=$(echo 'config.bindCredential=["'$BELLINI_PROJECT_PASSWORD'"]')
sh /opt/jboss/keycloak/bin/kcadm.sh create components -r cocoa-infra -s name="LDAP Active Directory" -s parentId=$PARENT_ID -s providerId=ldap -s providerType=org.keycloak.storage.UserStorageProvider -s 'config.priority=["1"]' -s 'config.fullSyncPeriod'=["604800"] -s 'config.changedSyncPeriod'=["86400"] -s 'config.cachePolicy=["DEFAULT"]' -s 'config.batchSizeForSync=["1000"]' -s 'config.editMode=["WRITABLE"]' -s 'config.syncRegistrations=["true"]' -s 'config.vendor=["other"]' -s 'config.usernameLDAPAttribute=["uid"]' -s 'config.rdnLDAPAttribute=["uid"]' -s 'config.uuidLDAPAttribute=["entryUUID"]' -s 'config.userObjectClasses=["inetOrgPerson, organizationalPerson"]' -s 'config.connectionUrl=["ldap://ldap:389"]' -s 'config.usersDn=["dc=example,dc=org"]' -s 'config.authType=["simple"]' -s 'config.bindDn=["cn=admin,dc=example,dc=org"]' -s $COMMAND_PASS -s 'config.searchScope=["1"]' -s 'config.pagination=["true"]' -s 'config.useTruststoreSpi=["ldapsOnly"]' &&
sh /opt/jboss/keycloak/bin/kcadm.sh create users -r cocoa-infra -s username=customer -s enabled=true -s firstName=Gionardo -s lastName=Fontaroli &&
sh /opt/jboss/keycloak/bin/kcadm.sh set-password -r cocoa-infra --username customer --new-password ${BELLINI_PROJECT_PASSWORD} --temporary=false &&
sh /opt/jboss/keycloak/bin/kcadm.sh create users -r cocoa-infra -s username=manager -s enabled=true -s firstName=Jacoris -s lastName=Brizarri &&
sh /opt/jboss/keycloak/bin/kcadm.sh set-password -r cocoa-infra --username manager --new-password ${BELLINI_PROJECT_PASSWORD} --temporary=false &&
sh /opt/jboss/keycloak/bin/kcadm.sh create users -r cocoa-infra -s username=owner -s enabled=true -s firstName=PierEnrico -s lastName=Vicarellini &&
sh /opt/jboss/keycloak/bin/kcadm.sh set-password -r cocoa-infra --username=owner --new-password ${BELLINI_PROJECT_PASSWORD} --temporary=false &&
sh /opt/jboss/keycloak/bin/kcadm.sh add-roles -r cocoa-infra --uusername customer --rolename customer &&
sh /opt/jboss/keycloak/bin/kcadm.sh add-roles -r cocoa-infra --uusername manager --rolename customer &&
sh /opt/jboss/keycloak/bin/kcadm.sh add-roles -r cocoa-infra --uusername manager --rolename manager &&
sh /opt/jboss/keycloak/bin/kcadm.sh add-roles -r cocoa-infra --uusername owner --rolename customer &&
sh /opt/jboss/keycloak/bin/kcadm.sh add-roles -r cocoa-infra --uusername owner --rolename manager &&
sh /opt/jboss/keycloak/bin/kcadm.sh add-roles -r cocoa-infra --uusername owner --rolename owner &&
sh /opt/jboss/keycloak/bin/jboss-cli.sh --connect --command="/subsystem=elytron/credential-store=store:add(path=keycloak/store.jceks, relative-to=jboss.server.config.dir, credential-reference={clear-text=${BELLINI_PROJECT_PASSWORD}}, create=true)" &&
sh /opt/jboss/keycloak/bin/jboss-cli.sh --connect --command="/subsystem=elytron/credential-store=store/:add-alias(alias=STOREPASS,secret-value=progettobellini2023)" &&
sh /opt/jboss/keycloak/bin/jboss-cli.sh --connect --command="/subsystem=elytron/key-store=twoWayKS:add(path=keystores/keycloak.p12, relative-to=jboss.server.config.dir, credential-reference={store=store, alias=STOREPASS},type=PKCS12)" &&
sh /opt/jboss/keycloak/bin/jboss-cli.sh --connect --command="/subsystem=elytron/key-store=twoWayTS:add(path=keystores/keycloaktruststore.p12,  relative-to=jboss.server.config.dir,credential-reference={store=store, alias=STOREPASS},type=PKCS12)" &&
sh /opt/jboss/keycloak/bin/jboss-cli.sh --connect --command="/subsystem=elytron/key-manager=twoWayKM:add(key-store=twoWayKS,credential-reference={store=store, alias=STOREPASS})" &&
sh /opt/jboss/keycloak/bin/jboss-cli.sh --connect --command="/subsystem=elytron/trust-manager=twoWayTM:add(key-store=twoWayTS)" &&
sh /opt/jboss/keycloak/bin/jboss-cli.sh --connect --command="/subsystem=elytron/server-ssl-context=twoWaySSC:add(key-manager=twoWayKM,protocols=["TLSv1.2"],trust-manager=twoWayTM,need-client-auth=true)" &&
sh /opt/jboss/keycloak/bin/jboss-cli.sh --connect --command="/subsystem=undertow/server=default-server/https-listener=https:read-attribute(name=security-realm)" && 
sh /opt/jboss/keycloak/bin/jboss-cli.sh --connect --command="/subsystem=undertow/server=default-server/https-listener=https:undefine-attribute(name=security-realm)" &&
sh /opt/jboss/keycloak/bin/jboss-cli.sh --connect --command="/subsystem=undertow/server=default-server/https-listener=https:write-attribute(name=ssl-context,value=twoWaySSC)" &&
unset COMMAND_PASS
unset BELLINI_PROJECT_PASSWORD
rm /opt/jboss/keycloak/standalone/configuration/standalone-ha.xml &&
mv /opt/jboss/keycloak/standalone/configuration/standalone.xml /opt/jboss/keycloak/standalone/configuration/standalone-ha.xml &&
sh /opt/jboss/keycloak/bin/jboss-cli.sh --connect command=:shutdown