import React from 'react';
import ReactDOM from 'react-dom';

type WithStatePersistedProps = {
  saveState: Function,
  loadState: Function
}

function withStatePersisted(WrappedComponent) {
  class WithStatePersisted extends React.Component<WithStatePersistedProps> {

    componentWillUnmount () {
      if (this.props.saveState) {
        this.props.saveState(this.refs.wrapped && this.refs.wrapped.state);
      }
    }

  componentDidMount () {
    if (this.props.loadState) {
      if (this.props.loadState()) {
        this.refs.wrapped.state = this.props.loadState();
      }
    }
  }

    render() {
      return <WrappedComponent ref='wrapped' {...this.props}/>;
    }
  }

  WithStatePersisted.displayName = `WithStatePersisted(${getDisplayName(WrappedComponent)})`;
  return WithStatePersisted;
}

function getDisplayName(WrappedComponent) {
  return WrappedComponent.displayName || WrappedComponent.name || 'Component';
}

export default withStatePersisted;
