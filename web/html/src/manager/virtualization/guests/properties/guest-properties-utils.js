// @flow

function getOrderedDevicesFromModel(model: Object, device: string): Array<string> {
  return Object.keys(model)
    .map(property => property.split('_')[0])
    .filter((property, index, array) => property.startsWith(device) && index === array.indexOf(property))
    .sort((prop1, prop2) => {
      const num1 = Number.parseInt(prop1.substring(device.length), 10);
      const num2 = Number.parseInt(prop2.substring(device.length), 10);
      return num1 - num2;
    });
}

module.exports = {
  getOrderedDevicesFromModel,
};
