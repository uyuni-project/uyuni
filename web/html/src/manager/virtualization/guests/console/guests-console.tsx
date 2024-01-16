import { hot } from "react-hot-loader/root";

import * as React from "react";

import { Button } from "components/buttons";
import { showDialog } from "components/dialog/util";
import { hideDialog } from "components/dialog/util";

import Network from "utils/network";

import styles from "./guests-console.css";
import { ConsoleClientType } from "./guests-console-types";
import { MessagePopUp, PopupState } from "./MessagePopUp";
import { SpiceClient } from "./spice-client";
import { VncClient } from "./vnc-client";

type Props = {
  hostId?: string;
  guestUuid: string;
  guestName?: string;
  guestState?: string;
  graphicsType?: string;
  token?: string;
};

type State = {
  error?: string | string[];
  expanded: boolean;
  connected: boolean;
  popupState: PopupState;
  password?: string;
  vmState?: string;
  currentToken?: string;
  hostId?: string;
  stateName?: string;
  graphicsType?: string;
  guestName?: string;
};

function getTokenLifetime(token: String): number {
  const jwsParts = token.split(".");
  const claims = JSON.parse(atob(jwsParts[1]));
  // TODO: If you touch this code, please use `localizedMoment()` here instead
  // eslint-disable-next-line local-rules/no-raw-date
  return new Date(claims["exp"] * 1000).valueOf() - new Date(claims["iat"] * 1000).valueOf();
}

class GuestsConsole extends React.Component<Props, State> {
  client?: ConsoleClientType;
  popupSubmit?: (...args: any[]) => any;
  websocket?: WebSocket;
  pageUnloading: boolean;
  intervalId?: number;
  updateGuestTimeoutId?: number;
  // This is intentionally not in state since we don't want this alone to trigger a rerender
  isRefreshing = false;

  clients = {
    vnc: VncClient,
    spice: SpiceClient,
  };

  constructor(props: Props) {
    super(props);

    const error =
      props.graphicsType != null && this.clients[props.graphicsType] != null
        ? undefined
        : t(`Can not show display. Ensure, the virtual machine is stopped, set the display to VNC and start again`);

    this.pageUnloading = false;
    this.openVirtSocket();

    this.state = {
      error,
      expanded: false,
      connected: false,
      popupState: "wait",
      password: undefined,
      vmState: props.guestState,
      currentToken: props.token,
      hostId: props.hostId,
      graphicsType: props.graphicsType,
      guestName: props.guestName,
    };
  }

  componentDidMount() {
    if (this.state.currentToken != null) {
      // Schedule token refresh two minutes before its expiration
      const tokenTime = getTokenLifetime(this.state.currentToken);
      this.intervalId = window.setInterval(this.refreshToken, tokenTime - 120000);
    }

    this.connect();
  }

  componentWillUnmount() {
    this.pageUnloading = true;
    this.client?.removeErrorHandler();
    if (this.intervalId) {
      window.clearInterval(this.intervalId);
    }
    if (this.updateGuestTimeoutId) {
      clearTimeout(this.updateGuestTimeoutId);
    }
  }

  openVirtSocket = () => {
    const { port } = window.location;
    const url = `wss://${window.location.hostname}${port ? `:${port}` : ""}/rhn/websocket/minion/virt-notifications`;
    const ws = new WebSocket(url);

    ws.onopen = () => {
      // Tell the websocket that we want to hear from all events about this virtual guest.
      ws.send(`{guestUuid: ${this.props.guestUuid}}`);
    };

    ws.onclose = () => {
      if (!this.pageUnloading) {
        setTimeout(this.openVirtSocket, 1000);
      }
    };

    ws.onmessage = (e) => {
      if (typeof e.data === "string") {
        this.virtEventHandler(JSON.parse(e.data));
      }
    };

    this.websocket = ws;
  };

  connect = () => {
    if (
      this.state.graphicsType != null &&
      this.state.vmState !== "stopped" &&
      Object.keys(this.clients).includes(this.state.graphicsType) &&
      this.state.currentToken != null &&
      this.client == null
    ) {
      const port = window.location.port ? `:${window.location.port}` : "";
      const url = `wss://${window.location.hostname}${port}/rhn/websockify/?token=${this.state.currentToken}`;
      this.client = new this.clients[this.state.graphicsType](
        "canvas",
        url,
        this.onConnect,
        this.onDisconnect,
        this.askPassword
      );
      this.client?.connect();

      this.popupSubmit = undefined;
      this.setState(
        {
          popupState: "wait",
          password: undefined,
        },
        this.showPopup
      );
    }
  };

  onConnect = () => {
    hideDialog("popup");
    this.setState({
      expanded: false,
      connected: true,
    });
  };

  onDisconnect = (error: string | null | undefined) => {
    // When disconnecting during migration, get a new token to reconnect
    if (this.state.vmState === "migrating") {
      this.refreshToken();
      return;
    }

    // Something bad happened, notify the user
    this.popupSubmit = this.connect.bind(this);
    this.client = undefined;
    this.setState((oldState) => {
      const connectionError = oldState.connected ? t("Disconnected") : t("Failed to connect");
      return {
        error: error || connectionError,
        connected: false,
        popupState: "errors",
      };
    }, this.showPopup);
  };

  toggleScale = () => {
    this.setState((state) => {
      const expanded = !state.expanded;
      if (this.client != null) {
        this.client.toggleScale(expanded);
      }
      return Object.assign({}, state, { expanded });
    });
  };

  showPopup = () => {
    showDialog("popup");
  };

  askPassword = () =>
    new Promise((resolve) => {
      this.popupSubmit = () => {
        hideDialog("popup");
        resolve(this.state.password);
      };
      this.setState({ popupState: "askPassword" }, this.showPopup);
    });

  onPasswordChange = (model: any) => {
    this.setState({ password: model.password });
  };

  refreshToken = () => {
    if (this.isRefreshing) {
      return;
    }
    this.isRefreshing = true;
    Network.post(
      `/rhn/manager/api/systems/details/virtualization/guests/consoleToken/${this.props.guestUuid}`,
      this.state.currentToken
    ).then(
      (response) => {
        this.isRefreshing = false;
        this.popupSubmit = undefined;

        this.setState(
          {
            connected: false,
            currentToken: response,
            popupState: "wait",
          },
          this.connect
        );
      },
      (xhr) => {
        this.isRefreshing = false;
        if (xhr.status === 400) {
          // We have hit a time where the VM is not yet in the DB, retry
          this.refreshToken();
        } else {
          this.popupSubmit = () => {
            window.location.reload();
          };
          this.setState(
            {
              error: Network.errorMessageByStatus(xhr.status),
              popupState: "errors",
            },
            this.showPopup
          );
        }
      }
    );
  };

  updateGuestData = () => {
    Network.get(
      `/rhn/manager/api/systems/details/virtualization/guests/${this.state.hostId}/guest/${this.props.guestUuid}`
    ).then(
      (response) => {
        this.setState(
          {
            graphicsType: response.graphics?.type,
            guestName: response.name,
          },
          this.refreshToken
        );
      },
      (xhr) => {
        if (xhr.status === 404) {
          if (this.updateGuestTimeoutId) {
            clearTimeout(this.updateGuestTimeoutId);
          }
          // We may have hit the time where the machine is not yet in the DB, try again
          this.updateGuestTimeoutId = window.setTimeout(() => this.updateGuestData(), 500);
        }
      }
    );
  };

  virtEventHandler = (msg: any) => {
    // Ignore all other events that come from other hosts
    // hostIds are parsed as numbers in the message while we get them as string from the properties
    const hostId = msg.hostId.toString();
    if (msg.event === "resumed" && msg.detail === "migrated") {
      this.client = undefined;
      setTimeout(() => {}, 500);
      this.refreshToken();
      this.setState({ vmState: "running", hostId }, this.showPopup);
    }

    if (this.state.hostId !== hostId) {
      if (this.state.hostId == null) {
        this.setState({ hostId }, this.updateGuestData);
      }
      return;
    }

    if (msg.event === "started") {
      setTimeout(() => {}, 500);
      this.refreshToken();
      this.setState({ vmState: "running" }, this.showPopup);
      return;
    }

    if (msg.event === "suspended" && msg.detail === "migrated") {
      this.client?.removeErrorHandler();
      this.setState({ vmState: "migrating" });
      return;
    }

    if (msg.event === "shutdown") {
      this.client?.removeErrorHandler();
      return;
    }

    if (msg.event === "stopped") {
      this.client = undefined;
      this.setState({ vmState: "stopped" });
      return;
    }
  };

  render() {
    const canResize = this.client != null && this.client.canResize;
    const areaClassName = `display_area_${this.state.graphicsType}`;
    return (
      <>
        <header className={`navbar-pf navbar navbar-dark bg-dark ${styles.navbar_pf_console}`}>
          <div className={`navbar-header d-flex flex-row ${styles.navbar_header_console}`}>
            <i className="fa spacewalk-icon-virtual-guest" />
            {this.state.guestName}
          </div>
          <ul className="nav navbar-nav navbar-utility d-flex flex-row">
            <li>
              {this.state.graphicsType === "vnc" && (
                <Button
                  title={t("Toggle full size")}
                  icon={this.state.expanded ? "fa-compress" : "fa-expand"}
                  handler={this.toggleScale}
                  disabled={!this.state.connected || !canResize}
                />
              )}
            </li>
          </ul>
        </header>
        <MessagePopUp
          id="popup"
          onSubmit={this.popupSubmit}
          popupState={this.state.popupState}
          model={this.state}
          setModel={this.onPasswordChange}
          error={this.state.error}
        />
        <div id="display-area" className={`${styles.display_area_console} ${styles[areaClassName]}`}>
          <div id="canvas" className={styles.canvas}>
            {this.state.vmState != null && this.state.vmState !== "running" && this.state.vmState !== "running" && (
              <div className="col-md-12">{t("Guest is not running")}</div>
            )}
            {this.state.vmState == null && <div className="col-md-12">{t("Unknown guest")}</div>}
          </div>
        </div>
      </>
    );
  }
}

export default hot(GuestsConsole);
