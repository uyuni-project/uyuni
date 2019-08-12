// @flow
export type FilterServerType = {
  entityType: string,
  matcher: string,
  id: number,
  name?: string,
  criteriaKey?: string,
  criteriaValue?: string,
  rule: string,
  projects?: Array<string>
}

export type FilterFormType = {
  type: string,
  matcher: string,
  rule: string,
  id?: number,
  name?: string,
  projects?: Array<string>,
  packageName?: string,
  epoch?: string,
  version?: string,
  release?: string,
  architecture?: string,
  advisoryName?: string,
  criteria?: string,
  issueDate?: Date
}
