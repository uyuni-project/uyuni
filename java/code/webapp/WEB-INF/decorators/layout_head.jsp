<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ page import="com.redhat.rhn.common.conf.ConfigDefaults"%>
<%@ page import="com.redhat.rhn.GlobalInstanceHolder" %>

<!-- enclosing head tags in layout_c.jsp -->
    <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
    <c:if test="${pageContext.request.requestURI == '/rhn/Load.do'}">
      <meta http-equiv="refresh" content="0; url=<c:out value="${param.return_url}" />" />
    </c:if>
    <meta http-equiv="content-type" content="text/html;charset=UTF-8"/>
    <title>
      <bean:message key="layout.jsp.productname"/>
      <%= GlobalInstanceHolder.MENU_TREE.getTitlePage(pageContext) %>
    </title>
    <link rel="shortcut icon" href="/img/favicon.ico" />

    <meta name="viewport" content="width=device-width, initial-scale=1.0" />

    <c:set var="cb_version" value="${rhn:getConfig('web.buildtimestamp')}" />

    <!-- import default fonts/icons styles -->
    <link rel="stylesheet" href="/fonts/font-awesome/css/font-awesome.css?cb=${cb_version}" />
    <!-- import custom fonts/icons styles -->
    <link rel="stylesheet" href="/fonts/font-spacewalk/css/spacewalk-font.css?cb=${cb_version}" />

    <!-- import plugins styles -->
    <link rel="stylesheet" href="/css/jquery.timepicker.css?cb=${cb_version}" />
    <link rel="stylesheet" href="/css/bootstrap-datepicker.css?cb=${cb_version}" />
    <link rel="stylesheet" href="/javascript/select2/select2.css?cb=${cb_version}" />
    <link rel="stylesheet" href="/javascript/select2/select2-bootstrap.css?cb=${cb_version}" />

    <!-- import styles -->
    <c:set var="webTheme" value="${GlobalInstanceHolder.USER_PREFERENCE_UTILS.getCurrentWebTheme(pageContext)}"/>
    <!-- <link rel="stylesheet" href="/css/${webTheme}.css?cb=${cb_version}" /> -->
    <link rel="stylesheet" href="/css/susemanager-light.css?cb=${cb_version}" />

    <!-- expose user preferred language to the application -->
    <c:set var="currentLocale" value="${GlobalInstanceHolder.USER_PREFERENCE_UTILS.getCurrentLocale(pageContext)}"/>
    <script>window.preferredLocale='${currentLocale}'</script>

    <!-- expose user preferred documentation language to the application -->
    <c:set var="docsLocale" value="${GlobalInstanceHolder.USER_PREFERENCE_UTILS.getDocsLocale(pageContext)}"/>
    <script>window.docsLocale='${docsLocale}'</script>

    <!-- expose server and user datetime globally to handle localization -->
    <c:set var="serverTime" value="${GlobalInstanceHolder.VIEW_HELPER.getServerTime()}"/>
    <c:set var="serverTimeZone" value="${GlobalInstanceHolder.VIEW_HELPER.getExtendedServerTimeZone()}"/>
    <c:set var="userTimeZone" value="${GlobalInstanceHolder.USER_PREFERENCE_UTILS.getExtendedUserTimeZone(pageContext)}"/>
    <c:set var="userDateFormat" value="${GlobalInstanceHolder.USER_PREFERENCE_UTILS.getUserDateFormat(pageContext)}"/>
    <c:set var="userTimeFormat" value="${GlobalInstanceHolder.USER_PREFERENCE_UTILS.getUserTimeFormat(pageContext)}"/>
    <script>
        window.serverTime='${serverTime}'
        window.serverTimeZone='${serverTimeZone}'
        window.userTimeZone='${userTimeZone}'
        window.userDateFormat='${userDateFormat}'
        window.userTimeFormat='${userTimeFormat}'
    </script>

    <script src="/javascript/jquery.js?cb=${cb_version}"></script>
    <script src="/javascript/bootstrap.js?cb=${cb_version}"></script>
    <script src="/javascript/select2/select2.js?cb=${cb_version}"></script>
    <script src="/javascript/spacewalk-essentials.js?cb=${cb_version}"></script>
    <script src="/javascript/spacewalk-checkall.js?cb=${cb_version}"></script>

    <script src="/rhn/dwr/engine.js?cb=${cb_version}"></script>
    <script src="/rhn/dwr/util.js?cb=${cb_version}"></script>
    <script src="/rhn/dwr/interface/DWRItemSelector.js?cb=${cb_version}"></script>
    <script src="/javascript/jquery.timepicker.js?cb=${cb_version}"></script>
    <script src="/javascript/bootstrap-datepicker.js?cb=${cb_version}"></script>

    <script src='/javascript/manager/main.bundle.js?cb=${cb_version}'></script>
    <script src='/javascript/momentjs/moment-with-langs.min.js?cb=${cb_version}' type='text/javascript'></script>
