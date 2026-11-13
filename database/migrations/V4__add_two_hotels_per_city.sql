INSERT INTO hotels (name, location, description, nightly_price, room_capacity) VALUES
    ('Palm Cove Hideaway', 'Goa', 'A breezy beachside retreat with leafy paths and open-air dining.', 5800.00, 18),
    ('Monsoon Sands Hotel', 'Goa', 'A relaxed coastal stay with poolside lounging and easy beach access.', 7600.00, 22),
    ('Pine & Peak Lodge', 'Manali', 'A warm mountain lodge with forest trails and valley-facing rooms.', 6100.00, 16),
    ('Snowline Haven', 'Manali', 'A quiet hillside escape with a fireplace lounge and mountain views.', 8400.00, 12),
    ('Harbourlight Suites', 'Mumbai', 'A modern city stay with convenient transport and a rooftop restaurant.', 6900.00, 30),
    ('The Marigold House', 'Mumbai', 'A welcoming urban hotel with a courtyard cafe and comfortable rooms.', 4600.00, 26),
    ('Aravalli Garden Stay', 'Udaipur', 'A peaceful garden property with a terrace overlooking the old city.', 4200.00, 15),
    ('Fateh Lake Retreat', 'Udaipur', 'A lakeside hideaway with sunset views and traditional Rajasthani touches.', 7900.00, 12),
    ('Pink City Haveli', 'Jaipur', 'A restored haveli stay near local markets with a shaded inner courtyard.', 4500.00, 17),
    ('Amber Courtyard Hotel', 'Jaipur', 'A heritage-inspired hotel with a rooftop dining space and garden rooms.', 6300.00, 20);

INSERT INTO hotel_amenities (hotel_id, amenity) VALUES
    (6, 'Beach access'), (6, 'Breakfast'), (6, 'Garden'), (6, 'Wi-Fi'),
    (7, 'Beach access'), (7, 'Restaurant'), (7, 'Swimming pool'), (7, 'Wi-Fi'),
    (8, 'Breakfast'), (8, 'Mountain views'), (8, 'Parking'), (8, 'Wi-Fi'),
    (9, 'Fireplace lounge'), (9, 'Mountain views'), (9, 'Restaurant'), (9, 'Wi-Fi'),
    (10, 'Air conditioning'), (10, 'Rooftop restaurant'), (10, 'Business lounge'), (10, 'Wi-Fi'),
    (11, 'Air conditioning'), (11, 'Cafe'), (11, 'Courtyard'), (11, 'Wi-Fi'),
    (12, 'Garden'), (12, 'Lake views'), (12, 'Rooftop terrace'), (12, 'Wi-Fi'),
    (13, 'Lake views'), (13, 'Restaurant'), (13, 'Swimming pool'), (13, 'Wi-Fi'),
    (14, 'Breakfast'), (14, 'Courtyard'), (14, 'Heritage decor'), (14, 'Wi-Fi'),
    (15, 'Garden'), (15, 'Rooftop terrace'), (15, 'Swimming pool'), (15, 'Wi-Fi');
