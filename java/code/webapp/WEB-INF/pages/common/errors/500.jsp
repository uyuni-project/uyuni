<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>

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
                    <bean:message key="500.jsp.title" />
                </h1>
                <p>
                    <bean:message key="500.jsp.summary" />
                </p>
                <p>
                    <bean:message key="500.jsp.message" />
                </p>
                <p>
                    <a href="/"><bean:message key="500.jsp.backhome" /></a>
                </p>
            </section>
        </div>
    </div>
</body>
</html>
