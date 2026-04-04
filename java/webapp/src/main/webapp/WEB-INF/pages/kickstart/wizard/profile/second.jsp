<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<html:html >

<head>
<meta http-equiv="Pragma" content="no-cache"/>
</head>

<body>
  <script language="javascript">
    function setState() {
      var radio = document.getElementById("wizard-defaultdownloadon");
      if (radio.checked == true) {
        disableCtl('wizard-userdefdload');
      }
    }

    function disableCtl(ctlId) {
      var ctl = document.getElementById(ctlId);
      ctl.disabled = true;
    }

    function enableCtl(ctlId) {
      var ctl = document.getElementById(ctlId);
      ctl.disabled = false;
    }

    function swapValues(fromCtlId, toCtlId) {
      var fromCtl = document.getElementById(fromCtlId);
      var toCtl = document.getElementById(toCtlId);
      toCtl.value = fromCtl.value;
    }

    function moveNext() {
      var form = jQuery("form[name='kickstartCreateWizardForm']");
      swapValues("wizard-nextstep", "wizard-curstep");
      form.submit();
    }

    function movePrevious() {
      var form = jQuery("form[name='kickstartCreateWizardForm']");
      swapValues("wizard-prevstep", "wizard-curstep");
      form.submit();
    }
  </script>

  <html:form method="post" action="/kickstart/CreateProfileWizard.do">
    <rhn:csrf />
    <rhn:submitted />
    <html:hidden property="wizardStep" styleId="wizard-curstep" />
    <html:hidden property="nextStep" styleId="wizard-nextstep"/>
    <html:hidden property="prevStep" styleId="wizard-prevstep" />
    <html:hidden property="kickstartLabel" />
    <html:hidden property="virtualizationTypeLabel" />
    <html:hidden property="kstreeId" />
    <html:hidden property="kstreeUpdateType" />
    <rhn:toolbar base="h1" icon="header-kickstart"><bean:message key="kickstart.jsp.create.wizard.step.two"/></rhn:toolbar>
    <p><bean:message key="kickstart.jsp.create.wizard.second.heading1" /></p>
    <div class="panel panel-default">
      <div class="panel-body">
        <div class="row">
          <div class="col-sm-6 col-sm-offset-3 offset-sm-3">
            <div class="radio">
              <label>
                <html:radio styleId="wizard-defaultdownloadon" property="defaultDownload" value="true" onclick="disableCtl('wizard-userdefdload');">
                </html:radio>
                <p>
                  <strong>
                    <bean:message key="kickstart.jsp.create.wizard.default.download.location.label" />:
                  </strong>
                </p>
                <bean:write name="kickstartCreateWizardForm" property="defaultDownloadLocation" />
              </label>
            </div>
            <div class="radio">
              <label>
                <html:radio styleId="wizard-defaultdownloadoff" property="defaultDownload" value="false" onclick="enableCtl('wizard-userdefdload');">
                </html:radio>
                <p>
                  <strong>
                    <bean:message key="kickstart.jsp.create.wizard.custom.download.location.label" />:
                  </strong>
                </p>
                <html:text property="userDefinedDownload" styleClass="form-control" styleId="wizard-userdefdload" size="50" maxlength="512" />
              </label>
            </div>
          </div>
        </div>
      </div>
      <div class="panel-footer text-center">
        <button type="button" onclick="movePrevious();" class="btn btn-default">
          <bean:message key='wizard.jsp.previous.step'/>
        </button>
      &nbsp;&nbsp;
        <button type="button" onclick="moveNext();" class="btn btn-primary">
          <bean:message key='wizard.jsp.next.step'/>
        </button>
      </div>
    </div>

  </html:form>
  <script>
    setState();
  </script>

</body>
</html:html>

