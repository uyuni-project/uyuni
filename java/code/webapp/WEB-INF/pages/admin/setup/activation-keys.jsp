<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>

<html>
    <head>
    </head>
    <body>
        <rhn:toolbar base="h1" icon="header-preferences">
            Setup Wizard
        </rhn:toolbar>
        <rhn:dialogmenu mindepth="0" maxdepth="1" definition="/WEB-INF/nav/setup_wizard.xml"
                        renderer="com.redhat.rhn.frontend.nav.DialognavRenderer" />
        <p>
            COMING SOON: Setup your activation keys.
        </p>
    </body>
</html>
