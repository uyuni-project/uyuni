// @flow
export type FilterServerType = {
  entityType: string,
  matcher: string,
  id: number,
  name?: string,
  criteriaKey?: string,
  criteriaValue?: string,
  deny?: boolean,
  projects?: Array<string>
}

export type FilterFormType = {
  type: string,
  matcher: string,
  deny?: boolean,
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
}
