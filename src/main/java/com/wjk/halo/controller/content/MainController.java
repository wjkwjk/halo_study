package com.wjk.halo.controller.content;

import com.wjk.halo.config.properties.HaloProperties;
import com.wjk.halo.exception.ServiceException;
import com.wjk.halo.model.entity.User;
import com.wjk.halo.model.properties.BlogProperties;
import com.wjk.halo.model.support.HaloConst;
import com.wjk.halo.service.OptionService;
import com.wjk.halo.service.UserService;
import com.wjk.halo.utils.HaloUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class MainController {

    /**
     * 重定向URI
     */
    private final static String INDEX_REDIRECT_URI = "index.html";

    /**
     * 安装重定向URI
     */
    private final static String INSTALL_REDIRECT_URI = INDEX_REDIRECT_URI + "#install";

    private final UserService userService;

    private final OptionService optionService;

    private HaloProperties haloProperties;

    public MainController(UserService userService, OptionService optionService, HaloProperties haloProperties) {
        this.userService = userService;
        this.optionService = optionService;
        this.haloProperties = haloProperties;
    }

    @GetMapping("${halo.admin-path:admin}")
    public void admin(HttpServletResponse response) throws IOException{
        String adminIndexRedirectUri = HaloUtils.ensureBoth(haloProperties.getAdminPath(), HaloUtils.URL_SEPARATOR) + INDEX_REDIRECT_URI;
        response.sendRedirect(adminIndexRedirectUri);
    }

    @GetMapping("version")
    @ResponseBody
    public String version(){
        return HaloConst.HALO_VERSION;
    }

    @GetMapping("install")
    public void installation(HttpServletResponse response) throws IOException{
        String installRedirectUri = StringUtils.appendIfMissing(this.haloProperties.getAdminPath(), "/") + INSTALL_REDIRECT_URI;
        response.sendRedirect(installRedirectUri);
    }

    @GetMapping("avatar")
    public void avatar(HttpServletResponse response) throws IOException{
        User user = userService.getCurrentUser().orElseThrow(() -> new ServiceException("未查询到博主信息"));
        if (StringUtils.isNotEmpty(user.getAvatar())){
            response.sendRedirect(HaloUtils.normalizeUrl(user.getAvatar()));
        }
    }

    @GetMapping("logo")
    public void logo(HttpServletResponse response) throws IOException{
        String blogLogo = optionService.getByProperty(BlogProperties.BLOG_LOGO).orElse("").toString();
        if (StringUtils.isNotEmpty(blogLogo)){
            response.sendRedirect(HaloUtils.normalizeUrl(blogLogo));
        }
    }

    @GetMapping("favicon.ico")
    public void favicon(HttpServletResponse response) throws IOException{
        String favicon = optionService.getByProperty(BlogProperties.BLOG_FAVICON).orElse("").toString();
        if (StringUtils.isNotEmpty(favicon)){
            response.sendRedirect(HaloUtils.normalizeUrl(favicon));
        }
    }

}
