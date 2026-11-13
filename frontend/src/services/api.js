const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL || '/api').replace(/\/$/, '');

async function request(path, options = {}) {
  let response;
  try {
    response = await fetch(`${apiBaseUrl}${path}`, {
      ...options,
      headers: { Accept: 'application/json', ...options.headers },
    });
  } catch {
    throw new Error('The hotel service could not be reached. Check that the backend is running and try again.');
  }

  if (!response.ok) {
    let message = `The request failed (${response.status}). Please try again.`;
    try {
      const body = await response.json();
      if (body.message) message = body.message;
    } catch {
      // Keep the readable fallback when the server did not return a JSON error.
    }
    throw new Error(message);
  }

  return response.json();
}

function getHotels(location = '') {
  const query = location ? `?location=${encodeURIComponent(location)}` : '';
  return request(`/hotels${query}`);
}

function getHotel(hotelId) {
  return request(`/hotels/${hotelId}`);
}

function getAvailability(hotelId, checkIn, checkOut) {
  const query = new URLSearchParams({ checkIn, checkOut });
  return request(`/hotels/${hotelId}/availability?${query.toString()}`);
}

function postJson(path, body) {
  return request(path, {
    method: 'POST',
    headers: { Accept: 'application/json', 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  });
}

function createBooking(booking) {
  return postJson('/bookings', booking);
}

function lookupBooking(bookingReference, customerEmail) {
  return postJson('/bookings/lookup', { bookingReference, customerEmail });
}

function cancelBooking(bookingReference, customerEmail) {
  return postJson(`/bookings/${encodeURIComponent(bookingReference)}/cancel`, { customerEmail });
}

export { apiBaseUrl, cancelBooking, createBooking, getAvailability, getHotel, getHotels, lookupBooking };
