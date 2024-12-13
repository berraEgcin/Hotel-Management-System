DROP DATABASE IF exists hotel_management_system1;

CREATE database hotel_management_system1;

USE hotel_management_system1;

DROP TABLE IF EXISTS schedulee; --
DROP TABLE IF EXISTS housekeeping; --
DROP TABLE IF EXISTS receptionist; --
DROP TABLE IF EXISTS employee_name; --
DROP TABLE IF EXISTS employee;
DROP TABLE IF EXISTS payment; --
DROP TABLE IF EXISTS booking;--
DROP TABLE IF EXISTS room;
DROP TABLE IF EXISTS room_type;
DROP TABLE IF EXISTS inviteeName;
DROP TABLE IF EXISTS invitee;
DROP TABLE IF EXISTS guest_name;
DROP TABLE IF EXISTS guest;
DROP TABLE IF EXISTS loyalty;
DROP TABLE IF EXISTS address;
DROP TABLE IF EXISTS hotel;
DROP TABLE IF EXISTS administrator;

CREATE TABLE hotel
(
    hotel_ID int NOT NULL,
    hotel_name varchar(255),
    PRIMARY KEY (hotel_ID)
);


CREATE TABLE loyalty
(
    loyalty_id int NOT NULL,
    loyalty_rank ENUM('bronze', 'silver', 'gold'),
    min_points int AS (
        CASE loyalty_rank
        WHEN 'bronze' THEN 0
        WHEN 'silver' THEN 10
        WHEN 'gold' THEN 50
    END),
    max_points int AS (
        CASE loyalty_rank
        WHEN 'bronze' THEN 9
        WHEN 'silver' THEN 49
        WHEN 'gold' THEN 999999999
    END),
    discount_percentage DECIMAL(2,0) AS (
        CASE loyalty_rank
        WHEN 'bronze' THEN 5
        WHEN 'silver' THEN 15
        WHEN 'gold' THEN 25
        END),
    PRIMARY KEY (loyalty_id)
);

CREATE TABLE administrator
(
    admin_ID int NOT NULL,
    PRIMARY KEY (admin_ID)
);

CREATE TABLE room_type
(
    type_ID int NOT NULL,
    type_name ENUM('Single', 'Double', 'Suite'),
    max_occupancy int,
    price int,
    bed_count int,
    bed_type ENUM('Single', 'Double', 'Queen', 'King'),
    description varchar(255),
    PRIMARY KEY (type_ID)
);


CREATE TABLE room
(
    room_ID int NOT NULL,
    type_ID int NOT NULL,
    isClean ENUM('Clean', 'Dirty', 'In Progress'),
    isAvailable BOOLEAN DEFAULT TRUE,
    PRIMARY KEY (room_ID),
    FOREIGN KEY (type_ID) REFERENCES room_type(type_ID) -- rooma mı room type a mı foreign gidecek
);



CREATE TABLE address
(
    hotel_ID int NOT NULL,
    street varchar(255),
    city varchar(255),
    district varchar(255),
    PRIMARY KEY (hotel_ID),
    FOREIGN KEY (hotel_ID) REFERENCES hotel(hotel_ID)
);




CREATE TABLE guest
(   guest_ID      int NOT NULL,
    birth_date    DATE,
    b_points int,
    loyalty_id int,
	age int,
	phone_number varchar(10), -- phone number is a 10-digit number
    PRIMARY KEY (guest_ID),
    FOREIGN KEY (loyalty_id) REFERENCES loyalty(loyalty_id)
);

DELIMITER //

CREATE TRIGGER age_trigger
BEFORE INSERT ON guest
FOR EACH ROW
BEGIN
    SET NEW.age = TIMESTAMPDIFF(YEAR, NEW.birth_date, CURDATE());
END;

DELIMITER ;


CREATE TABLE guest_name
(
    guest_ID  int NOT NULL,
    g_first_Name varchar(255),
    g_last_name  varchar(255),
    PRIMARY KEY (guest_ID),
    FOREIGN KEY (guest_ID) REFERENCES guest (guest_ID)
);

CREATE TABLE invitee
(
    invitee_ID int NOT NULL,
    guest_ID int NOT NULL,
    PRIMARY KEY (invitee_ID, guest_ID), -- composite primary key for the weak entity set
    FOREIGN KEY (guest_ID) REFERENCES guest (guest_ID)
);

CREATE TABLE inviteeName
(
    invitee_ID   int NOT NULL,
    i_first_name varchar(255),
    i_last_name  varchar(255),
    FOREIGN KEY (invitee_ID) REFERENCES invitee (invitee_ID)
);

--

CREATE TABLE booking
(
    booking_ID int NOT NULL,
    guest_ID int NOT NULL, -- foreign key
    room_ID int NOT NULL, -- foreign key
    num_Of_Guests int,
    check_in DATE,
    check_out DATE,
    b_status ENUM('confirmed', 'cancelled', 'pending'),
    PRIMARY KEY (booking_ID),
    FOREIGN KEY (guest_ID) REFERENCES guest (guest_ID),
    FOREIGN KEY (room_ID) REFERENCES room (room_ID)
);



CREATE TABLE payment
(
    pay_ID int NOT NULL,
    booking_ID int NOT NULL,
    pay_method ENUM('cash', 'card'),
    pay_time ENUM('check out', 'advance'),
    amount int,
    PRIMARY KEY (pay_ID),
    FOREIGN KEY (booking_ID) REFERENCES booking (booking_ID)
);

CREATE TABLE employee
(
    employee_ID int NOT NULL,
    role ENUM('receptionist', 'housekeeping'),
    PRIMARY KEY (employee_ID)
);

CREATE TABLE employee_name
(
    employee_ID int NOT NULL,
    e_first_name varchar(255),
    e_last_name varchar(255),
    PRIMARY KEY (employee_ID),
    FOREIGN KEY (employee_ID) REFERENCES employee(employee_ID)
);

CREATE TABLE receptionist
(
    employee_ID int NOT NULL,
    PRIMARY KEY (employee_ID),
    FOREIGN KEY (employee_ID) REFERENCES employee(employee_ID)
);

CREATE TABLE housekeeping
(
    employee_ID int NOT NULL,
    PRIMARY KEY (employee_ID),
    FOREIGN KEY (employee_ID) REFERENCES employee(employee_ID)
);

CREATE TABLE schedulee
(
    schedulee_ID int NOT NULL,
    employee_ID int NOT NULL, -- foreign key
    room_ID int NOT NULL, -- foreign key
    cleaning_date DATE,
    status ENUM('Completed', 'In Progress', 'Not Started'),
    PRIMARY KEY (schedulee_ID),
    FOREIGN KEY (employee_ID) REFERENCES housekeeping(employee_ID), -- housekeeping is a subclass of employee
    FOREIGN KEY (room_ID) REFERENCES room(room_ID)
);
