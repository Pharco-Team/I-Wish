CREATE TABLE users (
    user_id INT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100)
);

CREATE TABLE items (
    item_id INT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    item_name VARCHAR(100) NOT NULL,
    item_description VARCHAR(255),
    price DOUBLE NOT NULL
);

CREATE TABLE friends (
    user_id_1 INT NOT NULL REFERENCES users(user_id),
    user_id_2 INT NOT NULL REFERENCES users(user_id),
    status VARCHAR(20) NOT NULL DEFAULT 'pending',
    PRIMARY KEY (user_id_1, user_id_2)
);

CREATE TABLE wishlist_items (
    wishlist_item_id INT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(user_id),
    item_id INT NOT NULL REFERENCES items(item_id),
    contributed_amount DOUBLE NOT NULL DEFAULT 0.0,
    status VARCHAR(20) NOT NULL DEFAULT 'open',
    is_deleted SMALLINT NOT NULL DEFAULT 0
);

CREATE TABLE contributions (
    contribution_id INT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    wishlist_item_id INT NOT NULL REFERENCES wishlist_items(wishlist_item_id),
    buyer_id INT NOT NULL REFERENCES users(user_id),
    amount DOUBLE NOT NULL
);

CREATE TABLE notifications (
    notification_id INT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(user_id),
    message VARCHAR(255) NOT NULL,
    is_read SMALLINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO items (item_name, item_description, price) VALUES ('Wireless Headphones', 'Bluetooth over-ear headphones', 1800.0);
INSERT INTO items (item_name, item_description, price) VALUES ('Mechanical Keyboard', 'RGB mechanical keyboard, blue switches', 1250.0);
INSERT INTO items (item_name, item_description, price) VALUES ('Smart Watch', 'Fitness tracking smart watch', 2400.0);
INSERT INTO items (item_name, item_description, price) VALUES ('Coffee Maker', 'Drip coffee maker, 1.5 litre', 950.0);
INSERT INTO items (item_name, item_description, price) VALUES ('Backpack', 'Laptop backpack, water resistant', 700.0);
INSERT INTO items (item_name, item_description, price) VALUES ('Desk Lamp', 'LED desk lamp with dimmer', 320.0);
