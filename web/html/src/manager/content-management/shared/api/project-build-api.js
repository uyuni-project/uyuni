const React = require('react');
declare function t(msg: string, ...args: Array<any>): string;

type Props = {
  children: Function,
};

type State = {
  message: Object,
  isLoading: boolean,
};

class ProjectBuildApi extends React.Component<Props, State> {

  constructor(props: Props) {
    super(props);
    this.state = {
      message: {},
      isLoading: false,
    };
  }

  buildProject = (project: Object) => {
    this.setState({isLoading: true});
    // Network.post(`/rhn/manager/api/contentmanagement/publishproject`,
    //   JSON.stringify(project), 'application/json').promise
    //   .then((response) => {
    //
    //
    //   }, (xhr) => {
    //     const errMessages = xhr.status === 0
    //       ? [MessagesUtils.error(
    //         t('Request interrupted or invalid response received from the server. Please try again.'),
    //       )]
    //       : [MessagesUtils.error(Network.errorMessageByStatus(xhr.status))];
    //     this.setState({
    //       message: errMessages,
    //       isLoading: false,
    //     });
    //   });
    return new Promise((resolve) => setTimeout(() => { this.setState({isLoading: false}); resolve()}, 2000))
  }

  render() {
    return this.props.children({
      buildProject: this.buildProject,
      message: this.state.message,
      isLoading: this.state.isLoading,
    });
  }
}

export default ProjectBuildApi
