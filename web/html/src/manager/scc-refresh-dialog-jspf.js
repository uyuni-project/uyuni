'use strict';

const React = require('react');
const ReactDOM = require('react-dom');

const SCCDialog = React.createClass({
  render: function() {
    return (
      <div>
        <div id="scc-migration-dialog" className="modal fade bs-example-modal-sm"
            tabIndex="-1" role="dialog" aria-labelledby="scc-refresh-dialog-title" aria-hidden="true">
          <div className="modal-dialog modal-sm">
              <div className="modal-content">
                  <div className="modal-header">
                    <h4 className="modal-title">
                      <span id="scc-refresh-dialog-title"></span>
                    </h4>
                  </div>
                  <div className="modal-body">
                      <p>{t('Please be patient, this might take several minutes.')}</p>
                      <ul id='scc-task-list'>
                      </ul>
                      <div id='scc-migration-current-task'></div>
                  </div>
                  <div className="modal-footer row">
                      <div id="scc-migration-dialog-status" className="col-md-9 text-left"></div>
                      <div className="col-md-3 text-right">
                        <button id="scc-migrate-dialog-close-btn" type="button" className="btn btn-default" data-dismiss="modal">
                          {t('Close')}
                        </button>
                      </div>
                  </div>
              </div>
          </div>
        </div>

        <div className="hidden" id="sccconfig.jsp.channels">{t('Channels')}</div>
        <div className="hidden" id="sccconfig.jsp.channelfamilies">{t('Channel Families')}</div>
        <div className="hidden" id="sccconfig.jsp.products">{t('Products')}</div>
        <div className="hidden" id="sccconfig.jsp.productchannels">{t('Product Channles')}</div>
        <div className="hidden" id="sccconfig.jsp.subscriptions">{t('Subscriptions')}</div>
        <div className="hidden" id="sccconfig.jsp.completed">{t('Completed')}</div>
        <div className="hidden" id="sccconfig.jsp.failed">{t('Operation not successful')}</div>
        <div className="hidden" id="sccconfig.jsp.failed.details.link">{t('Details')}</div>
      </div>
    )
  }
});


module.exports = {
  SCCDialog: SCCDialog
}