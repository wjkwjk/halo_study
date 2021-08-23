package com.wjk.halo.security.handler;

import com.wjk.halo.exception.AbstractHaloException;
import com.wjk.halo.exception.NotInstallException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 定义错误处理事件，当有错误发生时（管理员权限验证失败），调用该方法
 */

public class ContentAuthenticationFailureHandler implements AuthenticationFailureHandler {
    @Override
    public void onFailure(HttpServletRequest request, HttpServletResponse response, AbstractHaloException exception) throws IOException, ServletException {
        /**
         * 如果是因为 未安装异常 触发的该事件，则重定向到安装界面
         * response.sendRedirect：前后是两个request。通过向客户端发发送一个特殊的消息，使得它重新再取指定页面发送请求
         */
        if (exception instanceof NotInstallException){
            response.sendRedirect(request.getContextPath() + "/install");
            return;
        }

        /**
         * 请求转发。
         * 在服务器内部进行，对客户端是透明，前后共享一个request请求，具体可以分为请求转发和请求包含
         * 请求转发（forward） : 由下一个Servlet完成响应体，当前Servlet可以设置响应头（留头不留体）。
         *      举个例子，AServlet请求转发到BServlet，那么AServlet不能够使用response.getWriter（） 和response.getOutputStream（）向客户端输出响应体，但可以使用response.setContentType("text/html;charset=utf-8") 设置响应头。而在BServlet中可以输出响应体。
         * 请求包含（include）:由两个Servlet共同完成响应体（留头又留体）。
         *      同样用上面的例子，AServlet请求包含到BServlet，那么AServlet既可以设置响应头，也可以完成响应体。
         */
        /**
         * 在这里将请求转发到专门处理错误的接口
         */
        request.getRequestDispatcher(request.getContextPath() + "/error").forward(request, response);

    }
}
