type Project = {
  /** Project label */
  left: string;
  /** Project name */
  right: string;
};

export type FilterServerType = {
  entityType: string;
  matcher: string;
  id: number;
  name: string;
  criteriaKey: string;
  criteriaValue: string;
  rule: string;
  projects?: Array<Project>;
};

export type FilterFormType = {
  type: string;
  matcher?: string;
  rule: string;
  id?: number;
  filter_name: string;
  projects?: Array<Project>;
  packageName?: string;
  epoch?: string;
  version?: string;
  release?: string;
  architecture?: string;
  advisoryName?: string;
  advisoryType?: string;
  synopsis?: string;
  criteria?: string;
  issueDate?: Date;
  moduleName?: string;
  moduleStream?: string;
  labelPrefix?: string;
  kernelId?: number;
};
