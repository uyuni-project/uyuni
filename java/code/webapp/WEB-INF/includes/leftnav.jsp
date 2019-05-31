<%@page import="com.redhat.rhn.common.conf.Config"%>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ page import="com.suse.manager.webui.menu.MenuTree" %>

<div id="left-menu-data">
    <script type="text/javascript">
        window.JSONMenu = <%= MenuTree.getJsonMenu(pageContext) %>;
        window._IS_UYUNI = <%= Config.get().getString("product_name").compareToIgnoreCase("Uyuni") == 0 %>;
    </script>
</div>
