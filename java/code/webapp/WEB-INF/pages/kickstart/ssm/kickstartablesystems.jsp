<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://rhn.redhat.com/tags/list" prefix="rl" %>

<html>
<head>
</head>
<body>
<%@ include file="/WEB-INF/pages/common/fragments/ssm/header.jspf" %>
<h2><bean:message key="ssm.kickstartable-systems.jsp.title"/></h2>
  <div class="page-summary">
    <p>
    <bean:message key="ssm.kickstartable-systems.jsp.summary"/>
    </p>
  </div>

<rl:listset name="systemListSet" legend="system">
    <rhn:csrf />
        <rl:list
                emptykey="nosystems.message"
                alphabarcolumn="name"
                filter="com.redhat.rhn.frontend.taglibs.list.filters.SystemOverviewFilter"
                >
            <rl:decorator name="ElaborationDecorator"/>
                <rl:decorator name="PageSizeDecorator"/>

                <rl:column sortable="true"
                                   bound="false"
                           headerkey="systemlist.jsp.system"
                           sortattr="name"
                           defaultsort="asc">
                        <%@ include file="/WEB-INF/pages/common/fragments/systems/system_list_fragment.jspf" %>
                </rl:column>

                <!-- Base Channel Column -->
                <rl:column sortable="false"
                                   bound="false"
                           headerkey="systemlist.jsp.channel"
 >
           <%@ include file="/WEB-INF/pages/common/fragments/channel/channel_list_fragment.jspf" %>
                </rl:column>
        </rl:list>

<c:if test="${empty disableSystems}">
    <h2>
        <bean:message key="ssm.kickstartable-systems.jsp.systems"/>
    </h2>
    <p><bean:message key="ssm.kickstartable-systems.jsp.systems.summary"/></p>
    <hr/>
    <div class="form-group">
        <div class="control-label">
            <bean:message key="ssm.kickstartable-systems.jsp.type"/>:
        </div>
            <div class="radio">
            <label class="radio control-label" for="manualId">
                <input type="radio" name="scheduleManual" value="true" id="manualId"
                    <c:if test="${not empty disableProfiles}">disabled="true"</c:if>
                    <c:if test="${empty param.scheduleManual or param.scheduleManual =='true' }">checked="checked" </c:if> />
                <bean:message key="ssm.kickstartable-systems.jsp.manual-summary"/>
            </label>
            </div>
            <div class="radio">
            <label class="radio control-label" for=ipId"">
                <input type="radio" name="scheduleManual" value="false" id="ipId"
                    <c:if test="${not empty disableProfiles or not empty disableRanges}">disabled="true"</c:if>
                    <c:if test="${param.scheduleManual =='false'}">checked="checked" </c:if> />
                <bean:message key="ssm.kickstartable-systems.jsp.ip-summary"/>*
            </label>
            </div>
        <p class="help-block">* <bean:message key="ssm.kickstartable-systems.jsp.ip-tooltip"/></p>
    </div>
    <div class="form-group text-right">
        <input class="btn btn-default" type="submit" name="dispatch" value="${rhn:localize('ssm.config.subscribe.jsp.continue')}"
                    <c:if test="${not empty disableProfiles}">disabled="true"</c:if> />
    </div>
</c:if>
<rhn:submitted/>
</rl:listset>

</body>
</html>
