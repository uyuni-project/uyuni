<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://rhn.redhat.com/tags/list" prefix="rl" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>

<html>
<head></head>
<body>

    <rhn:toolbar base="h1" icon="header-errata" iconAlt="errata.common.errataAlt"
                 helpUrl="">
        <bean:message key="errata.edit.toolbar"/>
        <c:out value="${advisory}"/>
    </rhn:toolbar>

    <rhn:dialogmenu mindepth="0" maxdepth="1" definition="/WEB-INF/nav/manage_errata.xml"
                    renderer="com.redhat.rhn.frontend.nav.DialognavRenderer"/>

    <h2>
        <bean:message key="errata.edit.packages.addpackages"/>
    </h2>

    <p>
        <bean:message key="errata.edit.packages.add.instructions"/>
    </p>

    <rl:listset name="groupSet">
        <rhn:csrf/>
        <rhn:submitted/>

        <div class="form-horizontal">
            <div class="form-group">
                <label class="col-lg-3 control-label">
                    <bean:message key="channel.jsp.manage.package.channel"/>:
                </label>
                <div class="col-lg-6">
                    <div class="input-group">
                        <select class="form-control" name="view_channel">
                            <c:forEach items="${viewoptions}" var="option">
                                <option value="<c:out value="${option.value}"/>" ${option.value == param.view_channel ? 'selected="1"' : ''}>
                                    <c:out value="${option.label}"/>
                                </option>
                            </c:forEach>
                        </select>
                        <span class="input-group-btn">
                            <input class="btn btn-default" type="submit" name="view_clicked" value="View Packages"/>
                        </span>
                    </div>
                </div>
            </div>
        </div>

        <rhn:hidden name="eid" value="${param.eid}"/>

        <div class="spacewalk-section-toolbar">
            <div class="action-button-wrapper">
                <button type="submit" name="dispatch" class="btn btn-primary" value="<bean:message key='errata.edit.packages.add.addpackages'/>">
                    <bean:message key="errata.edit.packages.add.addpackages"/>
                </button>
            </div>
        </div>

        <rl:list dataset="pageList"
                 width="100%"
                 styleclass="list"
                 emptykey="packagelist.jsp.nopackages">

            <rl:decorator name="PageSizeDecorator"/>
            <rl:decorator name="SelectableDecorator"/>
            <rl:decorator name="ElaborationDecorator"/>

            <rl:selectablecolumn value="${current.selectionKey}"
                                 selected="${current.selected}"
                                 disabled="${not current.selectable}"/>

            <rl:column headerkey="errata.edit.packages.add.package" bound="false"
                       sortattr="packageNvre" sortable="true" filterattr="packageNvre">
                <a href="/rhn/software/packages/Details.do?pid=${current.id}">
                    <c:out value="${current.packageNvre}" escapeXml="false"/>
                </a>
            </rl:column>

            <rl:column headerkey="errata.edit.packages.add.channels" bound="false">
                <c:choose>
                    <c:when test="${current.packageChannels != null}">
                        <c:forEach items="${current.packageChannels}" var="channel">
                            <c:out value="${channel}"/>
                            <br/>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        (none)
                    </c:otherwise>
                </c:choose>
            </rl:column>
        </rl:list>
    </rl:listset>

</body>
</html>
