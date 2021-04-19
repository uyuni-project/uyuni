// @flow

import { hot } from 'react-hot-loader/root';
import * as React from 'react';
import { Button } from 'components/buttons';
import { showDialog } from 'components/dialog/util';
import { hideDialog } from 'components/dialog/util';
import { VncClient } from './vnc-client';
import type { ConsoleClientType } from './guests-console-types';
import { SpiceClient } from './spice-client';
import { MessagePopUp } from './MessagePopUp';
import styles from './guests-console.css';

type Props = {
  hostId: string,
  guestUuid: string,
  guestName: string,
  graphicsType: string,
  token: string,
};

type State = {
  error: ?string,
  expanded: boolean,
  connected: boolean,
  popupState: string,
  password: ?string,
};

class GuestsConsole extends React.Component<Props, State> {
  client: ConsoleClientType;
  popupSubmit: Function;

  constructor(props: Props) {
    super(props);

    const clients = {
      vnc: VncClient,
      spice: SpiceClient,
    };
    const port = window.location.port ? `:${window.location.port}` : "";
    const url = `wss://${window.location.hostname}${port}/rhn/websockify/?token=${this.props.token}`;
    this.client = new clients[this.props.graphicsType]('canvas', url, this.onConnect, this.onDisconnect, this.askPassword);

    const editUrl = `/rhn/manager/systems/details/virtualization/guests/${this.props.hostId}/edit/${this.props.guestUuid}`;
    const error = this.client !== undefined
      ? undefined
      : t(`Can not show display. Ensure, the virtual machine is stopped, <a href='${editUrl}'>set the display</a> to VNC and start again`);

    this.state = {
      error,
      expanded: false,
      connected: false,
      popupState: 'wait',
      password: undefined,
    };
  }

  componentDidMount() {
    this.connect();

    window.onbeforeunload = () => {
      this.client.removeErrorHandler();
    };
  }

  connect = () => {
    if (this.client != null) {
      this.client.connect();
    }
    this.popupSubmit = undefined;

    this.setState({
      popupState: 'wait',
      password: undefined,
    }, this.showPopup);
  }

  onConnect = () => {
    hideDialog('popup');
    this.setState({
      expanded: false,
      connected: true,
    });
  }

  onDisconnect = (error: ?string) => {
    const message = error != null ? error : t('Disconnected');
    const connectionError = t('Failed to connect');
    this.setState((oldState) => {
      const errorMessage = (oldState.connected ? message : connectionError);
      return {
        error: errorMessage,
        connected: false,
      };
    });
    this.popupSubmit = this.connect;
    this.setState({ popupState: 'errors' }, this.showPopup);
  }

  toggleScale = () => {
    this.setState((state) => {
      const expanded = !state.expanded;
      if (this.client != null) {
        this.client.toggleScale(expanded);
      }
      return Object.assign({}, state, { expanded });
    });
  }

  showPopup = () => {
    showDialog('popup');
  }

  askPassword = () => new Promise((resolve) => {
    this.popupSubmit = () => {
      hideDialog('popup');
      resolve(this.state.password);
    };
    this.setState({ popupState: 'askPassword' }, this.showPopup);
  });

  onPasswordChange = (model: Object) => {
    this.setState({ password: model.password });
  }

  render() {
    const canResize = this.client != null && this.client.canResize;
    const areaClassName = `display_area_${this.props.graphicsType}`;
    return (
      <>
        <header className={`navbar-pf ${styles.navbar_pf_console}`}>
          <div className={`navbar-header ${styles.navbar_header_console}`}>
            <i className="fa spacewalk-icon-virtual-guest" />
            {this.props.guestName}
          </div>
          <ul className="nav navbar-nav navbar-utility">
            <li>
              {this.props.graphicsType === 'vnc' && (
                <Button
                  title={t("Toggle full size")}
                  icon={this.state.expanded ? 'fa-compress' : 'fa-expand'}
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
        <div
          id="display-area"
          className={`${styles.display_area_console} ${styles[areaClassName]}`}
        >
          <div id="canvas" className={styles.canvas}/>
        </div>
      </>
    );
  }
}

export default hot(GuestsConsole);
