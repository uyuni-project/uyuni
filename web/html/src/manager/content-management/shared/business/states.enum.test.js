import statesEnum from "./states.enum";

describe('Testing CLM sources states enum', () => {
  test('findByKey success case', () => {
    expect(statesEnum.findByKey(statesEnum.enum.ATTACHED.key).description).toBe("added");
    expect(statesEnum.findByKey(statesEnum.enum.BUILT.key).description).toBe("built");
  });

  test('findByKey default case', () => {
    expect(statesEnum.findByKey("unexisting").description).toBe();
  });

  test('isDeletion testing', () => {
    expect(statesEnum.isDeletion(statesEnum.enum.DETACHED.key)).toBeTruthy();
    expect(statesEnum.isDeletion(statesEnum.enum.ATTACHED.key)).toBeFalsy();
    expect(statesEnum.isDeletion(statesEnum.enum.EDITED.key)).toBeFalsy();
    expect(statesEnum.isDeletion(statesEnum.enum.BUILT.key)).toBeFalsy();
  });

  test('isEdited testing', () => {
    expect(statesEnum.isEdited(statesEnum.enum.DETACHED.key)).toBeTruthy();
    expect(statesEnum.isEdited(statesEnum.enum.ATTACHED.key)).toBeTruthy();
    expect(statesEnum.isEdited(statesEnum.enum.EDITED.key)).toBeTruthy();
    expect(statesEnum.isEdited(statesEnum.enum.BUILT.key)).toBeFalsy();
  });

})
