import * as React from "react";
import SpaRenderer from "core/spa/spa-renderer";

type Props = {};

type State = {
  unreadMessagesLength: any;
  websocket: any;
  classStyle: string;
  errors?: any;
  websocketErr?: any;
  pageUnloading?: boolean;
};

class Notifications extends React.Component<Props, State> {
  state: State = {
    unreadMessagesLength: null,
    websocket: null,
    classStyle: "",
  };

  onBeforeUnload = (e) => {
    if (this.state.websocket != null) {
      this.state.websocket.close();
    }
    this.setState({
      pageUnloading: true,
    });
  };

  componentDidMount() {
    var port = window.location.port;
    var url = "wss://" + window.location.hostname + (port ? ":" + port : "") + "/rhn/websocket/notifications";
    var ws = new WebSocket(url);
    ws.onopen = () => {};
    ws.onclose = (e) => {
      var errs = this.state.errors ? this.state.errors : [];
      if (!this.state.pageUnloading && !this.state.websocketErr) {
        errs.push(t("Websocket connection closed. Refresh the page to try again."));
      }
      this.setState({
        errors: errs,
        websocket: null,
      });
    };
    ws.onerror = (e) => {
      console.log("Websocket error: " + JSON.stringify(e));
      if (this.state.websocket != null) {
        this.state.websocket.close();
      }
      this.setState({
        errors: [t("Error connecting to server. Refresh the page to try again.")],
        websocketErr: true,
        websocket: null,
      });
    };
    ws.onmessage = (e) => {
      this.setState({ unreadMessagesLength: e.data });
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
        <i className={this.state.websocket == null ? "fa fa-bell-slash" : "fa fa-bell"}></i>
        {this.state.websocket != null && this.state.unreadMessagesLength > 0 ? (
          <div id="notification-counter" className={this.state.classStyle}>
            {this.state.unreadMessagesLength}
          </div>
        ) : null}
      </a>
    );
  }
}

SpaRenderer.renderGlobalReact(<Notifications />, document.getElementById("notifications"));
