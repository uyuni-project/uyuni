<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>

<c:set value="${requestScope[&quot;javax.servlet.error.request_uri&quot;]}" var="errorUrl" />
<c:set var="escapedUrl" value="${fn:escapeXml(errorUrl)}"/>

<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>

<!DOCTYPE HTML>
<html>
<head>
    <meta http-equiv="content-type" content="text/html;charset=UTF-8" />
    <title><bean:message key="layout.jsp.productname" /></title>

    <link rel="shortcut icon" href="/img/favicon.ico" />
    <link rel="stylesheet" href="/fonts/font-awesome/css/font-awesome.css" />
    <link rel="stylesheet" href="/fonts/font-spacewalk/css/spacewalk-font.css" />

    <meta name="viewport" content="initial-scale=1" />

    <link rel="stylesheet/less" type="text/css" href="/css/spacewalk.less" />

    <script src="/javascript/less.js">
    </script>
    <script src="/javascript/bootstrap.js">
    </script>
    <script src="/javascript/spacewalk-essentials.js">
    </script>
</head>

<body>
    <div class="spacewalk-top-wrap">
        <header>
            <div class="spacewalk-header-container">
                <div id="logo" class="spacewalk-logo">
                    <a href="/" title="<bean:message key="layout.jsp.productname"/>"> <img src="/img/susemanager/logo-header.png"
                        alt="<bean:message key="layout.jsp.productname"/>"
                    />
                    </a>
                </div>
            </div>
        </header>

        <div class="spacewalk-main-column-layout">
            <section id="spacewalk-content">
                <h1>
                    <rhn:icon type="system-warn" title="500.jsp.imgAlt" />
                    <bean:message key="404.jsp.title"/>
                </h1>
                <p><bean:message key="404.jsp.summary" arg0="${escapedUrl}"/></p>
                <ol>
                    <li><bean:message key="404.jsp.reason1"/></li>
                    <li><bean:message key="404.jsp.reason2" arg0="${escapedUrl}"/></li>
                    <li><bean:message key="404.jsp.reason3" arg0="${escapedUrl}"/></li>
                    <li><bean:message key="404.jsp.reason4"/></li>
                </ol>
                <p>
                    <a href="/"><bean:message key="500.jsp.backhome" /></a>
                </p>
            </section>
        </div>
    </div>
</body>
</html>
