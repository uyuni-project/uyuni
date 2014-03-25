<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>

<html>
    <head>
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
                                            <p class="editable-text http-proxy-input-hostname">error</p>
                                          <input type="text" class="form-control" id="http-proxy-input-hostname" placeholder="Hostname">
                                        </div>
                                      </div>
                                      <div class="form-group">
                                        <label for="http-proxy-input-Username" class="col-xs-4 control-label">HTTP Proxy Username:</label>
                                        <div class="col-xs-8">
                                            <p class="editable-text http-proxy-input-Username">error</p>
                                          <input type="text" class="form-control" id="http-proxy-input-Username" value="" placeholder="Username">
                                        </div>
                                      </div>
                                      <div class="form-group">
                                        <label for="http-proxy-input-password" class="col-xs-4 control-label">HTTP Proxy password:</label>
                                        <div class="col-xs-8">
                                            <p class="editable-text http-proxy-input-password">error</p>
                                          <input type="password" class="form-control" id="http-proxy-input-password" value="" placeholder="password">
                                        </div>
                                      </div>
                                </form>
                            </div>
                            <div class="panel-footer">
                                <div id="http-save-btn" class="text-right">
                                    <button class="btn btn-success">Save and Verify</button>
                                </div>
                                <div id="http-proxy-edit" class="text-left">
                                    <a href="#"><i class="fa fa-check-square text-success"></i></a>
                                    <a href="#"><i class="fa fa-pencil"></i></a>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="col-sm-3 hidden-xs" id="wizard-faq">
                        <img src="../../../img/setup-wizard/http-proxy.png" />
                        <h4>Any help title</h4>
                        <p>Any help description here. Any help description here. Any help description here. </p>
                    </div>
                </div>
              </div>
              <div class="panel-footer">
                <div class="row">
                    <div class="col-sm-3 hidden-xs">
                        1 of 7
                    </div>
                    <div class="col-sm-6 text-center">
                        <button class="btn btn-default" disabled="disabled"><i class="fa fa-arrow-left"></i>Prev</button>
                        <a class="btn btn-success" href="/rhn/admin/setup/MirrorCredentials.do">Next <i class="fa fa-arrow-right"></i></a>
                    </div>
                    <div class="col-sm-3 text-right">
                        <a href="#">You can skip this step <i class="fa fa-step-forward"></i></a>
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
