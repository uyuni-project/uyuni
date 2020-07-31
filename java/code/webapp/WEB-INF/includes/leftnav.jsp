<%@page import="com.redhat.rhn.common.conf.Config"%>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ page import="com.redhat.rhn.GlobalInstanceHolder" %>

<div id="left-menu-data">
    <script type="text/javascript">
        window.JSONMenu = <%= GlobalInstanceHolder.MENU_TREE.getJsonMenu(pageContext) %>;
        window._IS_UYUNI = <%= Config.get().getString("product_name").compareToIgnoreCase("Uyuni") == 0 %>;
    </script>
</div>
