package com.mst.service;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

@Service
public class RequiredRoleService {

    public String getRequiredRole(String path, HttpMethod method) {
        if (path.startsWith("/api/user/") || path.startsWith("/api/role/")) {
            return "all";
        }

        if (path.equals("/api/loader/scan")) {
            return "triggerScan";
        }

        if (path.startsWith("/api/action/")) {
            if (path.matches("/api/action/process/\\d+")) return "triggerProcess";
            if (path.matches("/api/action/restore/\\d+")) return "updateAction";
            if (method == HttpMethod.POST) return "createAction";
            if (method == HttpMethod.PUT) return "updateAction";
            if (method == HttpMethod.DELETE) return "deleteAction";
        }

        if (path.startsWith("/api/metric/")) {
            if (method == HttpMethod.POST) return "createMetric";
            if (method == HttpMethod.PUT) return "updateMetric";
            if (method == HttpMethod.DELETE) return "deleteMetric";
        }

        if (path.startsWith("/api/processor/")) {
            return "triggerProcess";
        }

        if (path.startsWith("/api/evaluation/")) {
            return "triggerEvaluation";
        }

        if (path.startsWith("/api/logger/") && method != HttpMethod.GET) {
            return "all";
        }

        if (path.startsWith("/api/email/") || path.startsWith("/api/sms/")) {
            return "all";
        }

        return "read";
    }
}
