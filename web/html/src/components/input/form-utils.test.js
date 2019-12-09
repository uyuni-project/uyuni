import { flattenModel } from './form-utils';
import { unflattenModel } from './form-utils';

describe('Testing flattenModel', () => {
  test('test copying simple items', () => {
    const tree = {
      name: 'foo',
      type: 'netfs',
      port: 1234
    };
    expect(flattenModel(tree)).toEqual(tree);
  })

  test('test nested items', () => {
    const tree = {
      name: 'foo',
      target: {
        path: '/foo/bar',
        permission: {
          owner: 123,
          group: 456,
          mode: '755',
        }
      }
    };
    expect(flattenModel(tree)).toEqual({
      name: 'foo',
      target_path: '/foo/bar',
      target_permission_owner: 123,
      target_permission_group: 456,
      target_permission_mode: '755',
    });
  })

  test('test array items', () => {
    const tree = {
      name: 'foo',
      hosts: [
        {name: 'one.example.com', port: 1234},
        {name: 'two.example.com', port: 4567},
        {name: 'three.example.com'}
      ]
    };
    expect(flattenModel(tree)).toEqual({
      name: 'foo',
      hosts0_name: 'one.example.com',
      hosts0_port: 1234,
      hosts1_name: 'two.example.com',
      hosts1_port: 4567,
      hosts2_name: 'three.example.com'
    })
  })

  test('test array strings', () => {
    const tree = {
      name: 'foo',
      hosts: ['one.example.com', 'two.example.com', 'three.example.com']
    };
    expect(flattenModel(tree)).toEqual({
      name: 'foo',
      hosts0: 'one.example.com',
      hosts1: 'two.example.com',
      hosts2: 'three.example.com'
    })
  })
});

describe("Test unflattenModel", () => {
  test("test with no array item", () => {
    const flat = {
      name: 'mine',
      target_path: '/foo/bar',
      target_permission_owner: 123,
      target_permission_group: 456
    };

    expect(unflattenModel(flat)).toEqual({
      name: 'mine',
      target: {
        path: '/foo/bar',
        permission: {
          owner: 123,
          group: 456
        }
      }
    })
  })

  test("test with array item", () => {
    const flat = {
      name: 'mine',
      source_hosts0_name: 'one.example.com',
      source_hosts0_port: 123,
      source_hosts1_name: 'two.example.com',
      source_hosts1_port: 456,
      source_hosts2_name: 'three.example.com',
    };

    expect(unflattenModel(flat)).toEqual({
      name: 'mine',
      source: {
        hosts: [
          {name: 'one.example.com', port: 123},
          {name: 'two.example.com', port: 456},
          {name: 'three.example.com'}
        ]
      }
    })
  })
});
