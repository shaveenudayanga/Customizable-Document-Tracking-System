package com.docutrace.user_service.tenant;

/**
 * Simple ThreadLocal holder for per-request tenant identifier.
 * Phase 1: best-effort only; enforcement happens later.
 */
public final class TenantContext {
    private static final ThreadLocal<String> CURRENT = new ThreadLocal<>();

    private TenantContext() {}

    public static void setTenant(String tenant) { CURRENT.set(tenant); }

    public static String getTenant() { return CURRENT.get(); }

    public static void clear() { CURRENT.remove(); }
}
