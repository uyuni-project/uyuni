package com.suse.manager.webui.websocket;

import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.struts.RequestContext;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

public class WebsocketSessionConfigurator extends ServerEndpointConfig.Configurator
{
    @Override
    public void modifyHandshake(ServerEndpointConfig config,
                                HandshakeRequest request,
                                HandshakeResponse response) {
        HttpSession httpSession = (HttpSession)request.getHttpSession();
        HttpServletRequest sr = (HttpServletRequest) httpSession.getAttribute("__original_request__");
        User user = new RequestContext(sr).getCurrentUser();
        // force roles to be loaded so now since it will fail from the websocket thread
        user.getRoles();
        config.getUserProperties().put("currentUser", user);
    }
}
