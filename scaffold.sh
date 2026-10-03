#!/bin/bash
set -e

SERVICES=(
  "risk-underwriting-service:8083:risk_underwriting_db:RiskUnderwritingServiceApplication"
  "claims-service:8084:claims_db:ClaimsServiceApplication"
  "vendor-partner-service:8085:vendor_partner_db:VendorPartnerServiceApplication"
  "recovery-service:8086:recovery_continuity_db:RecoveryServiceApplication"
  "workflow-notification-service:8087:workflow_notification_db:WorkflowNotificationServiceApplication"
  "document-audit-service:8088:document_audit_db:DocumentAuditServiceApplication"
  "analytics-intelligence-service:8089:analytics_intelligence_db:AnalyticsIntelligenceServiceApplication"
)

SOURCE="quote-policy-service"

for entry in "${SERVICES[@]}"; do
  IFS=':' read -r NAME PORT DB CLASSNAME <<< "$entry"
  echo "Scaffolding $NAME..."
  
  # Copy directory
  cp -r $SOURCE $NAME
  
  # Update POM
  sed -i '' "s/<artifactId>$SOURCE<\/artifactId>/<artifactId>$NAME<\/artifactId>/g" $NAME/pom.xml
  sed -i '' "s/<name>$SOURCE<\/name>/<name>$NAME<\/name>/g" $NAME/pom.xml
  sed -i '' "s/Quote and Policy Service/$NAME/g" $NAME/pom.xml
  
  # Update Application config
  sed -i '' "s/port: 8082/port: $PORT/g" $NAME/src/main/resources/application.yaml
  sed -i '' "s/quote_policy_db/$DB/g" $NAME/src/main/resources/application.yaml
  sed -i '' "s/name: $SOURCE/name: $NAME/g" $NAME/src/main/resources/application.yaml
  
  # Rename package and class
  mv $NAME/src/main/java/com/intellisure/quotepolicyservice $NAME/src/main/java/com/intellisure/$(echo $NAME | tr -d '-')
  mv $NAME/src/main/java/com/intellisure/$(echo $NAME | tr -d '-')/QuotePolicyServiceApplication.java $NAME/src/main/java/com/intellisure/$(echo $NAME | tr -d '-')/${CLASSNAME}.java
  
  # Update main class contents
  sed -i '' "s/package com.intellisure.quotepolicyservice;/package com.intellisure.$(echo $NAME | tr -d '-');/g" $NAME/src/main/java/com/intellisure/$(echo $NAME | tr -d '-')/${CLASSNAME}.java
  sed -i '' "s/public class QuotePolicyServiceApplication/public class ${CLASSNAME}/g" $NAME/src/main/java/com/intellisure/$(echo $NAME | tr -d '-')/${CLASSNAME}.java
  sed -i '' "s/QuotePolicyServiceApplication.class/${CLASSNAME}.class/g" $NAME/src/main/java/com/intellisure/$(echo $NAME | tr -d '-')/${CLASSNAME}.java
  
  # Remove the existing code specific to Quote Policy
  rm -rf $NAME/src/main/java/com/intellisure/$(echo $NAME | tr -d '-')/{config,controller,converter,dto,entity,exception,mapper,repository,service}
  rm -f $NAME/src/main/resources/schema.sql
  
  # Clean tests
  rm -rf $NAME/src/test/java/com/intellisure/quotepolicyservice
  mkdir -p $NAME/src/test/java/com/intellisure/$(echo $NAME | tr -d '-')
  cat <<EOF > $NAME/src/test/java/com/intellisure/$(echo $NAME | tr -d '-')/${CLASSNAME}Tests.java
package com.intellisure.$(echo $NAME | tr -d '-');

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ${CLASSNAME}Tests {

	@Test
	void contextLoads() {
	}

}
EOF
done

echo "Done."
