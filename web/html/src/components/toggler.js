'use strict';

const React = require('react');
const Button = require('./buttons').Button;

const WithRecommended = React.createClass({
  render:function() {
    return (
      <Button
          className={'btn btn-default btn-sm ' + (this.props.enabled ? 'text-info' : 'text-muted')}
          handler={this.props.handler}
          icon={'fa-1-5x with-margin ' + (this.props.enabled ? 'fa-toggle-on' : 'fa-toggle-off')}
          text={t('with recommended')}
      />
    )
  }
});

module.exports = {
  WithRecommended: WithRecommended
}
