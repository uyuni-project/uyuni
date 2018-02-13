'use strict';

const React = require('react');
const ReactDOM = require('react-dom');

const setupWizartSteps = [
  {
    label: 'HTTP Proxy',
    url: '/rhn/admin/setup/ProxySettings.do',
    active: false
  },
  {
    label: 'Organization Credentials',
    url: '/rhn/admin/setup/MirrorCredentials.do',
    active: false
  },
  {
    label: 'SUSE Products',
    url: '/rhn/admin/setup/ProxySettings.do',
    active: true
  },
  {
    label: '**SUSE Products [OLD VERSION]**',
    url: '/rhn/admin/setup/SUSEProducts.do',
    active: false
  }
];

const Products = React.createClass({
  getInitialState: function() {
    return {
      steps: []
    }
  },

  render: function() {
    const title =
      <div className='spacewalk-toolbar-h1'>
        <h1>
          <i className='fa fa-header-preferences'></i>
          {t('Setup Wizard')}
          <a href='/rhn/help/reference/en-US/ref.webui.admin.jsp#ref.webui.admin.wizard'
              target='_blank'><i className='fa fa-question-circle spacewalk-help-link'></i>
          </a>
        </h1>
      </div>
    ;

    const tabs = 
      <div className='spacewalk-content-nav'>
        <ul className='nav nav-tabs'>
          { setupWizartSteps.map(step => <li className={step.active ? 'active' : ''}><a href={step.url}>{t(step.label)}</a></li>)}
        </ul>
      </div>;

    const prevStyle = { 'margin-left': '10px' , 'vertical-align': 'middle'};
    const currentStepIndex = setupWizartSteps.indexOf(setupWizartSteps.find(step => step.active));
    const footer =
      <div className='panel-footer'>
        <div className='btn-group'>  
          {
            currentStepIndex > 1 ?
              <a className="btn btn-default" href={setupWizartSteps[currentStepIndex-1].url}>
                <i className="fa fa-arrow-left"></i>{t('Prev')}
              </a> : null
          }
          {
            currentStepIndex < (setupWizartSteps.length - 1) ?
              <a className="btn btn-success" href={setupWizartSteps[currentStepIndex+1].url}>
                <i className="fa fa-arrow-right"></i>{t('Next')}
              </a> : null
          }
        </div>
        <span style={prevStyle}>
          { currentStepIndex+1 }&nbsp;{t('of')}&nbsp;{ setupWizartSteps.length }
        </span>
      </div>;

    return (
      <div className='responsive-wizard'>
        {title}
        {tabs}
        <div className='panel panel-default' id='products-content' data-refresh-needed='${refreshNeeded}'>
            <div className='panel-body'>
            </div>
        </div>
        {footer}
      </div>
    )
  }
});

ReactDOM.render(
  <Products />,
  document.getElementById('products')
);
