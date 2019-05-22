<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://rhn.redhat.com/tags/list" prefix="rl"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://rhn.redhat.com/tags/list" prefix="rl"%>

<html>
<head>
<meta name="page-decorator" content="none" />
</head>
<body>
    <rhn:toolbar base="h1" icon="system-bare-metal" imgAlt="system.common.systemAlt"
        helpUrl="/docs/reference/systems/systems-list.html#ref.webui.systems.systems.baremetal">
        <bean:message key="bootstrapsystemlist.jsp.header" />
    </rhn:toolbar>

    <rl:listset name="bareMetalListSet" legend="system">
        <rhn:csrf />
        <rhn:submitted />

        <c:choose>
            <c:when test="${empty notSelectable}">
                <c:set var="namestyle" value="" />
            </c:when>
            <c:otherwise>
                <c:set var="namestyle" value="first-column" />
            </c:otherwise>
        </c:choose>

        <rl:list dataset="pageList" name="systemList" emptykey="nosystems.message"
            alphabarcolumn="name"
            filter="com.redhat.rhn.frontend.taglibs.list.filters.SystemOverviewFilter">

            <rl:decorator name="ElaborationDecorator" />
            <rl:decorator name="SystemIconDecorator" />
            <c:if test="${empty noAddToSsm}">
                <rl:decorator name="AddToSsmDecorator"/>
            </c:if>
            <rl:decorator name="PageSizeDecorator" />

            <c:if test="${empty notSelectable}">
                <rl:decorator name="SelectableDecorator" />
                <rl:selectablecolumn value="${current.id}" selected="${current.selected}"
                    disabled="${not current.selectable}" />
            </c:if>

            <!-- Name Column -->
            <rl:column bound="false" headerkey="systemlist.jsp.system"
                sortable="true" sortattr="name" defaultsort="asc" styleclass="${namestyle}">
                <%@ include
                    file="/WEB-INF/pages/common/fragments/systems/system_list_fragment.jspf"%>
            </rl:column>

            <!-- Detected on Column -->
            <rl:column sortable="true" attr="lastCheckin" bound="true"
                headerkey="bootstrapsystemlist.jsp.detected_on" />

            <!-- Number of CPUs Column -->
            <rl:column attr="cpuCount" bound="true"
                headerkey="bootstrapsystemlist.jsp.cpu_count" />

            <!-- Clock frquency Column -->
            <rl:column bound="false" headerkey="bootstrapsystemlist.jsp.cpu_clock_frequency">
              <c:out value="${current.cpuClockFrequency}"></c:out> GHz
            </rl:column>

            <!-- RAM Column -->
            <rl:column bound="false" headerkey="bootstrapsystemlist.jsp.ram">
              <c:out value="${current.ram}"></c:out> MB
            </rl:column>

            <!-- Number of disks Column -->
            <rl:column attr="diskCount" bound="true"
                headerkey="bootstrapsystemlist.jsp.disk_count" />

            <!-- MAC Addresses Column -->
            <rl:column bound="false" headerkey="bootstrapsystemlist.jsp.macs">
              <c:forEach var="mac" items="${current.macs}">
                  <c:out value="${mac}"></c:out>
              </c:forEach>
            </rl:column>
        </rl:list>
        <c:if test="${empty noCsv}">
            <rl:csv dataset="pageList" name="systemList"
                exportColumns="name,id,lastCheckin,cpuCount,cpuClockFrequency,ram,diskCount,macs" />
        </c:if>
        <rhn:csrf />
        <rhn:submitted />
    </rl:listset>
</body>
</html>
