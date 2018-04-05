'use strict';

const React = require('react');
const Button = require('./buttons').Button;

const WithRecommended = React.createClass({
  render:function() {
    return (
      <span onClick={this.props.handler} className='pointer v-middle'>
        <i className={'v-middle fa ' + (this.props.enabled ? 'fa-toggle-on text-success' : 'fa-toggle-off')} />
        &nbsp;
        <span className='v-middle'>{t('include recommended')}</span>
      </span>
    )
  }
});

module.exports = {
  WithRecommended: WithRecommended
}
