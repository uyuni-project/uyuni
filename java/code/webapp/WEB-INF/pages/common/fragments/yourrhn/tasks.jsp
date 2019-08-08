<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<form method="post" name="rhn_list" action="/YourRhn.do">


  <div class="panel panel-default">

    <div class="panel-heading">
      <h3 class="panel-title">
        <bean:message key="yourrhn.jsp.task.title" />
      </h3>
    </div>

    <ul class="list-group">

      <c:if test="${requestScope.amountOfMinions > 0}">
        <rhn:require acl="user_role(satellite_admin)">
          <rhn:require acl="user_role(org_admin)">
            <li class="list-group-item">
              <rhn:icon type="nav-bullet" /> <a
                    class="js-spa"
                    href="/rhn/manager/systems/keys">
                   <bean:message key="yourrhn.jsp.tasks.minions" arg0="${requestScope.amountOfMinions}"/>
              </a>
            </li>
          </rhn:require>
        </rhn:require>
      </c:if>

      <c:if test="${requestScope.requiringReboot > 0}">
        <rhn:require acl="user_role(org_admin)">
          <li class="list-group-item">
            <rhn:icon type="nav-bullet" /> <a
                  class="js-spa"
                  href="/rhn/systems/RequiringReboot.do">
                  <bean:message key="yourrhn.jsp.tasks.reboot" arg0="${requestScope.requiringReboot}"/>
            </a>
          </li>
        </rhn:require>
      </c:if>

      <rhn:require acl="not user_role(satellite_admin)">
        <rhn:require acl="user_role(org_admin)">
        <li class="list-group-item">
            <rhn:icon type="nav-bullet" /> <a
                class="js-spa"
                href="/rhn/systems/SystemEntitlements.do"> <bean:message
                  key="yourrhn.jsp.tasks.subscriptions" />
            </a>
          </li>
        </rhn:require>
      </rhn:require>

      <rhn:require acl="user_role(satellite_admin)">
        <rhn:require acl="user_role(org_admin)">
          <li class="list-group-item">
          <rhn:icon type="nav-bullet" /> <bean:message
                key="yourrhn.jsp.task.manage_subscriptions" /> <br />
              &ensp; &ensp;<a
                  class="js-spa"
                  href="/rhn/systems/SystemEntitlements.do">
                <bean:message key="header.jsp.my_organization" />
            </a>
          </li>
        </rhn:require>
      </rhn:require>

      <rhn:require acl="user_role(org_admin)">
        <li class="list-group-item">
        <rhn:icon type="nav-bullet" /> <a
                class="js-spa"
                href="/rhn/manager/systems/bootstrap">
              <bean:message key="yourrhn.jsp.tasks.registersystem" />
          </a>
        </li>
      </rhn:require>

      <rhn:require
        acl="user_role(activation_key_admin)">
        <li class="list-group-item">
        <rhn:icon type="nav-bullet" /> <a
                class="js-spa"
                href="/rhn/activationkeys/List.do"> <bean:message
                key="yourrhn.jsp.tasks.activationkeys" />
          </a>
        </li>

      </rhn:require>

      <rhn:require
        acl="user_role(config_admin)">
        <li class="list-group-item">
          <rhn:icon type="nav-bullet" /> <a
                class="js-spa"
                href="/rhn/kickstart/KickstartOverview.do"> <bean:message
                key="yourrhn.jsp.tasks.kickstart" />
          </a>
        </li>

        <li class="list-group-item">
          <rhn:icon type="nav-bullet" /> <a
                class="js-spa"
                href="/rhn/configuration/file/GlobalConfigFileList.do">
              <bean:message key="yourrhn.jsp.tasks.configuration" />
          </a>
        </li>

        <rhn:require acl="user_role(satellite_admin)">
          <li class="list-group-item">
            <rhn:icon type="nav-bullet" /> <a
              class="js-spa"
              href="/rhn/admin/multiorg/Organizations.do"> <bean:message
                  key="yourrhn.jsp.tasks.manage_sat_orgs" />
            </a>
          </li>
        </rhn:require>

      </rhn:require>

      <rhn:require acl="user_role(satellite_admin)">
        <li class="list-group-item">
          <rhn:icon type="nav-bullet" /> <a
                class="js-spa"
                href="/rhn/admin/config/GeneralConfig.do"> <bean:message
                key="yourrhn.jsp.tasks.config_sat" />
          </a>
        </li>
      </rhn:require>


    </ul>


    <div class="panel-footer"></div>
  </div>

</form>
