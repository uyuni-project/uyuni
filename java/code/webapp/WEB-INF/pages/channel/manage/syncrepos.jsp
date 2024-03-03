<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://rhn.redhat.com/tags/list" prefix="rl" %>

<html>
<head></head>
<body>
    <%@ include file="/WEB-INF/pages/common/fragments/channel/manage/manage_channel_header.jspf" %>

    <h2><rhn:icon type="header-package" /> <bean:message key="repos.jsp.channel.repos"/></h2>

    <c:if test='${not empty last_sync}'>
        <div class="panel panel-default">
            <div class="panel-heading">
                <h4><bean:message key="channel.edit.jsp.lastsynced"/></h4>
            </div>
            <div class="panel-body">
                <c:if test='${not empty log_url}'>
                    <a class="btn btn-info" href='${log_url}' target="_blank"><c:out value='${last_sync}'/></a>
                </c:if>
                <c:if test='${empty log_url}'>
                    <c:out value='${last_sync}'/>
                </c:if>
            </div>
        </div>
    </c:if>

    <rl:listset name="packageSet">
        <rhn:csrf />
        <rhn:submitted/>
        <rhn:hidden name="cid" value="${cid}" />

        <div class="spacewalk-section-toolbar">
            <div class="action-button-wrapper">
                <button type="submit" name="dispatch" value="<bean:message key='repos.jsp.button-sync'/>"
                        class="btn btn-success" ${in_progress || inactive ? 'disabled' : ''}>
                    <rhn:icon type="repo-sync"/>
                    <bean:message key='repos.jsp.button-sync'/>
                </button>
                <button type="submit" name="dispatch" value="<bean:message key='repos.jsp.button-save'/>"
                        class="btn btn-success" ${in_progress || inactive ? 'disabled' : ''}>
                    <rhn:icon type="repo-sync"/>
                    <bean:message key='repos.jsp.button-save'/>
                </button>
            </div>
        </div>

        <rl:list emptykey="repos.jsp.channel.norepos" alphabarcolumn="label">

            <rl:decorator name="PageSizeDecorator"/>


            <rl:column sortable="true"
                       bound="false"
                       headerkey="repos.jsp.channel.header"
                       sortattr="label"
                       defaultsort="asc">

                <a href="/rhn/channels/manage/repos/RepoEdit.do?id=${current.id}">${current.label}</a>
            </rl:column>

            <rl:column sortable="false"
                       bound="false"
                       headerkey="repos.jsp.channel.status">
                <c:set var="repoKey" value="${current.sourceUrl}"/>
                <c:set var="progress" value="${status[repoKey]['progress']}"/>
                <c:set var="title" value="${status[repoKey]['title']}"/>

                <c:if test="${status[repoKey]['finished']}">
                    <c:set var="barStyle" value="progress-bar-success"/>
                </c:if>

                <c:if test="${status[repoKey]['failed']}">
                    <c:set var="barStyle" value="progress-bar-danger"/>
                </c:if>

                <div class="progress progress-sm" title="${title}">
                    <div class="progress-bar ${barStyle}" role="progressbar" aria-valuenow="${progress}" aria-valuemin="0" aria-valuemax="100" style="width: ${progress}%;"></div>
                </div>
            </rl:column>

        </rl:list>

        <div class="checkbox">
            <label>
                <input type="checkbox" name="no-strict" id="no-strict"  <c:if test='${noStrict}'> checked</c:if> />
                <bean:message key="channel.manage.sync.nostrict.jsp"/>
            </label>
        </div>
        
        <div class="checkbox">
            <label>
                <input type="checkbox" name="no-errata" id="no-errata" <c:if test='${noErrata}'> checked</c:if>/>
                <bean:message key="channel.manage.sync.noerrata.jsp"/>
            </label>
        </div>
        
        <div class="checkbox">
            <label>
                <input type="checkbox" name="latest" id="latest" <c:if test='${latest}'> checked</c:if>/>
                <bean:message key="channel.manage.sync.latestonly.jsp"/>
            </label>
        </div>
        
        <div class="checkbox">
            <label>
                <input type="checkbox" name="sync-kickstart" id="sync-kickstart" <c:if test='${syncKickstart}'> checked</c:if>/>
                <bean:message key="channel.manage.sync.synckickstart.jsp"/>
            </label>
        </div>
        
        <div class="checkbox">
            <label>
                <input type="checkbox" name="fail" id="fail" <c:if test='${fail}'> checked</c:if>/>
                <bean:message key="channel.manage.sync.fail.jsp"/>
            </label>
        </div>
        
        <br/>
        <c:if test='${scheduleEditable}'>
            <jsp:include page="/WEB-INF/pages/common/fragments/repeat-task-picker.jspf">
                <jsp:param name="widget" value="date"/>
            </jsp:include>
            <div class="text-right">
                <button type="submit" value="<bean:message key='schedule.button'/>" class="btn btn-default"
                        name="dispatch" ${inactive ? 'disabled' : ''}>
                    <rhn:icon type="repo-schedule-sync"/>
                    <bean:message key="schedule.button"/>
                </button>
            </div>
        </c:if>

        <c:if test='${not scheduleEditable}'>
            <div class="panel panel-default">
                <div class="panel-heading">
                    <h4><bean:message key="schedule.header"/></h4>
                </div>

              <div class="panel-body">
                  <bean:message key="channel.manage.sync.scheduledisabled.jsp"/>
              </div>
            </div>
        </c:if>

    </rl:listset>

</body>
</html>
