'use strict';

const React = require("react");
const ReactDOM = require("react-dom");

const {AsyncButton, Button} = require("../components/buttons");
const Network = require("../utils/network");

class DeleteSystem extends React.Component {

  handleDelete = () => {

  }

  render() {
    return (<AsyncButton id="btn-danger"
      action={this.handleDelete}
      name={t("Delete Profile")}/>);
  }

}
