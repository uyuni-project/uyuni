<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn"%>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://rhn.redhat.com/tags/list" prefix="rl"%>

<html>
<body>
    <%@ include file="/WEB-INF/pages/common/fragments/systems/system-header.jspf"%>

    <rhn:toolbar base="h2" icon="header-package-add"
        helpUrl="/docs/reference/systems/system-details/sd-packages.html">
        <bean:message key="pkg.lock.header" />
    </rhn:toolbar>

    <p>
        <bean:message key="pkg.lock.summary" />
    </p>

    <rl:listset name="packageListSet">
        <rhn:csrf />
        <rl:list dataset="dataset" width="100%" name="packageList" styleclass="list"
            emptykey="packagelist.jsp.nopackages" alphabarcolumn="nvre">
            <rl:decorator name="PageSizeDecorator" />
            <rl:decorator name="ElaborationDecorator" />
            <rl:decorator name="SelectableDecorator" />
            <rl:selectablecolumn value="${current.selectionKey}"
                selected="${current.selected and empty current.pending}"
                disabled="${not current.selectable or not empty current.pending}" />

            <rl:column headerkey="packagelist.jsp.packagename" bound="false" sortattr="nvre"
                sortable="true" filterattr="nvre" styleclass="">
                <c:if test="${not empty current.pending}">
                    <i class="fa fa-clock-o"></i>&nbsp;
                    </c:if>
                <c:choose>
                    <c:when test="${not checkPackageId or not empty current.packageId}">
                        <a href="/rhn/software/packages/Details.do?sid=${param.sid}&amp;id_combo=${current.idCombo}">${current.nvre}</a>
                    </c:when>
                    <c:otherwise>
                        <c:out value="${current.nvre}" />
                    </c:otherwise>
                </c:choose>
                <c:if test="${not empty current.pending}">
                    <span class="label label-info"> <c:choose>
                            <c:when test="${current.pending == 'L'}">
                                <bean:message key='pkg.lock.locking' />
                            </c:when>
                            <c:otherwise>
                                <bean:message key='pkg.lock.unlocking' />
                            </c:otherwise>
                        </c:choose>
                    </span>
                </c:if>
                <c:if test="${not empty current.locked && empty current.pending}">
                    <i class="fa fa-lock"></i>
                </c:if>
            </rl:column>
            <rl:column headerkey="packagelist.jsp.packagearch" bound="false"
                styleclass="thin-column last-column">
                <c:choose>
                    <c:when test="${not empty current.arch}">${current.arch}</c:when>
                    <c:otherwise>
                        <bean:message key="packagelist.jsp.notspecified" />
                    </c:otherwise>
                </c:choose>
            </rl:column>
        </rl:list>

        <div class="form-horizontal">
            <div class="form-group">
                <label class="col-md-3 control-label" for="radio_use_date_now"> <bean:message
                        key="schedule.jsp.at" />:
                </label>
                <div class="col-md-9">
                    <jsp:include page="/WEB-INF/pages/common/fragments/date-picker.jsp">
                        <jsp:param name="widget" value="date" />
                    </jsp:include>
                </div>
            </div>
            <div class="form-group">
                <div class="col-md-offset-3 col-md-9">
                    <rhn:submitted />
                    <input type="submit" class="btn btn-success" name="dispatch"
                        value='<bean:message key="pkg.lock.requestlock"/>' /> <input
                        type="submit" class="btn btn-success" name="dispatch"
                        value='<bean:message key="pkg.lock.requestunlock"/>' />
                </div>
            </div>
        </div>
    </rl:listset>
</body>
</html>
