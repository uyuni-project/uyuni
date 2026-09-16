<%--
SPDX-FileCopyrightText: Red Hat, Inc
SPDX-FileCopyrightText: SUSE LLC

SPDX-License-Identifier: GPL-2.0-only
--%>

<%@page import="com.redhat.rhn.common.conf.Config"%>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ page import="com.redhat.rhn.GlobalInstanceHolder" %>

<div id="left-menu-data">
    <script type="text/javascript">
        window.JSONMenu = <%= GlobalInstanceHolder.MENU_TREE.getJsonMenu(pageContext) %>;
    </script>
</div>
