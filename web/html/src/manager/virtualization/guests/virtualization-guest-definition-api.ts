import * as React from "react";

import Network from "utils/network";
import { Utils as MessagesUtils } from "components/messages";

type Props = {
  hostid: string;
  guestUuid: string;
  children: (...args: any[]) => JSX.Element;
};

type State = {
  messages: Array<any>;
  definition: any | null | undefined;
};

class VirtualizationGuestDefinitionApi extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      messages: [],
      definition: null,
    };
  }

  componentDidMount() {
    Network.get(
      `/rhn/manager/api/systems/details/virtualization/guests/${this.props.hostid}/guest/${this.props.guestUuid}`
    ).then(
      (response) => {
        this.setState({ definition: response });
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
      definition: this.state.definition,
      messages: this.state.messages,
    });
  }
}

export { VirtualizationGuestDefinitionApi };
