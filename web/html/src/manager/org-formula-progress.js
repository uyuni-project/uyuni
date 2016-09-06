'use strict';

const React = require("react");
const ReactDOM = require("react-dom");
const Network = require("../utils/network");
const Messages = require("../components/messages").Messages;
const Panel = require("../components/panel").Panel;

var FormulaProgress = React.createClass({

    getInitialState: function() {
        var st = {
            progress: {},
            messages: []
        };
        this.refreshData();
        return st;
    },

    refreshData: function() {
        console.log("Requesting progress data");
        Network.get("/rhn/manager/api/formula-progress/data").promise.then(data => {
            console.log(data);
            this.setState({
                progress: data
            });
        });
    },

    generateView() {
        var views = [];
        for (var server in this.state.progress) {
            var progress = this.state.progress[server];
            if (progress.hasProgress || progress.isFinished) {
                var percent = (progress.isFinished ? 100 : (progress.currProgress / progress.maxProgress) * 100);
                views.push(
                    <div key={server}>
                        {server + ": "}
                        {progress.status}
                        <div className="progress">
                            <div className="progress-bar" role="progressbar" aria-valuenow={percent} style={{"minWidth": "2em", "width": percent + "%"}}>
                                {progress.isFinished ? "Installation complete" : percent + "%"}
                            </div>
                        </div>
                    </div>
                );
            }
            else {
                views.push(
                    <div key={server}>
                        {server + ": "}
                        <div className="progress progress-bar-striped progress-striped active">
                            <div className="progress-bar progress-bar-striped progress-striped active" role="progressbar" style={{"width": "100%"}}>
                                {progress.status}
                            </div>
                        </div>
                    </div>
                );
            }
        }
        return views;
    },

    render: function() {
        var messages = <Messages items={[{severity: "info", text:
            <p><strong>{t('This is a feature preview')}</strong>: On this page you can see the installation progress of your formulas. We would be glad to receive your feedback via the <a href="https://forums.suse.com/forumdisplay.php?22-SUSE-Manager" target="_blank">{t('forum')}</a>.</p>
        }]}/>;
        if (this.state.messages.length > 0) {
            messages = <Messages items={this.state.messages.map(function(msg) {
                return {severity: "info", text: msg};
            })}/>;
        }
        return (
            <Panel title="Formula Installation Progress" icon="spacewalk-icon-salt-add">
            {messages}
            <a id="request-events-btn" href="#" onClick={this.refreshData} className="btn btn-default">Refresh</a>
            {this.generateView()}
            </Panel>
        );
    }

});

ReactDOM.render(
  <FormulaProgress />,
  document.getElementById('formula-progress')
);
