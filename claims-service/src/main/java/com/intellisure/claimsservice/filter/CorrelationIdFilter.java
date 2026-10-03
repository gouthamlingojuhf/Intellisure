package com.intellisure.claimsservice.filter;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
@Component
public class CorrelationIdFilter implements WebFilter {
  public static final String H = "X-Correlation-ID";
  @Override public Mono<Void> filter(ServerWebExchange ex, WebFilterChain chain) {
    String c = ex.getRequest().getHeaders().getFirst(H);
    if (c == null || c.isBlank()) c = UUID.randomUUID().toString();
    ServerWebExchange m = ex.mutate().request(ex.getRequest().mutate().header(H, c).build()).build();
    m.getResponse().getHeaders().add(H, c);
    return chain.filter(m);
  }
}
