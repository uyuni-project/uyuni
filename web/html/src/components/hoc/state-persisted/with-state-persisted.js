// We should integrate flow here, so we can have type-checker for all props even when decorating
// a component with this HOC. Check https://flow.org/en/docs/react/hoc/
// this seems not to be working with React14 - lets try to implement this after

import React from 'react';

type WithStatePersistedProps = {
  saveState: Function,
  loadState: Function
}

function getDisplayName(WrappedComponent) {
  return WrappedComponent.displayName || WrappedComponent.name || 'Component';
}

function withStatePersisted(WrappedComponent) {
  class WithStatePersisted extends React.Component<WithStatePersistedProps> {
    componentDidMount() {
      if (this.props.loadState) {
        if (this.props.loadState()) {
          this.refs.wrapped.state = this.props.loadState();// eslint-disable-line
        }
      }
    }

    componentWillUnmount() {
      if (this.props.saveState) {
        this.props.saveState(this.refs.wrapped && this.refs.wrapped.state);// eslint-disable-line
      }
    }

    render() {
      return <WrappedComponent ref={() => 'wrapped'} {...this.props} />;
    }
  }

  WithStatePersisted.displayName = `WithStatePersisted(${getDisplayName(WrappedComponent)})`;
  return WithStatePersisted;
}

export default withStatePersisted;
