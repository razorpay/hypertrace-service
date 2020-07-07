package org.hypertrace.gateway.service.entity.config;

import com.typesafe.config.Config;
import org.hypertrace.core.attribute.service.v1.AttributeScope;

public class DomainObjectMapping {
  private static final String VALUE = "value";
  private AttributeScope scope;
  private String key;
  private DomainObjectFilter filter;

  public DomainObjectMapping(AttributeScope scope, String key, Config filterConfig) {
    this.scope = scope;
    this.key = key;
    if (filterConfig != null) {
      String filterValue = filterConfig.getString(VALUE);
      this.filter = new DomainObjectFilter(filterValue);
    }
  }

  public AttributeScope getScope() {
    return scope;
  }

  public String getKey() {
    return key;
  }

  public DomainObjectFilter getFilter() {
    return filter;
  }

  @Override
  public String toString() {
    return "DomainObjectMapping{"
        + "scope="
        + scope
        + ", key='"
        + key
        + '\''
        + ", filter="
        + filter
        + '}';
  }
}
