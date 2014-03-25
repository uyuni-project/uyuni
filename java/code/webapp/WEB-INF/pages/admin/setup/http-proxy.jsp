<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>

<html>
    <head>
        <script type="text/javascript" src="/rhn/dwr/interface/HttpProxyRenderer.js"></script>
        <script type="text/javascript" src="/javascript/susemanager-setup-wizard.js"></script>
        <script type="text/javascript" src="/javascript/susemanager-setup-wizard-HTTPproxy.js"></script>
        <script type="text/javascript" src="/javascript/responsive-tab.js"></script>
        <script type="text/javascript">
            $(document).on("ready", function(){
                $.tabResizer()
            });
        </script>
    </head>
    <body>
        <div class="responsive-wizard">
            <rhn:toolbar base="h1" icon="header-preferences">
                Setup Wizard
            </rhn:toolbar>
            <rhn:dialogmenu mindepth="0" maxdepth="1" definition="/WEB-INF/nav/setup_wizard.xml"
                            renderer="com.redhat.rhn.frontend.nav.DialognavRenderer" />
            <div class="panel panel-default">
              <div class="panel-body">
                <!-- HTTP proxy: 1.0 -->
                <div class="row" id="http-proxy">
                    <div class="col-sm-9">
                        <div class="panel panel-default">
                            <div class="panel-body">
                                <form class="form-horizontal" role="form">
                                    <div class="form-group">
                                        <label for="http-proxy-input-hostname" class="col-xs-4 control-label">HTTP Proxy Hostname:</label>
                                        <div class="col-xs-8">
                                            <p class="form-control-static http-proxy-hostname">error</p>
                                          <input type="text" class="form-control http-proxy-hostname" id="http-proxy-input-hostname" value="something" placeholder="hostname:port">
                                        </div>
                                      </div>
                                      <div class="form-group">
                                        <label for="http-proxy-input-username" class="col-xs-4 control-label">HTTP Proxy Username:</label>
                                        <div class="col-xs-8">
                                            <p class="form-control-static http-proxy-username">error</p>
                                          <input type="text" class="form-control http-proxy-username" id="http-proxy-input-username" value="" placeholder="Username">
                                        </div>
                                      </div>
                                      <div class="form-group">
                                        <label for="http-proxy-input-password" class="col-xs-4 control-label">HTTP Proxy password:</label>
                                        <div class="col-xs-8">
                                            <p class="form-control-static http-proxy-password">error</p>
                                          <input type="password" class="form-control http-proxy-password" id="http-proxy-input-password" value="" placeholder="password">
                                        </div>
                                      </div>
                                </form>
                            </div>
                            <div class="panel-footer">
                                <div id="http-save-btn" class="text-right">
                                    <button id="http-proxy-save" class="btn btn-success">Save and Verify</button>
                                </div>
                                <div class="text-left">
                                    <a id="http-proxy-verify" href="#"><i class="fa fa-check-square text-success"></i></a>
                                    <a id="http-proxy-edit" href="#"><i class="fa fa-pencil"></i></a>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="col-sm-3 hidden-xs" id="wizard-faq">
                        <img src="../../../img/setup-wizard/http-proxy.png" />
                        <h4>HTTP Proxy</h4>
                        <p>If this server uses an HTTP proxy to access the outside network, you can use this form to configure it. If that is not the case simply click on Next.</p>
                    </div>
                </div>
              </div>
              <div class="panel-footer">
                <div class="row">
                    <div class="col-sm-3 hidden-xs">
                        1 of 2
                    </div>
                    <div class="col-sm-6 text-center">
                        <a class="btn btn-success" href="/rhn/admin/setup/MirrorCredentials.do">Next <i class="fa fa-arrow-right"></i></a>
                    </div>
                </div>
              </div>
            </div>
        </div>
        <!-- Message when the user clicks on Verify -->
        <div class="modal fade" id="Verify-Proxy" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
          <div class="modal-dialog">
            <div class="modal-content">
              <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                <h4 class="modal-title" id="myModalLabel">HTTP Proxy Verification</h4>
              </div>
              <div class="modal-body">
              </div>
              <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
              </div>
            </div>
          </div>
        </div>
    </body>
</html>
