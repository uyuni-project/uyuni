<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://struts.apache.org/tags-html"
           prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-bean"
           prefix="bean"%>


<html>
    <body>
        <rhn:toolbar base="h1" icon="header-configuration"
                     iconAlt="config.common.globalAlt"
                     helpUrl="/docs/reference/configuration/config-channels.html">
            <bean:message key="channelOverview.jsp.newToolbar" />
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
