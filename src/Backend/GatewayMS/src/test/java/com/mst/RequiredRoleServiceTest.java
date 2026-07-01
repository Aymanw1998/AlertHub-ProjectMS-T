package com.mst;

import com.mst.service.RequiredRoleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = RequiredRoleService.class)
class RequiredRoleServiceTest {

    @Autowired
    private RequiredRoleService requiredRoleService;

    @Test
    void getRequiredRole_forUserOrRoleRoutes_returnsAdmin() {
        assertEquals("all", requiredRoleService.getRequiredRole("/api/user/1", HttpMethod.GET));
        assertEquals("all", requiredRoleService.getRequiredRole("/api/role/get-all", HttpMethod.GET));
    }

    @Test
    void getRequiredRole_forLoaderScan_returnsTriggerScan() {
        assertEquals("triggerScan", requiredRoleService.getRequiredRole("/api/loader/scan", HttpMethod.GET));
    }

    @Test
    void getRequiredRole_forActionRoutes_returnsOperationSpecificRoles() {
        assertEquals("createAction", requiredRoleService.getRequiredRole("/api/action/create", HttpMethod.POST));
        assertEquals("updateAction", requiredRoleService.getRequiredRole("/api/action/1", HttpMethod.PUT));
        assertEquals("deleteAction", requiredRoleService.getRequiredRole("/api/action/1", HttpMethod.DELETE));
        assertEquals("triggerProcess", requiredRoleService.getRequiredRole("/api/action/process/5", HttpMethod.GET));
    }

    @Test
    void getRequiredRole_forMetricRoutes_returnsOperationSpecificRoles() {
        assertEquals("createMetric", requiredRoleService.getRequiredRole("/api/metric/create", HttpMethod.POST));
        assertEquals("updateMetric", requiredRoleService.getRequiredRole("/api/metric/1", HttpMethod.PUT));
        assertEquals("deleteMetric", requiredRoleService.getRequiredRole("/api/metric/1", HttpMethod.DELETE));
    }

    @Test
    void getRequiredRole_forNotificationRoutes_returnsAdmin() {
        assertEquals("all", requiredRoleService.getRequiredRole("/api/email/send", HttpMethod.POST));
        assertEquals("all", requiredRoleService.getRequiredRole("/api/sms/send", HttpMethod.POST));
    }

    @Test
    void getRequiredRole_forUnknownGetRoute_returnsRead() {
        assertEquals("read", requiredRoleService.getRequiredRole("/api/loader/get-all", HttpMethod.GET));
    }
}
