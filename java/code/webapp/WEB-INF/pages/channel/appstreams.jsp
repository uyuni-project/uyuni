<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://rhn.redhat.com/tags/list" prefix="rl" %>


<html>
<head>
</head>
<body>

 <%@ include file="/WEB-INF/pages/common/fragments/channel/appstream_header.jspf" %>
  <h2>
      <rhn:icon type="header-channel-configuration" />
      <bean:message key="appstream.jsp.headerappstream"/>
  </h2>

<rl:listset name="systemSet" legend="system-group">
<rhn:csrf />

<rhn:hidden name="cid" value="${cid}" />

        <rl:list dataset="pageList"
                        name="moduleList"
                        emptykey="appstream.jsp.nomodules"
                        filter="com.redhat.rhn.frontend.action.channel.AppStreamFilter" >

                        <rl:decorator name="PageSizeDecorator"/>

                 <rl:column sortable="true"
                                   bound="false"
                           headerkey="appstream.jsp.modulename"
                           sortattr="key"
                           defaultsort="asc"
                           >
                        <a href="/rhn/channels/software/Search.do?search_string=${current.key}&view_mode=search_free_form&whereCriteria=channel&channel_filter=${cid}&fineGrained=on" >
                        	<c:out value="${current.key}" />
                        </a>
                </rl:column>

                 <rl:column sortable="false"
                                   bound="false"
                           headerkey="appstream.jsp.streamname"
                          >
                        <c:forEach var="stream"  items="${current.value.streams}" >
                            <c:if test="${stream == current.value.defaultStream}">
                                <span class="label label-primary">
                                <c:out value="${stream}"/>
                                </span>
                                &nbsp;
                            </c:if>
                            <c:if test="${stream != current.value.defaultStream}">
                                <span class="label label-info">
                                <c:out value="${stream}"/>
                                </span>
                                &nbsp;
                            </c:if>
                        </c:forEach>
                </rl:column>
        </rl:list>
</rl:listset>
</body>
</html>
