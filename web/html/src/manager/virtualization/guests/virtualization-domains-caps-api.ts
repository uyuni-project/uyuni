import * as React from "react";

import Network from "utils/network";
import { Utils as MessagesUtils } from "components/messages";

type Props = {
  hostId: string;
  children: (...args: any[]) => JSX.Element;
};

type State = {
  messages: Array<any>;
  osTypes: Array<string>;
  domainsCaps: Array<any>;
};

class VirtualizationDomainsCapsApi extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      osTypes: [],
      domainsCaps: [],
      messages: [],
    };
  }

  componentDidMount() {
    Network.get(
      `/rhn/manager/api/systems/details/virtualization/guests/${this.props.hostId}/domains_capabilities`
    ).then(
      (response) => {
        this.setState({
          osTypes: response.osTypes,
          domainsCaps: response.domainsCaps,
        });
      },
      (xhr) => {
        const errMessages =
          xhr.status === 0
            ? [
                MessagesUtils.error(
                  t("Request interrupted or invalid response received from the server. Please try again.")
                ),
              ]
            : [MessagesUtils.error(Network.errorMessageByStatus(xhr.status))];
        this.setState({
          messages: errMessages,
        });
      }
    );
  }

  render() {
    return this.props.children({
      osTypes: this.state.osTypes,
      domainsCaps: this.state.domainsCaps,
      messages: this.state.messages,
    });
  }
}

export { VirtualizationDomainsCapsApi };
