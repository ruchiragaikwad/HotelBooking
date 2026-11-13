import { useEffect, useMemo, useRef, useState } from 'react';
import {
  cancelBooking,
  createBooking,
  getAvailability,
  getHotel,
  getHotels,
  lookupBooking,
} from './services/api.js';
import { toggleComparedHotel } from './services/hotelComparison.js';

function localDateString(date = new Date()) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

function tomorrowString() {
  const tomorrow = new Date();
  tomorrow.setDate(tomorrow.getDate() + 1);
  return localDateString(tomorrow);
}

function money(value) {
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    maximumFractionDigits: 0,
  }).format(value);
}

function isDateRangeValid(checkIn, checkOut) {
  return Boolean(checkIn && checkOut && checkIn >= localDateString() && checkOut > checkIn);
}

const HOTEL_IMAGE_FILES = {
  'Aurora Bay Resort': 'AuroraBayResort.jpg',
  'Cedar Ridge Retreat': 'Cedar Ridge.jpg',
  'Citylight Grand': 'CitylightGrand.jpg',
  'Lake Palace View': 'Lake Palace View.jpg',
  'Maharani Courtyard': 'Maharani Courtyard.jpg',
  'Palm Cove Hideaway': 'PalmCoveHideaway.jpg',
  'Monsoon Sands Hotel': 'MonsoonSandHotel.jpg',
  'Pine & Peak Lodge': 'Pine & Peak Lodge.jpg',
  'Snowline Haven': 'Snowline Haven.jpg',
  'Harbourlight Suites': 'HarbourSuites.jpg',
  'The Marigold House': 'Marigold.jpg',
  'Aravalli Garden Stay': 'Aravalli Garden Stay.jpg',
  'Fateh Lake Retreat': 'FatehLakeRetreat.jpg',
  'Pink City Haveli': 'PinkCityHaveli.jpg',
  'Amber Courtyard Hotel': 'Ambar Courtyard Hotel.jpg',
};

function hotelImageUrl(hotelName) {
  const imageFile = HOTEL_IMAGE_FILES[hotelName];
  return imageFile ? encodeURI(`/images/hotels/${imageFile}`) : '';
}

export default function App() {
  const [hotels, setHotels] = useState([]);
  const [comparisonIds, setComparisonIds] = useState([]);
  const [comparisonNotice, setComparisonNotice] = useState('');
  const [location, setLocation] = useState('');
  const [searchLocation, setSearchLocation] = useState('');
  const [checkIn, setCheckIn] = useState(localDateString());
  const [checkOut, setCheckOut] = useState(tomorrowString());
  const [selectedHotelId, setSelectedHotelId] = useState(null);
  const [selectedHotel, setSelectedHotel] = useState(null);
  const [detailLoading, setDetailLoading] = useState(false);
  const [detailError, setDetailError] = useState('');
  const [availability, setAvailability] = useState(null);
  const [listLoading, setListLoading] = useState(true);
  const [listError, setListError] = useState('');
  const [availabilityLoading, setAvailabilityLoading] = useState(false);
  const [availabilityError, setAvailabilityError] = useState('');
  const [dateError, setDateError] = useState('');
  const [customerName, setCustomerName] = useState('');
  const [customerEmail, setCustomerEmail] = useState('');
  const [formNotice, setFormNotice] = useState('');
  const [refreshKey, setRefreshKey] = useState(0);
  const [bookingSubmitting, setBookingSubmitting] = useState(false);
  const [bookingConfirmation, setBookingConfirmation] = useState(null);
  const [lookupReference, setLookupReference] = useState('');
  const [lookupEmail, setLookupEmail] = useState('');
  const [lookupSubmitting, setLookupSubmitting] = useState(false);
  const [lookupError, setLookupError] = useState('');
  const [foundBooking, setFoundBooking] = useState(null);
  const [cancelPrompt, setCancelPrompt] = useState(false);
  const [cancelSubmitting, setCancelSubmitting] = useState(false);
  const [bookingActionMessage, setBookingActionMessage] = useState('');
  const [availabilityRefreshKey, setAvailabilityRefreshKey] = useState(0);
  const bookingRequestInFlight = useRef(false);
  const comparisonHotels = useMemo(
    () => comparisonIds.map((id) => hotels.find((hotel) => hotel.id === id)).filter(Boolean),
    [comparisonIds, hotels],
  );

  const nights = useMemo(() => {
    if (!isDateRangeValid(checkIn, checkOut)) return 0;
    const start = new Date(`${checkIn}T00:00:00`);
    const end = new Date(`${checkOut}T00:00:00`);
    return Math.round((end - start) / 86400000);
  }, [checkIn, checkOut]);

  useEffect(() => {
    let active = true;
    setListLoading(true);
    setListError('');
    getHotels(searchLocation)
      .then((data) => {
        if (active) setHotels(data);
      })
      .catch((error) => {
        if (active) setListError(error.message);
      })
      .finally(() => {
        if (active) setListLoading(false);
      });
    return () => { active = false; };
  }, [searchLocation, refreshKey]);

  useEffect(() => {
    if (!selectedHotelId) {
      setSelectedHotel(null);
      setDetailError('');
      return undefined;
    }
    let active = true;
    setSelectedHotel(null);
    setDetailLoading(true);
    setDetailError('');
    getHotel(selectedHotelId)
      .then((data) => { if (active) setSelectedHotel(data); })
      .catch((error) => { if (active) setDetailError(error.message); })
      .finally(() => { if (active) setDetailLoading(false); });
    return () => { active = false; };
  }, [selectedHotelId]);

  useEffect(() => {
    if (!selectedHotelId || !isDateRangeValid(checkIn, checkOut)) {
      setAvailability(null);
      setAvailabilityError('');
      setAvailabilityLoading(false);
      return undefined;
    }
    let active = true;
    setAvailability(null);
    setAvailabilityLoading(true);
    setAvailabilityError('');
    getAvailability(selectedHotelId, checkIn, checkOut)
      .then((data) => {
        if (active) setAvailability(data);
      })
      .catch((error) => {
        if (active) setAvailabilityError(error.message);
      })
      .finally(() => {
        if (active) setAvailabilityLoading(false);
      });
    return () => { active = false; };
  }, [selectedHotelId, checkIn, checkOut, availabilityRefreshKey]);

  function submitSearch(event) {
    event.preventDefault();
    if (!checkIn || !checkOut) {
      setDateError('Choose both check-in and check-out dates.');
      return;
    }
    if (!isDateRangeValid(checkIn, checkOut)) {
      setDateError(checkIn < localDateString()
        ? 'Check-in cannot be before today.'
        : 'Check-out must be after check-in.');
      return;
    }
    setDateError('');
    setSearchLocation(location.trim());
  }

  function submitBookingForm(event) {
    event.preventDefault();
    setFormNotice('');
    setBookingConfirmation(null);
    if (!selectedHotel || selectedHotel.id !== selectedHotelId) {
      setFormNotice('Choose a hotel before booking.');
      return;
    }
    if (!isDateRangeValid(checkIn, checkOut)) {
      setFormNotice('Choose a valid check-in and check-out date before booking.');
      return;
    }
    if (!availability?.available) {
      setFormNotice(availabilityLoading ? 'Please wait while we check room availability.' : 'There are no rooms available for these dates.');
      return;
    }
    if (!customerName.trim() || !customerEmail.trim()) {
      setFormNotice('Enter your name and email to continue.');
      return;
    }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(customerEmail)) {
      setFormNotice('Enter a valid email address.');
      return;
    }
    if (bookingRequestInFlight.current) return;
    bookingRequestInFlight.current = true;
    setBookingSubmitting(true);
    createBooking({
      hotelId: selectedHotelId,
      customerName: customerName.trim(),
      customerEmail: customerEmail.trim(),
      checkIn,
      checkOut,
    })
      .then((result) => {
        setBookingConfirmation(result);
        setFormNotice('');
        setAvailabilityRefreshKey((value) => value + 1);
      })
      .catch((error) => setFormNotice(error.message))
      .finally(() => {
        bookingRequestInFlight.current = false;
        setBookingSubmitting(false);
      });
  }

  function submitLookup(event) {
    event.preventDefault();
    setLookupError('');
    setFoundBooking(null);
    setBookingActionMessage('');
    setCancelPrompt(false);
    if (!lookupReference.trim() || !lookupEmail.trim()) {
      setLookupError('Enter both the booking reference and the email used for the booking.');
      return;
    }
    setLookupSubmitting(true);
    lookupBooking(lookupReference.trim(), lookupEmail.trim())
      .then((booking) => setFoundBooking(booking))
      .catch((error) => setLookupError(error.message))
      .finally(() => setLookupSubmitting(false));
  }

  function confirmCancellation() {
    if (!foundBooking) return;
    setCancelSubmitting(true);
    setLookupError('');
    setBookingActionMessage('');
    cancelBooking(foundBooking.bookingReference, lookupEmail.trim())
      .then((booking) => {
        setFoundBooking(booking);
        setCancelPrompt(false);
        setBookingActionMessage(booking.status === 'CANCELLED'
          ? 'Booking cancelled. The room is available again for these dates.'
          : 'The booking status was updated.');
        setAvailabilityRefreshKey((value) => value + 1);
      })
      .catch((error) => setLookupError(error.message))
      .finally(() => setCancelSubmitting(false));
  }

  function updateDates(update) {
    update();
    setDateError('');
    setFormNotice('');
    setBookingConfirmation(null);
  }

  function toggleComparison(hotel) {
    setComparisonNotice('');
    const result = toggleComparedHotel(comparisonIds, hotel.id);
    if (result.limitReached) {
      setComparisonNotice('You can compare up to three stays. Remove one to add another.');
      return;
    }
    setComparisonIds(result.ids);
  }

  return (
    <div className="site-shell">
      <header className="topbar">
        <a className="brand" href="#top" aria-label="Staywell home">
          <span className="brand-mark" aria-hidden="true">S</span>
          <span>staywell<span className="brand-dot">.</span></span>
        </a>
        <nav className="top-nav" aria-label="Main navigation">
          <a href="#stays">Explore stays</a>
          <span className="nav-note">Thoughtful stays, easy booking</span>
        </nav>
      </header>

      <main id="top">
        <section className="hero" aria-labelledby="hero-title">
          <div className="hero-copy">
            <p className="eyebrow"><span className="eyebrow-line" /> FIND YOUR PLACE</p>
            <h1 id="hero-title">A little closer<br />to <em>somewhere</em>.</h1>
            <p className="hero-description">Find a stay that feels right. Search our handpicked hotels and make room for a better getaway.</p>
          </div>
          <div className="hero-stamp" aria-hidden="true">
            <span>GOOD DAYS</span><span>START HERE</span><b>✳</b>
          </div>
          <form className="search-panel" onSubmit={submitSearch} noValidate>
            <label className="search-field destination-field">
              <span className="field-icon" aria-hidden="true">⌖</span>
              <span className="field-content"><span className="field-label">DESTINATION</span>
                <input value={location} onChange={(event) => setLocation(event.target.value)} placeholder="Where to? Try Goa" aria-label="Search by location" />
              </span>
            </label>
            <label className="search-field">
              <span className="field-icon calendar-icon" aria-hidden="true">▦</span>
              <span className="field-content"><span className="field-label">CHECK IN</span>
                <input type="date" min={localDateString()} value={checkIn} onChange={(event) => updateDates(() => setCheckIn(event.target.value))} aria-label="Check-in date" />
              </span>
            </label>
            <label className="search-field">
              <span className="field-icon calendar-icon" aria-hidden="true">▦</span>
              <span className="field-content"><span className="field-label">CHECK OUT</span>
                <input type="date" min={checkIn || localDateString()} value={checkOut} onChange={(event) => updateDates(() => setCheckOut(event.target.value))} aria-label="Check-out date" />
              </span>
            </label>
            <button className="search-button" type="submit"><span>Search stays</span><span aria-hidden="true">↗</span></button>
            {dateError && <p className="search-error" role="alert">{dateError}</p>}
          </form>
          <div className="hero-bottomline"><span>CURATED FOR YOUR NEXT CHAPTER</span><span>SCROLL TO EXPLORE ↓</span></div>
        </section>

        <section className="stays-section" id="stays" aria-labelledby="stays-title">
          <div className="section-heading">
            <div><p className="eyebrow">A GOOD PLACE TO BEGIN</p><h2 id="stays-title">Stays worth the trip<span>.</span></h2></div>
            <p className="section-aside">Small escapes. Big exhale.<br />Find your kind of away.</p>
          </div>

          {listLoading && <div className="state-panel" role="status"><span className="spinner" /> Finding stays for you…</div>}
          {listError && <div className="state-panel error-panel" role="alert"><strong>We couldn’t load the stays.</strong><span>{listError}</span><button className="text-button" onClick={() => setRefreshKey((value) => value + 1)}>Try again</button></div>}
          {!listLoading && !listError && hotels.length === 0 && <div className="state-panel"><strong>No stays found for “{searchLocation}”.</strong><span>Try another destination or clear your search.</span><button className="text-button" onClick={() => { setLocation(''); setSearchLocation(''); }}>Show all stays</button></div>}

          {!listLoading && !listError && hotels.length > 0 && <div className="hotel-grid">
            {hotels.map((hotel, index) => (
              <article className={`hotel-card ${selectedHotelId === hotel.id ? 'hotel-card-selected' : ''}`} key={hotel.id}>
                <div className={`hotel-art art-${index % 5}`}>
                  <img className="hotel-photo" src={hotelImageUrl(hotel.name)} alt={`${hotel.name} in ${hotel.location}`} onError={(event) => { event.currentTarget.hidden = true; }} />
                  <span className="art-number">0{index + 1}</span><span className="art-caption">{hotel.location}</span>
                </div>
                <div className="hotel-card-body">
                  <div className="card-kicker"><span>{hotel.location}</span><span className="rating-mark">✳ STAYWELL PICK</span></div>
                  <h3>{hotel.name}</h3>
                  <p className="hotel-description">{hotel.description}</p>
                  <div className="amenity-list" aria-label="Amenities">
                    {hotel.amenities.slice(0, 3).map((amenity) => <span key={amenity}>{amenity}</span>)}
                    {hotel.amenities.length > 3 && <span>+{hotel.amenities.length - 3}</span>}
                  </div>
                  <div className="card-footer">
                    <p className="price"><strong>{money(hotel.nightlyPrice)}</strong><span> / night</span></p>
                    <div className="card-actions">
                      <button className="compare-toggle" type="button" onClick={() => toggleComparison(hotel)} aria-pressed={comparisonIds.includes(hotel.id)}>
                        {comparisonIds.includes(hotel.id) ? 'Added to compare' : 'Compare'}
                      </button>
                      <button className="card-link" type="button" onClick={() => { setSelectedHotelId(selectedHotelId === hotel.id ? null : hotel.id); setFormNotice(''); setBookingConfirmation(null); }} aria-expanded={selectedHotelId === hotel.id}>
                        {selectedHotelId === hotel.id ? 'Close details' : 'View stay'} <span aria-hidden="true">→</span>
                      </button>
                    </div>
                  </div>
                </div>
              </article>
            ))}
          </div>}

          {!listLoading && !listError && <section className="comparison-section" aria-labelledby="comparison-title">
            <div className="comparison-heading">
              <div><p className="eyebrow">MAKE THE CHOICE THAT FITS</p><h2 id="comparison-title">Compare stays<span>.</span></h2>
                <p>Select up to three stays to compare their location, amenities and nightly price side by side.</p></div>
              <span className="comparison-count" aria-live="polite">{comparisonHotels.length} of 3 selected</span>
            </div>
            {comparisonNotice && <p className="comparison-notice" role="status">{comparisonNotice}</p>}
            {comparisonHotels.length === 0 ? <div className="comparison-empty">Choose “Compare” on any hotel card to start your comparison.</div> : <>
              <div className="comparison-table-wrap">
                <table className="comparison-table">
                  <caption className="sr-only">Hotel comparison by location, nightly price and amenities</caption>
                  <thead><tr><th scope="col">Stay</th>{comparisonHotels.map((hotel) => <th scope="col" key={hotel.id}>
                    <span className="comparison-hotel-name">{hotel.name}</span>
                    <button className="comparison-remove" type="button" onClick={() => toggleComparison(hotel)} aria-label={`Remove ${hotel.name} from comparison`}>Remove</button>
                  </th>)}</tr></thead>
                  <tbody>
                    <tr><th scope="row">Location</th>{comparisonHotels.map((hotel) => <td key={hotel.id}>{hotel.location}</td>)}</tr>
                    <tr><th scope="row">Nightly price</th>{comparisonHotels.map((hotel) => <td key={hotel.id}><strong>{money(hotel.nightlyPrice)}</strong> / night</td>)}</tr>
                    <tr><th scope="row">Amenities</th>{comparisonHotels.map((hotel) => <td key={hotel.id}><ul>{hotel.amenities.map((amenity) => <li key={amenity}>{amenity}</li>)}</ul></td>)}</tr>
                  </tbody>
                </table>
              </div>
              <button className="comparison-clear" type="button" onClick={() => { setComparisonIds([]); setComparisonNotice(''); }}>Clear comparison</button>
            </>}
          </section>}
          {selectedHotelId && <section className="detail-panel" aria-labelledby="detail-title">
            <div className="detail-heading">
              <div><p className="eyebrow">YOUR DATES, YOUR STAY</p><h2 id="detail-title">{selectedHotel?.name || 'Hotel details'}</h2><p>{selectedHotel ? `${selectedHotel.location} · ${nights || '—'} ${nights === 1 ? 'night' : 'nights'}` : 'Loading hotel information…'}</p></div>
              <button type="button" className="close-button" onClick={() => setSelectedHotelId(null)} aria-label="Close hotel details">×</button>
            </div>
            {detailLoading && <p className="availability-message" role="status"><span className="spinner" /> Loading hotel details…</p>}
            {detailError && <p className="inline-error" role="alert">{detailError}</p>}
            {!isDateRangeValid(checkIn, checkOut) && <p className="inline-error" role="alert">Choose a valid date range above to check room availability.</p>}
            {availabilityLoading && <p className="availability-message" role="status"><span className="spinner" /> Checking availability…</p>}
            {availabilityError && <p className="inline-error" role="alert">{availabilityError}</p>}
            {availability && <div className={`availability-banner ${availability.available ? 'available' : 'sold-out'}`} role="status">
              <span className="availability-symbol">{availability.available ? '✓' : '!'}</span>
              <span><strong>{availability.available ? `${availability.availableRooms} rooms available` : 'No rooms available for these dates'}</strong><small>{availability.bookedRooms} of {availability.roomCapacity} rooms reserved · {checkIn} to {checkOut}</small></span>
            </div>}
            {selectedHotel?.id === selectedHotelId && <div className="detail-columns">
              <div className="detail-about"><h3>A stay made for slowing down.</h3><p>{selectedHotel.description}</p><div className="detail-amenities"><span className="field-label">THE GOOD STUFF</span><div>{selectedHotel.amenities.map((amenity) => <span key={amenity}>{amenity}</span>)}</div></div></div>
              <form className="booking-card" onSubmit={submitBookingForm} noValidate>
                <div className="booking-price"><span>YOUR ESTIMATED TOTAL</span><strong>{nights ? money(selectedHotel.nightlyPrice * nights) : '—'}</strong><small>{nights ? `${money(selectedHotel.nightlyPrice)} × ${nights} ${nights === 1 ? 'night' : 'nights'}` : 'Choose valid dates to see an estimate'}</small></div>
                <p className="estimate-note">Price estimate only. The backend calculates the final total when a booking is submitted.</p>
                <label className="form-label">YOUR NAME<input value={customerName} onChange={(event) => { setCustomerName(event.target.value); setFormNotice(''); }} autoComplete="name" placeholder="e.g. Asha Rao" /></label>
                <label className="form-label">EMAIL ADDRESS<input type="email" value={customerEmail} onChange={(event) => { setCustomerEmail(event.target.value); setFormNotice(''); }} autoComplete="email" placeholder="you@example.com" /></label>
                <button className="booking-button" type="submit" disabled={bookingSubmitting || bookingConfirmation?.hotelId === selectedHotelId} aria-busy={bookingSubmitting}>
                  {bookingSubmitting ? 'Booking…' : bookingConfirmation?.hotelId === selectedHotelId ? 'Booking confirmed' : 'Reserve stay'} <span aria-hidden="true">→</span>
                </button>
                {formNotice && <p className="form-notice form-error" role="alert">{formNotice}</p>}
              </form>
            </div>}
            {bookingConfirmation?.hotelId === selectedHotelId && <div className="confirmation-card" role="status" aria-live="polite">
              <div className="confirmation-icon" aria-hidden="true">✓</div>
              <div className="confirmation-content"><p className="eyebrow">YOU’RE ALL SET</p><h3>Stay confirmed</h3><p>Your booking is confirmed. Keep this reference to look it up or cancel later.</p>
                <dl className="confirmation-details"><div><dt>BOOKING REFERENCE</dt><dd>{bookingConfirmation.bookingReference}</dd></div><div><dt>CONFIRMED TOTAL</dt><dd>{money(bookingConfirmation.totalAmount)}</dd></div><div><dt>STAY</dt><dd>{bookingConfirmation.checkIn} to {bookingConfirmation.checkOut} · {bookingConfirmation.nights} {bookingConfirmation.nights === 1 ? 'night' : 'nights'}</dd></div></dl>
              </div>
            </div>}
          </section>}
        </section>

        <section className="manage-section" aria-labelledby="manage-title">
          <div className="section-heading manage-heading"><div><p className="eyebrow">YOUR TRIP, WHENEVER YOU NEED IT</p><h2 id="manage-title">Manage a booking<span>.</span></h2></div><p className="section-aside">Your reference and email<br />are your key to the details.</p></div>
          <div className="manage-panel">
            <div className="manage-intro"><span className="manage-icon" aria-hidden="true">↗</span><h3>Already booked with us?</h3><p>Enter the booking reference from your confirmation and the email used when booking.</p></div>
            <form className="lookup-form" onSubmit={submitLookup} noValidate>
              <label className="form-label">BOOKING REFERENCE<input value={lookupReference} onChange={(event) => { setLookupReference(event.target.value); setLookupError(''); setFoundBooking(null); setBookingActionMessage(''); setCancelPrompt(false); }} autoComplete="off" placeholder="e.g. A1B2C3D4" /></label>
              <label className="form-label">BOOKING EMAIL<input type="email" value={lookupEmail} onChange={(event) => { setLookupEmail(event.target.value); setLookupError(''); setFoundBooking(null); setBookingActionMessage(''); setCancelPrompt(false); }} autoComplete="email" placeholder="you@example.com" /></label>
              <button className="booking-button lookup-button" type="submit" disabled={lookupSubmitting} aria-busy={lookupSubmitting}>{lookupSubmitting ? 'Looking up…' : 'Find my booking'} <span aria-hidden="true">→</span></button>
            </form>
            {lookupError && <p className="lookup-error" role="alert">{lookupError}</p>}
            {foundBooking && <article className="booking-result" aria-live="polite">
              <div className="result-topline"><div><span className="field-label">BOOKING REFERENCE</span><strong>{foundBooking.bookingReference}</strong></div><span className={`booking-status ${foundBooking.status === 'CANCELLED' ? 'status-cancelled' : ''}`}>{foundBooking.status === 'CANCELLED' ? 'Cancelled' : 'Confirmed'}</span></div>
              <h3>{foundBooking.hotelName}</h3>
              <dl className="result-details"><div><dt>GUEST</dt><dd>{foundBooking.customerName}</dd></div><div><dt>DATES</dt><dd>{foundBooking.checkIn} – {foundBooking.checkOut}</dd></div><div><dt>NIGHTS</dt><dd>{foundBooking.nights}</dd></div><div><dt>TOTAL</dt><dd>{money(foundBooking.totalAmount)}</dd></div></dl>
              {foundBooking.status === 'CONFIRMED' && !cancelPrompt && <button className="cancel-link" type="button" onClick={() => setCancelPrompt(true)}>Cancel this booking</button>}
              {cancelPrompt && <div className="cancel-confirm" role="group" aria-label="Confirm booking cancellation"><p>Cancel this booking? The room will become available for these dates.</p><div><button className="cancel-confirm-button" type="button" onClick={confirmCancellation} disabled={cancelSubmitting}>{cancelSubmitting ? 'Cancelling…' : 'Yes, cancel booking'}</button><button className="cancel-back-button" type="button" onClick={() => setCancelPrompt(false)} disabled={cancelSubmitting}>Keep booking</button></div></div>}
              {bookingActionMessage && <p className="booking-action-message" role="status">{bookingActionMessage}</p>}
            </article>}
          </div>
        </section>
      </main>
      <footer className="footer"><a className="brand" href="#top"><span className="brand-mark" aria-hidden="true">S</span><span>staywell<span className="brand-dot">.</span></span></a><p>Find your kind of away.</p><span>HOTEL BOOKING PORTAL · MADE FOR GOOD DAYS</span></footer>
    </div>
  );
}
