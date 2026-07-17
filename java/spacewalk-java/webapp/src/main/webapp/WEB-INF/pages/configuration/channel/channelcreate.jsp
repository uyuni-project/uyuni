<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn"%>
<%@ taglib uri="jakarta.tags.core" prefix="c"%>
<%@ taglib uri="http://struts.apache.org/tags-html"
           prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-bean"
           prefix="bean"%>


<html>
    <body>
        <rhn:toolbar base="h1" icon="header-configuration"
                     iconAlt="config.common.globalAlt"
                     helpUrl="/docs/${rhn:getDocsLocale(pageContext)}/reference/configuration/config-channels.html">
            <c:choose>
                <c:when test="${param.type == 'state'}">
                    <bean:message key="channelOverview.jsp.newToolbar.stateType" />
                </c:when>
                <c:otherwise>
                    <bean:message key="channelOverview.jsp.newToolbar" />
                </c:otherwise>
            </c:choose>
        </rhn:toolbar>

        <p><bean:message key="channelOverview.jsp.create-instruction" /></p>
        <html:form action="/configuration/ChannelCreate" styleClass="form-horizontal">
            <rhn:csrf />
            <html:hidden property="creating" value="true"/>
            <html:hidden property="submitted" value="true"/>
             <html:hidden property="type" value="${param.type}"/>
            <%@ include file="/WEB-INF/pages/common/fragments/configuration/channel/propertybody.jspf"%>
        </html:form>
    </body>
</html>
