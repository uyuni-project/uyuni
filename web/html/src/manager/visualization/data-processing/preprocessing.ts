import * as d3 from "d3";

/*
Preprocessors for the data retrieved from server

The task of the preprocessor is to convert flat data (list of objects with id and
parentId property) into a structure that can be further processed by d3 tree component.
*/

// Simple preprocessor
//
// uses d3.stratify to prepare the data
//
// input: array of objects containing id and parentId attributes,
// ids are unique and single object (root) has parentId == null
function stratifyPreprocessor(data?: any) {
  data = data || [{ id: "root", name: "Root", parentId: null }];

  function instance() {
    return d3.stratify()(data);
  }

  instance.data = function (d) {
    return arguments.length ? ((data = d), instance) : data;
  };

  return instance;
}

// Grouping preprocessor
//
// organize data in hierarchical groups
//
// input:
// - data:
//  - must contain a single element with parentId==null (root)
//  - non-root elements intended for grouping must contain 'managed_groups'
//  attribute (grouping by managed_groups is only possible at the moment,
//  extension to work with any attribute of the element is easy).
// - groupingConfiguration: nested array, for example
//    [[['devel'],['qa']], // 1st level of grouping
//     [['sles'],['nbg'],['rhel', 'prg']]]  // 2nd level of grouping
//  Such array will lead to division of systems given in the 'data' according to
//  their groups into a tree with depth 4, where:
//   - the root node (depth=0) represents SUSE Manager
//   - the non-leaf nodes represent the grouping, e.g.:
//    - nodes with the depth=1 represent dividing systems into 2 groups:
//     - 1. those belonging to the 'devel' group
//     - 2. those belonging to the 'qa' group
//    - nodes with the depth=2 represent further dividing systems into 2 groups:
//     - 1. those belonging to the 'sles' AND 'prg' group
//     - 2. those belonging to the 'rhel' AND 'prg' group
//   - leaf nodes (systems) are attached ONLY to the nodes with depth=3
//   according to their system groups
//
// Example tree based on the groupingConfiguration above:
//
//                                ||
//                    ===========ROOT==========
//                   /                         \
//              devel                           qa
//            /      \                        /      \
// 'sles'AND'nbg'  'rhel'AND'prg'    'sles'AND'nbg' 'rhel'AND'prg'
//        |              |                  |              |
//  [systems in     [systems in          .......        .......
//   groups devel,   groups devel,
//   sles and nbg]   rhel and prg]
//
function groupingPreprocessor(data?: any, groupingConfiguration?: any) {
  data = data || [{ id: "root", name: "Root", parentId: null }];
  groupingConfiguration = groupingConfiguration || [];

  // function representing instance,
  // calling it causes refresh of output data
  function instance() {
    const root = data.filter((d) => d.parentId == null)[0];
    const leaves = data.filter((d) => d.parentId != null);

    const groupElems = makeGroups(
      root,
      groupingConfiguration.filter((criterion) => criterion.length > 0)
    );
    const allElems = [root] // root
      .concat(groupElems) // inner nodes (represantation of groups)
      .concat(
        groupData(
          leaves,
          groupElems.filter((e) => e.isLeafGroup),
          root
        )
      ); // systems partitioned by groups
    return d3.stratify()(allElems);
  }

  // getter/setter for data
  instance.data = function (d) {
    return arguments.length ? ((data = d), instance) : data;
  };

  // getter/setter for groupingConfiguration
  instance.groupingConfiguration = function (gs) {
    return arguments.length ? ((groupingConfiguration = gs), instance) : groupingConfiguration;
  };

  // Recursively turn the multi-level group configuration into group elements so
  // that they can be consumed and displayed by d3 hierarchy (i.e. list of
  // elements with id and parentId - flat representation of a tree).
  //
  // There is no actual data about systems involved in this point!
  //
  // input:
  //  - groupingConfiguration: grouping configuration [
  //    [['group1'],['group2']], // 1st level of grouping
  //    [['group3'],['group4'],['group5, group6']]  // 2nd level of grouping
  //  ]
  //  - par: parent element for the first level of groups
  //
  // output: list with 6 elements - representation of the group hierarchy:
  //  - 1st level represents splitting data between group1 and group2
  //  - 2nd level represents further splitting in 3 categories (corresponding to
  //   - 1: group3
  //   - 2: group4
  //   - 3: group5 AND group6 at the same time
  //  - example element: {
  //    "id":"root-group1-group5,group6",
  //    "parentId":"root-group1",
  //    "groups":["group1,group5,group6"],
  //    "isLeafGroup":true
  //  }
  //
  function makeGroups(par, grpCfg) {
    if (grpCfg.length === 0) {
      return [];
    }

    const fstCfg = grpCfg[0];
    const rstCfg = grpCfg.slice(1);

    return fstCfg
      .map((g) => {
        const newId = par.id + "-" + g.toString();
        const groups = (par.groups || []).concat(g);
        const elem = {
          id: newId,
          parentId: par.id,
          name: groups[groups.length - 1] || "no group",
          type: "group",
          groups: groups,
          isLeafGroup: rstCfg.length === 0,
        };

        return [elem].concat(makeGroups(elem, rstCfg));
      })
      .reduce((v1, v2) => v1.concat(v2), []);
  }

  const NO_GROUP_LABEL = "** NO GROUP **";

  // Creates groups of the given data (systems) according to the the
  // groupCriterion parameter.
  //
  // input:
  //  - data: list of objects with 'id' and 'managed_groups' attributes (list
  //  of groups the system belongs to)
  //  - groupCriterion: list of elements (as produced by makeGroups) with 'id'
  //  and 'groups' attributes
  //  - root: the "fallback parent" for the data in case groupCriterion is
  //  empty
  function groupData(data, groupCriterion, root) {
    // Helper function: does the superset contain all elements from sub?
    let containsAll = function (superset, sub) {
      return sub.map((e) => superset.includes(e)).reduce((v1, v2) => v1 && v2, true);
    };

    // corner case - no groups -> attach all elements to the root
    if ((groupCriterion || []).length === 0) {
      groupCriterion = [root];
    }

    return groupCriterion
      .map((gc) =>
        data
          .filter((d) => containsAll(d.managed_groups || [NO_GROUP_LABEL], gc.groups || []))
          .map((d) => Object.assign({}, d, { id: d.id + "-" + gc.id, parentId: gc.id }))
      )
      .reduce((v1, v2) => v1.concat(v2), []);
  }

  return instance;
}

export { groupingPreprocessor as grouping, stratifyPreprocessor as stratify };
