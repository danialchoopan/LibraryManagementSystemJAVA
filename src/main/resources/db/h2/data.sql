-- Sample Data for Library Management System (H2)

-- Insert sample books
INSERT INTO books (title, author, isbn, published_year, quantity, available_quantity) VALUES
('The Great Gatsby', 'F. Scott Fitzgerald', '978-0-7432-7356-5', 1925, 5, 3),
('To Kill a Mockingbird', 'Harper Lee', '978-0-06-112008-4', 1960, 3, 2),
('1984', 'George Orwell', '978-0-452-28423-4', 1949, 4, 1),
('Pride and Prejudice', 'Jane Austen', '978-0-14-143951-8', 1813, 6, 5),
('The Catcher in the Rye', 'J.D. Salinger', '978-0-316-76948-0', 1951, 2, 2),
('Harry Potter and the Philosopher''s Stone', 'J.K. Rowling', '978-0-7475-3274-3', 1997, 10, 7),
('The Lord of the Rings', 'J.R.R. Tolkien', '978-0-618-64015-7', 1954, 8, 6);

-- Insert sample members
INSERT INTO members (name, national_code, phone_number, join_date) VALUES
('John Smith', '1234567890', '+1-555-0101', '2023-01-15'),
('Emily Johnson', '9876543210', '+1-555-0102', '2023-02-20'),
('Michael Brown', '5555555555', '+1-555-0103', '2023-03-10'),
('Sarah Davis', '1111111111', '+1-555-0104', '2023-04-05'),
('Robert Wilson', '2222222222', '+1-555-0105', '2023-05-12');

-- Insert sample borrow records
INSERT INTO borrow_records (book_id, member_id, borrow_date, return_date, status) VALUES
(1, 1, '2024-01-01', '2024-01-10', 'RETURNED'),
(2, 1, '2024-01-05', NULL, 'BORROWED'),
(3, 2, '2024-01-10', NULL, 'BORROWED'),
(1, 3, '2024-01-15', NULL, 'BORROWED'),
(4, 4, '2024-01-20', NULL, 'BORROWED'),
(5, 5, '2023-12-01', NULL, 'OVERDUE'),
(6, 1, '2024-01-25', NULL, 'BORROWED'),
(7, 2, '2024-01-28', NULL, 'BORROWED');