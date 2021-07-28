package com.wjk.halo.security.handler;

import com.wjk.halo.exception.AbstractHaloException;
import com.wjk.halo.exception.NotInstallException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ContentAuthenticationFailureHandler implements AuthenticationFailureHandler {
    @Override
    public void onFailure(HttpServletRequest request, HttpServletResponse response, AbstractHaloException exception) throws IOException, ServletException {
        if (exception instanceof NotInstallException){
            response.sendRedirect(request.getContextPath() + "/install");
            return;
        }

        request.getRequestDispatcher(request.getContextPath() + "/error").forward(request, response);

    }
}
