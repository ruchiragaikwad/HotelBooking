INSERT INTO hotels (name, location, description, nightly_price, room_capacity) VALUES
    ('Aurora Bay Resort', 'Goa', 'A relaxed coastal resort close to the beach, with a quiet garden courtyard.', 6500.00, 24),
    ('Cedar Ridge Retreat', 'Manali', 'A mountain lodge with pine forest views and easy access to walking trails.', 7200.00, 14),
    ('Citylight Grand', 'Mumbai', 'A comfortable city hotel near central business and cultural districts.', 5400.00, 38),
    ('Lake Palace View', 'Udaipur', 'A lakeside stay with a rooftop terrace and views of the old city.', 4800.00, 18),
    ('Maharani Courtyard', 'Jaipur', 'A heritage-inspired courtyard hotel near the historic pink city markets.', 3900.00, 20);

INSERT INTO hotel_amenities (hotel_id, amenity) VALUES
    (1, 'Beach access'), (1, 'Breakfast'), (1, 'Swimming pool'), (1, 'Wi-Fi'),
    (2, 'Breakfast'), (2, 'Mountain views'), (2, 'Parking'), (2, 'Wi-Fi'),
    (3, 'Air conditioning'), (3, 'Business lounge'), (3, 'Gym'), (3, 'Wi-Fi'),
    (4, 'Lake views'), (4, 'Rooftop terrace'), (4, 'Restaurant'), (4, 'Wi-Fi'),
    (5, 'Breakfast'), (5, 'Courtyard'), (5, 'Parking'), (5, 'Wi-Fi');
