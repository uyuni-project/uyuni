<%@ taglib uri="jakarta.tags.core" prefix="c"%>
<%@ taglib uri="jakarta.tags.functions" prefix="fn"%>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn"%>
<%@ taglib uri="http://rhn.redhat.com/tags/list" prefix="rl"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>

<html:xhtml />
<html>
<body>
    <%@ include file="/WEB-INF/pages/common/fragments/ssm/header.jspf"%>

    <rhn:toolbar base="h2" icon="header-power"
        helpUrl="/docs/${rhn:getDocsLocale(pageContext)}/reference/systems/ssm-overview.html">
        <bean:message key="ssm.provisioning.powermanagement.configuration.header" />
    </rhn:toolbar>
    <div class="page-summary">
        <p>
            <bean:message key="ssm.provisioning.powermanagement.configuration.summary" />
        </p>
    </div>


    <c:if test="${fn:length(types) >= 1}">
        <%@ include file="/WEB-INF/pages/common/fragments/ssm/system_list.jspf"%>

        <html:form styleClass="form-horizontal"
            action="/systems/ssm/provisioning/PowerManagementConfiguration.do">

            <%@ include file="/WEB-INF/pages/common/fragments/kickstart/powermanagement-options.jspf"%>

            <div class="form-group">
                <div class="col-md-offset-3 offset-md-3 col-md-6">
                    <button type="submit" name="dispatch" class="btn btn-primary"
                        value='<bean:message key="ssm.provisioning.powermanagement.configuration.update" />'>
                        <bean:message key="ssm.provisioning.powermanagement.configuration.update" />
                    </button>
                </div>
            </div>
        </html:form>
    </c:if>
</body>
</html>
