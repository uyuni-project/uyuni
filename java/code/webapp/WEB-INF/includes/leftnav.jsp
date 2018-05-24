<%@page import="com.redhat.rhn.common.conf.Config"%>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ page import="com.suse.manager.webui.menu.MenuTree" %>

<script type="text/javascript">
  const JSONMenu = <%= MenuTree.getJsonMenu(pageContext) %>;
  const _IS_UYUNI = <%= Config.get().getString("product_name").compareToIgnoreCase("Uyuni") == 0 %>;
</script>
