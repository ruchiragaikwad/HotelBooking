import assert from 'node:assert/strict';
import test from 'node:test';
import { MAX_COMPARISON_HOTELS, toggleComparedHotel } from './hotelComparison.js';

test('adds a hotel to an empty comparison', () => {
  assert.deepEqual(toggleComparedHotel([], 12), { ids: [12], limitReached: false });
});

test('removes a hotel that is already selected', () => {
  assert.deepEqual(toggleComparedHotel([12, 15], 12), { ids: [15], limitReached: false });
});

test('allows at most three hotels', () => {
  const selected = [12, 15, 18];
  assert.equal(MAX_COMPARISON_HOTELS, 3);
  assert.deepEqual(toggleComparedHotel(selected, 20), { ids: selected, limitReached: true });
});

test('can add another hotel after one is removed', () => {
  const afterRemoval = toggleComparedHotel([12, 15, 18], 15).ids;
  assert.deepEqual(toggleComparedHotel(afterRemoval, 20), { ids: [12, 18, 20], limitReached: false });
});
