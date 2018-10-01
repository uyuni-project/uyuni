const PropTypes = require('prop-types');
const React = require('react');

/** @module toggler */

/**
 * A customized toggle switch element to represent boolean values.
 */
class Toggler extends React.Component {
  constructor(props) {
    super(props);
  }

  render() {
    let classes = this.props.disabled ? 'text-muted' : 'pointer';

    if(this.props.className) {
      classes += ' ' + this.props.className;
    }
    return (
      <span onClick={this.props.handler} className={classes}>
        <i className={'v-middle fa ' + (this.props.value ? 'fa-toggle-on text-success' : 'fa-toggle-off')} />
        &nbsp;
        <span className='v-middle'>{this.props.text}</span>
      </span>
    );
  }
}

Toggler.propTypes = {
  /** Callback function to execute on toggle switch. */
  handler: PropTypes.func,
  /** Text to display on the toggler. */
  text: PropTypes.string,
  /** The boolean value represented by the toggler. */
  value: PropTypes.bool,
  /** If true, the component will be rendered as disabled. */
  disabled: PropTypes.bool,
  /** className of the component. */
  className: PropTypes.string
};

module.exports = {
  Toggler: Toggler
}
