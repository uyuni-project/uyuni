'use strict';

const React = require('react');
const ReactDOM = require('react-dom');

const Products = React.createClass({
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
    const prevStyle = { 'margin-left': '10px' , 'vertical-align': 'middle'};
    return (
      <div className='responsive-wizard'>
        {title}
        <div className='spacewalk-content-nav'>
          <ul className='nav nav-tabs'>
            <li><a href='/rhn/admin/setup/ProxySettings.do'>{t('HTTP Proxy')}</a></li>
            <li><a href='/rhn/admin/setup/MirrorCredentials.do'>{t('Organization Credentials')}</a></li>
            <li className='active'><a href='/rhn/manager/admin/setup/products'>{t('SUSE Products')}</a></li>
            <li><a href='/rhn/admin/setup/SUSEProducts.do'>{t('**SUSE Products [OLD VERSION]**')}</a></li>
          </ul>
        </div>
        <div className='panel panel-default' id='products-content' data-refresh-needed='${refreshNeeded}'>
            <div className='panel-body'>
            </div>
        </div>
    

        <div className='panel-footer'>
          <div className='btn-group'>
            <a className='btn btn-default' href='/rhn/admin/setup/MirrorCredentials.do'>
              <i className='fa fa-arrow-left'></i>{t('Prev')}
            </a>
          </div>
          <span style={prevStyle}>
            {t('3 of 3')}
          </span>
        </div>

      </div>
    )
  }
});

ReactDOM.render(
  <Products />,
  document.getElementById('products')
);
