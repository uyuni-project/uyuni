<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ page import="com.suse.manager.webui.menu.MenuTree" %>
<%@ page import="com.redhat.rhn.common.conf.ConfigDefaults"%>

<!-- enclosing head tags in layout_c.jsp -->
    <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
    <!-- HTML5 shim and Respond.js IE8 support of HTML5 elements and media queries -->
    <!--[if lt IE 9]>
      <script type='text/javascript' src="/javascript/html5.js"></script>
      <script type='text/javascript' src="/javascript/respond.js"></script>
    <![endif]-->
    <c:if test="${pageContext.request.requestURI == '/rhn/Load.do'}">
      <meta http-equiv="refresh" content="0; url=<c:out value="${param.return_url}" />" />
    </c:if>
    <meta http-equiv="content-type" content="text/html;charset=UTF-8"/>
    <title>
      <bean:message key="layout.jsp.productname"/>
      <%= MenuTree.getTitlePage(pageContext) %>
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

    <!-- import spacewalk styles -->
    <c:choose>
        <c:when test='${ConfigDefaults.get().isUyuni()}'>
            <rhn:require acl="is(development_environment)">
                <link rel="stylesheet/less" type="text/css" href="/css/uyuni/spacewalk.less" />
                <script>less = { env: 'development for Uyuni' };</script>
                <script src="/javascript/less.js"></script>
            </rhn:require>
            <rhn:require acl="not is(development_environment)">
                <link rel="stylesheet" href="/css/uyuni/spacewalk.css?cb=${cb_version}" />
            </rhn:require>
        </c:when>
        <c:otherwise>
            <rhn:require acl="is(development_environment)">
                <link rel="stylesheet/less" type="text/css" href="/css/spacewalk.less" />
                <script>less = { env: 'development for SUSE Manager' };</script>
                <script src="/javascript/less.js"></script>
            </rhn:require>
            <rhn:require acl="not is(development_environment)">
                <link rel="stylesheet" href="/css/spacewalk.css?cb=${cb_version}" />
            </rhn:require>
        </c:otherwise>
    </c:choose>

    <script src="/javascript/loggerhead.js?cb=${cb_version}"></script>
    <script src="/javascript/frontend-log.js?cb=${cb_version}"></script>

    <script src="/javascript/jquery.js?cb=${cb_version}"></script>
    <script src="/javascript/bootstrap.js?cb=${cb_version}"></script>
    <script src="/javascript/select2/select2.js?cb=${cb_version}"></script>
    <script src="/javascript/spacewalk-essentials.js?cb=${cb_version}"></script>
    <script src="/javascript/susemanager-translate.js?cb=${cb_version}"></script>
    <script src="/javascript/spacewalk-checkall.js?cb=${cb_version}"></script>

    <script src="/rhn/dwr/engine.js?cb=${cb_version}"></script>
    <script src="/rhn/dwr/util.js?cb=${cb_version}"></script>
    <script src="/rhn/dwr/interface/DWRItemSelector.js?cb=${cb_version}"></script>
    <script src="/javascript/jquery.timepicker.js?cb=${cb_version}"></script>
    <script src="/javascript/bootstrap-datepicker.js?cb=${cb_version}"></script>

    <script src='/vendors/vendors.bundle.js?cb=${cb_version}'></script>
    <script src='/javascript/manager/core.bundle.js?cb=${cb_version}'></script>
    <script src='/javascript/manager/main.bundle.js?cb=${cb_version}'></script>
    <script src='/javascript/momentjs/moment-with-langs.min.js?cb=${cb_version}' type='text/javascript'></script>
