drop database distributed_system;
CREATE DATABASE distributed_system;
USE distributed_system;
-- Create customer table
CREATE TABLE customer (
                          customer_id INT AUTO_INCREMENT PRIMARY KEY,
                          customer_name VARCHAR(100) NOT NULL,
                          phone VARCHAR(20) NOT NULL
);

-- Create branch table
CREATE TABLE branch (
                        branch_id INT AUTO_INCREMENT PRIMARY KEY,
                        branch_name VARCHAR(100) NOT NULL
);

-- Create drink table
CREATE TABLE drink (
                       drink_id INT AUTO_INCREMENT PRIMARY KEY,
                       drink_name VARCHAR(100) NOT NULL,
                       price DECIMAL(10, 2) NOT NULL
);

-- Create order table (use backticks because 'order' is a reserved keyword)
CREATE TABLE `order` (
                         order_id INT AUTO_INCREMENT PRIMARY KEY,
                         customer_id INT NOT NULL,
                         branch_id INT NOT NULL,
                         order_date DATETIME DEFAULT CURRENT_TIMESTAMP,
                         FOREIGN KEY (customer_id) REFERENCES customer(customer_id),
                         FOREIGN KEY (branch_id) REFERENCES branch(branch_id)
);

-- Create orderitem table
CREATE TABLE orderitem (
                           orderitem_id INT AUTO_INCREMENT PRIMARY KEY,
                           order_id INT NOT NULL,
                           drink_id INT NOT NULL,
                           quantity INT NOT NULL,
                           total_price DECIMAL(10, 2) NOT NULL,
                           FOREIGN KEY (order_id) REFERENCES `order`(order_id) ON DELETE CASCADE,
                           FOREIGN KEY (drink_id) REFERENCES drink(drink_id)
);

CREATE TABLE admin (
                       admin_id INT AUTO_INCREMENT PRIMARY KEY,
                       username VARCHAR(100) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       branch_id INT NOT NULL,
                       FOREIGN KEY (branch_id) REFERENCES branch(branch_id)
);

CREATE TABLE stock (
                       stock_id INT AUTO_INCREMENT PRIMARY KEY,
                       branch_id INT NOT NULL,
                       drink_id INT NOT NULL,
                       quantity INT NOT NULL,

                       FOREIGN KEY (branch_id) REFERENCES branch(branch_id),
                       FOREIGN KEY (drink_id) REFERENCES drink(drink_id),
);

INSERT INTO branch (branch_name) VALUES
                                     ('Nairobi'),
                                     ('Mombasa'),
                                     ('Nakuru'),
                                     ('Kisumu');


INSERT INTO admin (username, password, branch_id)
VALUES ('admin1', 'adminpass', 1);
INSERT INTO admin (username, password, branch_id)
VALUES ('admin2', 'adminpass', 2);
INSERT INTO admin (username, password, branch_id)
VALUES ('admin3', 'adminpass', 3);




-- Insert sample customers
INSERT INTO customer (customer_name, phone) VALUES
                                                ('John', '0712345678'),
                                                ('Jane', '0798765432');

-- Insert sample drinks
INSERT INTO drink (drink_name, price) VALUES
                                          ('Lemonade', 100.00),
                                          ('Iced Tea', 120.00),
                                          ('Mango Juice', 150.00);