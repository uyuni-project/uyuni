import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";

type Props = {
  loading: boolean;
};

type State = {
  loading: boolean;
};

class ProxyConfig extends React.Component<Props, State> {
  initState: State;

  constructor(props: Props) {
    super(props);

    this.initState = {
      loading: props.loading,
    };

    this.state = this.initState;
  }

  render() {
    return (
      <div>
        {this.state.loading}
      </div>
    );
  }
}

export const renderer = (id: string, docsLocale: string, isAdmin: boolean) => {
  return SpaRenderer.renderNavigationReact(<ProxyConfig loading={true} />, document.getElementById(id));
};
