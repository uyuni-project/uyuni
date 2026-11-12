import { ChangeEvent, Component } from "react";

import { FormGroup, Label } from "components/input";

import Network from "utils/network";

type ClmEnvironment = {
  id: number;
  label: string;
  name: string;
};

type ClmProject = {
  id: number;
  label: string;
  name: string;
  environments: ClmEnvironment[];
};

type Props = {
  actionType: "CLM_BUILD" | "CLM_PROMOTE";
  projectLabel?: string;
  environmentLabel?: string;
  disabled?: boolean;
  onSelectClm: (projectLabel: string, environmentLabel?: string) => void;
};

type State = {
  projects: ClmProject[];
  selectedProjectLabel: string;
  selectedEnvironmentLabel: string;
  loading: boolean;
};

class RecurringClmPicker extends Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      projects: [],
      selectedProjectLabel: props.projectLabel || "",
      selectedEnvironmentLabel: props.environmentLabel || "",
      loading: true,
    };
  }

  componentDidMount() {
    Network.get("/rhn/manager/api/recurringactions/clm/projects")
      .then((projects: ClmProject[]) => {
        this.setState({ projects, loading: false }, () => {
          if (this.props.projectLabel) {
            this.setState({
              selectedProjectLabel: this.props.projectLabel,
              selectedEnvironmentLabel: this.props.environmentLabel || "",
            });
          }
        });
      })
      .catch(() => {
        this.setState({ loading: false });
      });
  }

  componentDidUpdate(prevProps: Props) {
    if (
      prevProps.projectLabel !== this.props.projectLabel ||
      prevProps.environmentLabel !== this.props.environmentLabel
    ) {
      this.setState({
        selectedProjectLabel: this.props.projectLabel || "",
        selectedEnvironmentLabel: this.props.environmentLabel || "",
      });
    }
  }

  handleProjectChange = (e: ChangeEvent<HTMLSelectElement>) => {
    const projectLabel = e.target.value;
    const selectedProject = this.state.projects.find((p) => p.label === projectLabel);
    let environmentLabel = "";

    if (this.props.actionType === "CLM_PROMOTE" && selectedProject?.environments?.length) {
      // Default to first environment or reset
      environmentLabel = selectedProject.environments[0].label;
    }

    this.setState({
      selectedProjectLabel: projectLabel,
      selectedEnvironmentLabel: environmentLabel,
    });

    this.props.onSelectClm(projectLabel, environmentLabel);
  };

  handleEnvironmentChange = (e: ChangeEvent<HTMLSelectElement>) => {
    const environmentLabel = e.target.value;
    this.setState({ selectedEnvironmentLabel: environmentLabel });
    this.props.onSelectClm(this.state.selectedProjectLabel, environmentLabel);
  };

  render() {
    const { actionType, disabled } = this.props;
    const { projects, selectedProjectLabel, selectedEnvironmentLabel, loading } = this.state;
    const isPromote = actionType === "CLM_PROMOTE";
    const selectedProject = projects.find((p) => p.label === selectedProjectLabel);
    const environments = selectedProject?.environments || [];

    return (
      <div className="form-horizontal">
        <FormGroup className="form-group">
          <Label name={t("Content Project")} className="col-sm-3" required />
          <div className="col-sm-6">
            <select
              className="form-control"
              value={selectedProjectLabel}
              onChange={this.handleProjectChange}
              disabled={disabled || loading}
            >
              <option value="">{loading ? t("Loading projects...") : t("-- Select a Project --")}</option>
              {projects.map((p) => (
                <option key={p.label} value={p.label}>
                  {p.name} ({p.label})
                </option>
              ))}
            </select>
          </div>
        </FormGroup>

        {isPromote && (
          <FormGroup className="form-group">
            <Label name={t("Target Environment")} className="col-sm-3" required />
            <div className="col-sm-6">
              <select
                className="form-control"
                value={selectedEnvironmentLabel}
                onChange={this.handleEnvironmentChange}
                disabled={disabled || loading || !selectedProjectLabel}
              >
                <option value="">{t("-- Select an Environment --")}</option>
                {environments.map((env) => (
                  <option key={env.label} value={env.label}>
                    {env.name} ({env.label})
                  </option>
                ))}
              </select>
            </div>
          </FormGroup>
        )}
      </div>
    );
  }
}

export { RecurringClmPicker };
