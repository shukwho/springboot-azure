package com.example.demo.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

  private static final int MAX_BODY_LOG_CHARS = 4000;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String traceId = UUID.randomUUID().toString();
    MDC.put("traceId", traceId);
    response.setHeader("X-Trace-Id", traceId);

    Instant start = Instant.now();

    ContentCachingRequestWrapper wrappedReq = new ContentCachingRequestWrapper(request);
    ContentCachingResponseWrapper wrappedRes = new ContentCachingResponseWrapper(response);

    try {
      filterChain.doFilter(wrappedReq, wrappedRes);
    } finally {
      long ms = Duration.between(start, Instant.now()).toMillis();

      String method = request.getMethod();
      String path = request.getRequestURI();
      String query = request.getQueryString();
      String fullPath = (query == null) ? path : (path + "?" + query);

      int status = wrappedRes.getStatus();

      String reqBody = readRequestBody(wrappedReq);
      String resBody = readResponseBody(wrappedRes);

      // Avoid logging huge bodies; also don’t dump actuator env, etc.
      if (fullPath.startsWith("/actuator")) {
        log.info("HTTP {} {} -> {} ({}ms)", method, fullPath, status, ms);
      } else {
        // Log headers selectively (don’t log Authorization)
        String contentType = request.getHeader(HttpHeaders.CONTENT_TYPE);
        String userAgent = request.getHeader(HttpHeaders.USER_AGENT);

        if (status >= 400) {
          log.warn("HTTP {} {} -> {} ({}ms) ct={} ua={} reqBody={} resBody={}",
              method, fullPath, status, ms, safe(contentType), safe(userAgent), reqBody, resBody);
        } else {
          log.info("HTTP {} {} -> {} ({}ms) ct={} ua={} reqBody={}",
              method, fullPath, status, ms, safe(contentType), safe(userAgent), reqBody);
        }
      }

      wrappedRes.copyBodyToResponse();
      MDC.remove("traceId");
    }
  }

  private String readRequestBody(ContentCachingRequestWrapper req) {
    byte[] buf = req.getContentAsByteArray();
    if (buf == null || buf.length == 0) return "";
    String s = new String(buf, StandardCharsets.UTF_8);
    return truncate(s);
  }

  private String readResponseBody(ContentCachingResponseWrapper res) {
    try {
      byte[] buf = res.getContentAsByteArray();
      if (buf == null || buf.length == 0) return "";
      String s = new String(buf, StandardCharsets.UTF_8);
      return truncate(s);
    } catch (Exception e) {
      return "";
    }
  }

  private String truncate(String s) {
    if (s == null) return "";
    s = s.replaceAll("\\s+", " ").trim();
    if (s.length() <= MAX_BODY_LOG_CHARS) return s;
    return s.substring(0, MAX_BODY_LOG_CHARS) + "...(truncated)";
  }

  private String safe(String s) {
    return s == null ? "" : s;
  }
}
