package com.github.kaydunovdenis.config.hibernate;

import com.sap.cloud.sdk.cloudplatform.tenant.exception.TenantAccessException;
import com.sap.cloud.security.xsuaa.token.AuthenticationToken;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver {

  @Value("${multitenant.defaultTenant}")
  private String defaultTenant;

  @Override
  public String resolveCurrentTenantIdentifier() {
    try {
      AuthenticationToken authToken = (AuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
      if (Objects.nonNull(authToken)) {
        Map<String, Object> attributes = authToken.getTokenAttributes();
        if (Objects.nonNull(attributes)) {
          String tenant = (String) attributes.get("zid");
          return isValidTenant(tenant) ? tenant : defaultTenant;
        }
      }
    } catch (TenantAccessException e) {
      log.warn("Tenant not found", e);
    }
    return defaultTenant;
  }

  private static boolean isValidTenant(String tenant) {
    return Objects.nonNull(tenant) && !Objects.equals("sap-provisioning", tenant);
  }

  @Override
  public boolean validateExistingCurrentSessions() {
    return true;
  }
}
