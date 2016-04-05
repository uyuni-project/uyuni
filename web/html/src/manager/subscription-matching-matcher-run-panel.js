"use strict";

const React = require("react");
const Network = require("../utils/network");

const MatcherRunPanel = React.createClass({
  getInitialState: function() {
    return {
      dataAvailable: this.props.dataAvailable,
      latestStart: this.props.initialLatestStart,
      latestEnd: this.props.initialLatestEnd,
      error: false,
    }
  },

  componentWillReceiveProps: function(nextProps) {
    this.setState({
      dataAvailable: nextProps.dataAvailable,
    });

    if (this.state.latestStart == null || nextProps.initialLatestStart >= this.state.latestStart) {
      this.setState({
        latestStart: nextProps.initialLatestStart,
        latestEnd: nextProps.initialLatestEnd,
        error: false,
      });
    }
  },

  onScheduled: function() {
    this.setState({
        latestStart: new Date().toJSON(),
        latestEnd: null,
        error: false,
      }
    );
    this.props.onMatcherRunSchedule();
  },

  onError: function() {
    this.setState({error: true});
  },

  render: function() {
    const matcherDataStatus = this.state.dataAvailable
        ? <div>
            <MatcherTaskDescription />
            <MatcherRunDescription latestStart={this.state.latestStart} latestEnd={this.state.latestEnd} error={this.state.error} />
            <MatcherScheduleButton
              matcherRunning={!this.state.error && this.state.latestStart != null && this.state.latestEnd == null}
              onScheduled={this.onScheduled}
              onError={this.onError}
            />
          </div>
        : <MatcherNoDataDescription />;

    return (
      <div className="row col-md-12">
        <h2>{t("Match data status")}</h2>
        {matcherDataStatus}
      </div>
    );
  }
});

const MatcherRunDescription = (props) => {
  if (props.error) {
    return <div className="text-danger">{t("Could not start a matching run. Please contact your SUSE Manager administrator to make sure the task scheduler is running.")}</div>
  }

  if (props.latestStart == null) {
    return (
      <div>
        {t("No match data is currently available.")}<br/>
        {t("You can also trigger a first run now by clicking the button below.")}
      </div>
    );
  }

  if (props.latestEnd == null) {
    return <div>{t("Matching data is currently being recomputed, it was started {0}.", moment(props.latestStart).fromNow())}</div>;
  }

  return <div>{t("Latest successful match data was computed {0}, you can trigger a new run by clicking the button below.", moment(props.latestEnd).fromNow())}</div>;
}

const MatcherNoDataDescription = () =>
  <div>
    {t("Couldn't retrieve the matcher data from the server. This can happen when the session is expired. Please try to log in again.")}
  </div>
;

const MatcherTaskDescription = () =>
  <div>
    {t("Match data is computed via a task schedule, nightly by default (you can ")}
    <a href="/rhn/admin/BunchDetail.do?label=gatherer-matcher-bunch">{t("change the task schedule from the administration page")}</a>
    {t("). ")}
  </div>
;

const MatcherScheduleButton = React.createClass({
  onClick: function() {
    Network.post("/rhn/manager/subscription-matching/schedule-matcher-run")
      .promise.catch(() => this.props.onError());
    this.props.onScheduled();
  },

  render: function() {
    const buttonClass = "btn spacewalk-btn-margin-vertical " +
      (!this.props.matcherRunning ? "btn-success" : "btn-default");

    return (
      <button
        type="button"
        className={buttonClass}
        disabled={this.props.matcherRunning}
        onClick={this.onClick}
      >
        <i className="fa fa-refresh"></i>{t("Refresh matching data")}
      </button>
    );
  }
});

module.exports = {
  MatcherRunPanel: MatcherRunPanel,
}
