-- Demo accounts. Every password is 1234.
INSERT INTO users (username, password, email) VALUES ('ahmed', 'yI/MS/ZUCl27zuRwBZ1P0A==:r5GbVDDJbRaSXRI3YOi2sSjcS7PNVI/D+JrU7OKS9Vs=', 'ahmed@iwish.com');
INSERT INTO users (username, password, email) VALUES ('sara', 'yI/MS/ZUCl27zuRwBZ1P0A==:r5GbVDDJbRaSXRI3YOi2sSjcS7PNVI/D+JrU7OKS9Vs=', 'sara@iwish.com');
INSERT INTO users (username, password, email) VALUES ('omar', 'yI/MS/ZUCl27zuRwBZ1P0A==:r5GbVDDJbRaSXRI3YOi2sSjcS7PNVI/D+JrU7OKS9Vs=', 'omar@iwish.com');
INSERT INTO users (username, password, email) VALUES ('laila', 'yI/MS/ZUCl27zuRwBZ1P0A==:r5GbVDDJbRaSXRI3YOi2sSjcS7PNVI/D+JrU7OKS9Vs=', 'laila@iwish.com');
INSERT INTO users (username, password, email) VALUES ('mona', 'yI/MS/ZUCl27zuRwBZ1P0A==:r5GbVDDJbRaSXRI3YOi2sSjcS7PNVI/D+JrU7OKS9Vs=', 'mona@iwish.com');

INSERT INTO friends (user_id_1, user_id_2, status) VALUES (1, 2, 'accepted');
INSERT INTO friends (user_id_1, user_id_2, status) VALUES (1, 3, 'accepted');
INSERT INTO friends (user_id_1, user_id_2, status) VALUES (2, 4, 'accepted');
INSERT INTO friends (user_id_1, user_id_2, status) VALUES (4, 1, 'pending');
INSERT INTO friends (user_id_1, user_id_2, status) VALUES (5, 1, 'pending');

INSERT INTO wishlist_items (user_id, item_id, contributed_amount, status) VALUES (2, 1, 600.0, 'open');
INSERT INTO wishlist_items (user_id, item_id, contributed_amount, status) VALUES (2, 3, 0.0, 'open');
INSERT INTO wishlist_items (user_id, item_id, contributed_amount, status) VALUES (1, 2, 0.0, 'open');
INSERT INTO wishlist_items (user_id, item_id, contributed_amount, status) VALUES (3, 6, 320.0, 'completed');
INSERT INTO wishlist_items (user_id, item_id, contributed_amount, status) VALUES (3, 5, 250.0, 'open');

INSERT INTO contributions (wishlist_item_id, buyer_id, amount) VALUES (1, 1, 600.0);
INSERT INTO contributions (wishlist_item_id, buyer_id, amount) VALUES (4, 1, 320.0);
INSERT INTO contributions (wishlist_item_id, buyer_id, amount) VALUES (5, 1, 250.0);

INSERT INTO notifications (user_id, message) VALUES (1, 'The gift ''Desk Lamp'' for omar is now fully funded. Your share: 320.0 EGP.');
INSERT INTO notifications (user_id, message) VALUES (3, 'Your wish ''Desk Lamp'' has been bought by ahmed (320.0 EGP).');
INSERT INTO notifications (user_id, message) VALUES (1, 'laila sent you a friend request.');
INSERT INTO notifications (user_id, message) VALUES (1, 'mona sent you a friend request.');
