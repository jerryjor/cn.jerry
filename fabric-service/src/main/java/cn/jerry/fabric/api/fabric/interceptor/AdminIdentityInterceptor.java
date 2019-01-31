package cn.jerry.fabric.api.fabric.interceptor;

import cn.jerry.fabric.api.fabric.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AdminIdentityInterceptor extends HandlerInterceptorAdapter {
    private static Logger logger = LogManager.getLogger();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String user = request.getHeader("user");
        String secretSha1 = request.getHeader("secretSha1");
        String failMsg = null;
        boolean pass = false;
        try {
            if (user == null || (user = user.trim()).isEmpty()
                    || secretSha1 == null || (secretSha1 = secretSha1.trim()).isEmpty()) {
                failMsg = "no user or secretSha1 in header.";
                return false;
            }

            UserService userService = new UserService();
            if (userService.existsAdmin(user, secretSha1)) {
                pass = true;
                return true;
            }
            return false;
        } finally {
            if (!pass) {
                response.setStatus(403);
                response.getWriter().write(failMsg == null ? "identity verify failed." : failMsg);
            }
        }
    }

}
