'use strict';

const React = require('react');
const Button = require('./buttons').Button;

const WithRecommended = React.createClass({
  render:function() {
    return (
      <span onClick={this.props.handler} className={'v-middle ' + (this.props.muted ? 'text-muted' : 'pointer')}>
        <i className={'v-middle fa ' + (this.props.enabled ? 'fa-toggle-on text-success' : 'fa-toggle-off')} />
        &nbsp;
        <span className='v-middle'>{t('include recommended')}</span>
      </span>
    )
  }
});

const TestState = React.createClass({
  render:function() {
    return (
      <span onClick={this.props.handler} className={'btn ' + (this.props.muted ? 'text-muted' : 'pointer')}>
        <i className={'v-middle fa ' + (this.props.enabled ? 'fa-toggle-on text-success' : 'fa-toggle-off')} />
        &nbsp;
        <span className='v-middle'>{t('Test State')}</span>
      </span>
    )
  }
});

module.exports = {
  WithRecommended: WithRecommended,
  TestState: TestState
}
