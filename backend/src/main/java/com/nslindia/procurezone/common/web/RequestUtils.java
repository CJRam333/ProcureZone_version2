package com.nslindia.procurezone.common.web;

import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Utility class for extracting client information from HTTP requests.
 */
public final class RequestUtils {

    private RequestUtils() {
    }

    /**
     * Get the current HTTP request from the request context.
     * 
     * @return the current HTTP request, or null if not in a request context
     */
    @Nullable
    public static HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    /**
     * Extract the client's IP address from the HTTP request.
     * Checks various headers used by proxies and load balancers.
     * 
     * @param request the HTTP request
     * @return the client's IP address, or "unknown" if not available
     */
    public static String getClientIpAddress(@Nullable HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }

        // Check various headers used by proxies
        String[] headerNames = {
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR",
                "HTTP_X_FORWARDED",
                "HTTP_X_CLUSTER_CLIENT_IP",
                "HTTP_CLIENT_IP",
                "HTTP_FORWARDED_FOR",
                "HTTP_FORWARDED",
                "HTTP_VIA",
                "REMOTE_ADDR"
        };

        for (String header : headerNames) {
            String ip = request.getHeader(header);
            if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For can contain multiple IPs, take the first one
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }

        // Fall back to remote address
        String remoteAddr = request.getRemoteAddr();
        return StringUtils.hasText(remoteAddr) ? remoteAddr : "unknown";
    }

    /**
     * Get the client's IP address from the current request context.
     * 
     * @return the client's IP address, or "unknown" if not available
     */
    public static String getCurrentClientIpAddress() {
        return getClientIpAddress(getCurrentRequest());
    }
}
