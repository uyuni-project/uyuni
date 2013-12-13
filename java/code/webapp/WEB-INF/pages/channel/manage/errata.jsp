<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://rhn.redhat.com/tags/list" prefix="rl" %>


<html>
<head>
    <meta name="page-decorator" content="none" />
</head>
<body>
<%@ include file="/WEB-INF/pages/common/fragments/channel/manage/manage_channel_header.jspf" %>
     <h2>
      <rhn:icon type="header-errata" title="erratum"/> <bean:message key="header.jsp.errata"/>
    </h2>

<ul>
	<li> <a href="/rhn/channels/manage/errata/ListRemove.do?cid=${cid}"> <bean:message key="list.jsp.errata.listremove"/></a> </li>
	<li> <a href="/rhn/channels/manage/errata/Add.do?cid=${cid}"> <bean:message key="list.jsp.errata.add"/> </a> </li>
	<rhn:require acl="channel_is_clone()" mixins="com.redhat.rhn.common.security.acl.ChannelAclHandler">
		<li> <a href="/network/software/channels/manage/errata/clone.pxt?pxt:trap=rhn:empty_set&set_label=errata_clone_actions&cid=${cid}"> <bean:message key="list.jsp.errata.clone"/> </a> </li>
	</rhn:require>
</u>


</body>
</html>



