package com.example.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.example.annotation.RequireModule;
import com.example.config.ModuleRoleMapping;
import com.example.entity.RestBean;
import com.example.entity.user.Admin;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;

        RequireModule annotation = handlerMethod.getMethodAnnotation(RequireModule.class);
        if (annotation == null) {
            annotation = handlerMethod.getBeanType().getAnnotation(RequireModule.class);
        }

        Admin admin = (Admin) request.getSession().getAttribute("admin");
        if (admin == null || admin.getRole() == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().write(JSONObject.toJSONString(RestBean.failure(401, "请重新登录管理员账户")));
            return false;
        }

        if (annotation == null) {
            return true;
        }

        String requiredModule = annotation.value();
        if (!ModuleRoleMapping.hasAccess(requiredModule, admin.getRole())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().write(JSONObject.toJSONString(RestBean.failure(403, "权限不足，无法访问该模块")));
            return false;
        }

        return true;
    }
}
