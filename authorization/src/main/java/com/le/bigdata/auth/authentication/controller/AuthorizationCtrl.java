package com.le.bigdata.auth.authentication.controller;

import com.le.bigdata.auth.authentication.AuthorizationHandler;
import com.le.bigdata.auth.token.IAuthTokenProvider;
import com.le.bigdata.core.dto.CommonResponseDTO;
import com.mangofactory.swagger.annotations.ApiIgnore;
import com.wordnik.swagger.annotations.ApiImplicitParam;
import com.wordnik.swagger.annotations.ApiImplicitParams;
import com.wordnik.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import static com.le.bigdata.core.Constant.ACCESS_TOKEN;

/**
 * 登录权限相关的ctrl
 * Created by benjamin on 2016/12/14.
 */
@Controller
public class AuthorizationCtrl {

    private List<AuthorizationHandler> authorizationHandlers = new ArrayList<AuthorizationHandler>();

    @Resource(name = "redisTokenProvider")
    private IAuthTokenProvider tokenProvider;

    @Resource(name = "passwordAuthHandler")
    private AuthorizationHandler password;

    @Resource(name = "authorizationCodeHandler")
    private AuthorizationHandler authorizationCode;

    @PostConstruct
    public void init() {
        authorizationHandlers.add(password);
        authorizationHandlers.add(authorizationCode);
    }

    public IAuthTokenProvider getTokenProvider() {
        return tokenProvider;
    }

    public void setTokenProvider(IAuthTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @RequestMapping(method = RequestMethod.POST, value = "authorize")
    @ApiOperation(value = "登录接口(OAuth2.0认证 实现了 grant_type=password 和 grant_type=code的认证方式)",
    notes = "username是必传参数, 理论上来讲可以灵活的传递其他参数, 但默认的只需要传递 username, password, grant_type即可")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username", dataType = "string",value = "用户名", paramType = "form"),
            @ApiImplicitParam(name = "password", dataType = "string", value = "密码", paramType = "form"),
            @ApiImplicitParam(name = "grant_type", dataType = "string", value = "用户名", defaultValue = "password", allowableValues = "password", paramType = "form")
    })
    public void authorize(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            for (AuthorizationHandler authorizationHandler : authorizationHandlers) {
                authorizationHandler.handleAuthorization(request, response);
            }
        } catch (Exception e) {
            response.setStatus(500);
            response.setCharacterEncoding(request.getCharacterEncoding());
            PrintWriter out = response.getWriter();
            e.printStackTrace(out);
            out.flush();
            out.close();
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "revoke_token")
    @ApiOperation("登出接口")
    @ResponseBody
    public CommonResponseDTO revokeToken(@ApiIgnore @CookieValue(ACCESS_TOKEN) String token) throws IOException {
        tokenProvider.deleteToken(token);
        return new CommonResponseDTO(200);
    }
}
