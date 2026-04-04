import { Component } from "react";

import SpaRenderer from "core/spa/spa-renderer";

import { DEPRECATED_unsafeEquals } from "utils/legacy";

type Props = Record<never, never>;

type State = {
  unreadMessagesLength: any;
  websocket: any;
  classStyle: string;
  errors?: any;
  websocketErr?: any;
  pageUnloading?: boolean;
};

class Notifications extends Component<Props, State> {
  state: State = {
    unreadMessagesLength: null,
    websocket: null,
    classStyle: "",
  };

  onBeforeUnload = () => {
    if (!DEPRECATED_unsafeEquals(this.state.websocket, null)) {
      this.state.websocket.close();
    }
    this.setState({
      pageUnloading: true,
    });
  };

  componentDidMount() {
    const port = window.location.port;
    const url = "wss://" + window.location.hostname + (port ? ":" + port : "") + "/rhn/websocket/notifications";
    const ws = new WebSocket(url);
    ws.onopen = () => {
      ws.send('["user-notifications"]');
    };
    ws.onclose = () => {
      this.setState((prevState) => {
        const errs = prevState.errors ? prevState.errors : [];
        if (!prevState.pageUnloading && !prevState.websocketErr) {
          errs.push(t("Websocket connection closed. Refresh the page to try again."));
        }
        return {
          errors: errs,
          websocket: null,
        };
      });
    };
    ws.onerror = (e) => {
      Loggerhead.error("Websocket error: " + JSON.stringify(e));
      if (!DEPRECATED_unsafeEquals(this.state.websocket, null)) {
        this.state.websocket.close();
      }
      this.setState({
        errors: [t("Error connecting to server. Refresh the page to try again.")],
        websocketErr: true,
        websocket: null,
      });
    };
    ws.onmessage = (e) => {
      const data = JSON.parse(e.data);
      this.setState({ unreadMessagesLength: data["user-notifications"] });
    };
    window.addEventListener("beforeunload", this.onBeforeUnload);

    this.setState({
      websocket: ws,
    });
  }

  componentWillUnmount() {
    window.removeEventListener("beforeunload", this.onBeforeUnload);
  }

  componentDidUpdate(prevProps, prevState) {
    if (prevState.unreadMessagesLength && this.state.unreadMessagesLength > prevState.unreadMessagesLength) {
      jQuery("#notification-counter").addClass("highlight-updated-value");
      setTimeout(function () {
        jQuery("#notification-counter").removeClass("highlight-updated-value");
      }, 1000);
    }
  }

  render() {
    return (
      <a className="js-spa" href="/rhn/manager/notification-messages">
        <i className={DEPRECATED_unsafeEquals(this.state.websocket, null) ? "fa fa-bell-slash" : "fa fa-bell"}></i>
        {!DEPRECATED_unsafeEquals(this.state.websocket, null) && this.state.unreadMessagesLength > 0 ? (
          <div id="notification-counter" className={this.state.classStyle}>
            {this.state.unreadMessagesLength}
          </div>
        ) : null}
      </a>
    );
  }
}

SpaRenderer.renderGlobalReact(<Notifications />, document.getElementById("notifications"));
