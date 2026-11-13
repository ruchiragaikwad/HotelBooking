const MAX_COMPARISON_HOTELS = 3;

function toggleComparedHotel(currentIds, hotelId) {
  if (currentIds.includes(hotelId)) {
    return { ids: currentIds.filter((id) => id !== hotelId), limitReached: false };
  }
  if (currentIds.length >= MAX_COMPARISON_HOTELS) {
    return { ids: currentIds, limitReached: true };
  }
  return { ids: [...currentIds, hotelId], limitReached: false };
}

export { MAX_COMPARISON_HOTELS, toggleComparedHotel };
