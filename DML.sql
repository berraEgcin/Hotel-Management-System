
DROP PROCEDURE IF EXISTS admin_check_room_status;
DROP PROCEDURE IF EXISTS admin_add_room;
DROP PROCEDURE IF EXISTS admin_modify_room;
DROP PROCEDURE IF EXISTS admin_handle_unpaid_booking;
DROP PROCEDURE IF EXISTS admin_view_user_accounts;
DROP PROCEDURE IF EXISTS admin_generate_revenue_report;
DROP PROCEDURE IF EXISTS admin_view_all_bookings;
DROP PROCEDURE IF EXISTS admin_view_housekeeping_records;
DROP PROCEDURE IF EXISTS admin_view_popular_room_types;
DROP PROCEDURE IF EXISTS admin_view_employees;

DROP TRIGGER IF EXISTS guest_id_check;
DROP TRIGGER IF EXISTS booking_id_check;
DROP TRIGGER IF EXISTS room_id_check;
DROP TRIGGER IF EXISTS employee_id_check;
DROP TRIGGER IF EXISTS payment_id_check;
DROP TRIGGER IF EXISTS room_price_check;
DROP TRIGGER IF EXISTS payment_amount_check;



DELIMITER //

DROP TRIGGER IF EXISTS check_in_before_check_out;

CREATE TRIGGER check_in_before_check_out
BEFORE INSERT ON booking
FOR EACH ROW
BEGIN
    IF NEW.check_out < NEW.check_in THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Check-out date must be after check-in date.';
    END IF;
END //

DROP TRIGGER IF EXISTS check_in_before_check_out_update;
CREATE TRIGGER check_in_before_check_out_update
BEFORE UPDATE ON booking
FOR EACH ROW
BEGIN
    IF NEW.check_out < NEW.check_in THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Check-out date must be after check-in date.';
    END IF;
END //


CREATE TRIGGER room_price_check
BEFORE INSERT ON room_type
FOR EACH ROW
BEGIN
    IF NEW.price < 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Price cannot be negative';
    END IF;
END //

DROP TRIGGER IF EXISTS room_price_update_check;
CREATE TRIGGER room_price_update_check
BEFORE UPDATE ON room_type
FOR EACH ROW
BEGIN
    IF NEW.price < 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Price cannot be negative';
    END IF;
END //

CREATE TRIGGER payment_amount_check
BEFORE INSERT ON payment
FOR EACH ROW
BEGIN
    IF NEW.amount < 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Amount cannot be negative';
    END IF;
END //

DROP TRIGGER IF EXISTS payment_amount_update_check;

CREATE TRIGGER payment_amount_update_check
BEFORE UPDATE ON payment
FOR EACH ROW
BEGIN
    IF NEW.amount < 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Amount cannot be negative';
    END IF;
END //

CREATE TRIGGER guest_id_check
BEFORE INSERT ON guest
FOR EACH ROW
BEGIN
    IF NEW.guest_ID IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'guest_ID cannot be NULL';
    END IF;

    IF EXISTS (SELECT 1 FROM guest WHERE guest_ID = NEW.guest_ID) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'guest_ID must be unique';
    END IF;
END //

CREATE TRIGGER room_id_check
BEFORE INSERT ON room
FOR EACH ROW
BEGIN
    IF NEW.room_ID IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'room_ID cannot be NULL';
    END IF;

    IF EXISTS (SELECT 1 FROM room WHERE room_ID = NEW.room_ID) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'room_ID must be unique';
    END IF;
END //

CREATE TRIGGER booking_id_check
BEFORE INSERT ON booking
FOR EACH ROW
BEGIN
    IF NEW.booking_ID IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'booking_ID cannot be NULL';
    END IF;

    IF EXISTS (SELECT 1 FROM booking WHERE booking_ID = NEW.booking_ID) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'booking_ID must be unique';
    END IF;
END //

CREATE TRIGGER employee_id_check
BEFORE INSERT ON employee
FOR EACH ROW
BEGIN
    IF NEW.employee_ID IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'employee_ID cannot be NULL';
    END IF;

    IF EXISTS (SELECT 1 FROM employee WHERE employee_ID = NEW.employee_ID) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'employee_ID must be unique';
    END IF;
END //

CREATE TRIGGER payment_id_check
BEFORE INSERT ON payment
FOR EACH ROW
BEGIN
    IF NEW.pay_ID IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'pay_ID cannot be NULL';
    END IF;

    IF EXISTS (SELECT 1 FROM payment WHERE pay_ID = NEW.pay_ID) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'pay_ID must be unique';
    END IF;
END //

CREATE PROCEDURE admin_check_room_status(IN p_room_id INT)
BEGIN
    IF NOT EXISTS (SELECT 1 FROM room WHERE room_id = p_room_id) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'room is not created.';
    ELSE
        SELECT
            r.room_id,
            rt.type_name,
            r.isClean,
            r.isAvailable,
            rt.price,
            COALESCE(b.booking_id, 'No active booking') AS current_booking
        FROM room r
        JOIN room_type rt ON r.type_id = rt.type_id
        LEFT JOIN booking b ON r.room_id = b.room_id
        WHERE r.room_id = p_room_id;
    END IF;
END //

CREATE PROCEDURE admin_add_room(
    IN p_room_ID INT,
    IN p_type_id INT,
    IN p_is_clean ENUM('Clean', 'Dirty', 'In Progress'),
    IN p_is_available BOOLEAN
)
BEGIN
IF EXISTS (SELECT 1 FROM room WHERE room_id = p_room_id) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Room ID already exists.';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM room_type WHERE type_id = p_type_id) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Invalid Type ID: Type does not exist.';
    END IF;

    IF p_is_clean NOT IN ('Clean', 'Dirty', 'In Progress') THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Invalid cleanliness status.';
    END IF;

    IF p_is_available NOT IN (TRUE, FALSE) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Invalid availability status.';
    END IF;
    INSERT INTO room (room_id, type_id, isClean, isAvailable)
    VALUES (p_room_id, p_type_id, p_is_clean, p_is_available);
END //

DROP PROCEDURE IF EXISTS admin_delete_room;

CREATE PROCEDURE admin_delete_room (
    IN delete_room_ID INT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM booking
        WHERE room_id = delete_room_ID
        AND b_status = 'confirmed'
        AND check_out >= CURDATE()
    ) THEN
        DELETE FROM room
        WHERE room_id = delete_room_ID;
    ELSE
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'The room is booked, therefore cannot be deleted';
    END IF;
END //



CREATE PROCEDURE admin_modify_room(
    IN p_room_id INT,
    IN p_type_id INT,
    IN p_is_clean ENUM('Clean', 'Dirty', 'In Progress'),
    IN p_is_available BOOLEAN
)
BEGIN
IF NOT EXISTS (SELECT 1 FROM room WHERE room_id = p_room_id) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Room ID does not exist.';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM room_type WHERE type_id = p_type_id) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Invalid Type ID: Type does not exist.';
    END IF;

    IF p_is_clean NOT IN ('Clean', 'Dirty', 'In Progress') THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Invalid cleanliness status.';
    END IF;

    IF p_is_available NOT IN (TRUE, FALSE) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Invalid availability status.';
    END IF;

    UPDATE room
    SET type_id = p_type_id,
        isClean = p_is_clean,
        isAvailable = p_is_available
    WHERE room_id = p_room_id;
END //


CREATE PROCEDURE admin_handle_unpaid_booking(IN p_booking_id INT)
BEGIN
    IF NOT EXISTS (SELECT 1 FROM booking WHERE booking_id = p_booking_id) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Booking ID does not exist.';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM payment WHERE booking_id = p_booking_id) THEN
        UPDATE booking
        SET b_status = 'cancelled'
        WHERE booking_id = p_booking_id;

        UPDATE room r
        JOIN booking b ON r.room_id = b.room_id
        SET r.isAvailable = TRUE
        WHERE b.booking_id = p_booking_id;
    END IF;
END //

CREATE PROCEDURE admin_view_user_accounts()
BEGIN
    SELECT
        g.guest_id,
        gn.g_first_name,
        gn.g_last_name,
        g.phone_number,
        g.b_points,
        l.loyalty_rank,
        l.discount_percentage
    FROM guest g
    JOIN guest_name gn ON g.guest_id = gn.guest_id
    JOIN loyalty l ON g.loyalty_id = l.loyalty_id
    ORDER BY g.guest_id;
END //

CREATE PROCEDURE admin_generate_revenue_report(
    IN start_date DATE,
    IN end_date DATE
)
BEGIN
    IF start_date >= end_date THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Invalid date range: start_date must be before end_date.';
    END IF;

    SELECT
        rt.type_name,
        COUNT(b.booking_id) AS total_bookings,
        COALESCE(SUM(p.amount), 0) AS total_revenue,
        COALESCE(AVG(p.amount), 0) AS average_revenue_per_booking
    FROM booking b
    JOIN payment p ON b.booking_id = p.booking_id
    JOIN room r ON b.room_id = r.room_id
    JOIN room_type rt ON r.type_id = rt.type_id
    WHERE b.check_in BETWEEN start_date AND end_date
    GROUP BY rt.type_name WITH ROLLUP;

    IF ROW_COUNT() = 0 THEN
        SELECT 'No bookings found in the specified date range.' AS message;
    END IF;
END //

CREATE PROCEDURE admin_view_all_bookings()
BEGIN
    SELECT
        b.booking_id,
        gn.g_first_name,
        gn.g_last_name,
        r.room_id,
        rt.type_name,
        b.check_in,
        b.check_out,
        b.b_status,
        COALESCE(p.amount, 'No payment required') as payment_amount
    FROM booking b
    JOIN guest g ON b.guest_id = g.guest_id
    JOIN guest_name gn ON g.guest_id = gn.guest_id
    JOIN room r ON b.room_id = r.room_id
    JOIN room_type rt ON r.type_id = rt.type_id
    LEFT JOIN payment p ON b.booking_id = p.booking_id
    ORDER BY b.check_in DESC;
END //

CREATE PROCEDURE admin_view_housekeeping_records()
BEGIN
    SELECT
        s.schedulee_id,
        en.e_first_name,
        en.e_last_name,
        r.room_id,
        s.cleaning_date,
        s.status,
        r.isClean as current_room_status
    FROM schedulee s
    JOIN housekeeping h ON s.employee_id = h.employee_id
    JOIN employee e ON h.employee_id = e.employee_id
    JOIN employee_name en ON e.employee_id = en.employee_id
    JOIN room r ON s.room_id = r.room_id
    ORDER BY s.cleaning_date DESC;
END //

CREATE PROCEDURE admin_view_popular_room_types()
BEGIN
    SELECT
        rt.type_name,
        COUNT(b.booking_id) as booking_count,
        rt.price,
        rt.max_occupancy,
        rt.bed_type
    FROM room_type rt
    JOIN room r ON rt.type_id = r.type_id
    JOIN booking b ON r.room_id = b.room_id
    WHERE b.b_status = 'confirmed'
    GROUP BY rt.type_id
    ORDER BY booking_count DESC;
END //

CREATE PROCEDURE admin_view_employees()
BEGIN
    SELECT
        e.employee_id,
        en.e_first_name,
        en.e_last_name,
        e.role,
        CASE
            WHEN h.employee_id IS NOT NULL THEN 'Housekeeping'
            WHEN r.employee_id IS NOT NULL THEN 'Receptionist'
            ELSE 'Unknown'
        END as specific_role
    FROM employee e
    JOIN employee_name en ON e.employee_id = en.employee_id
    LEFT JOIN housekeeping h ON e.employee_id = h.employee_id
    LEFT JOIN receptionist r ON e.employee_id = r.employee_id
    ORDER BY e.role, en.e_last_name;
END //

DROP PROCEDURE IF EXISTS admin_add_guest;

CREATE PROCEDURE admin_add_guest(
    IN p_guest_id INT,
    IN p_birth_date DATE,
    IN p_b_points INT,
    IN p_loyalty_id INT,
    IN p_phone_number VARCHAR(10),
    IN p_first_name VARCHAR(255),
    IN p_last_name VARCHAR(255)
)
BEGIN

    INSERT INTO guest (guest_ID, birth_date, b_points, loyalty_id, phone_number)
    VALUES (p_guest_id, p_birth_date, p_b_points, p_loyalty_id, p_phone_number);

    INSERT INTO guest_name (guest_ID, g_first_Name, g_last_name)
    VALUES (p_guest_id, p_first_name, p_last_name);

END //

DROP PROCEDURE IF EXISTS admin_add_employee;

CREATE PROCEDURE admin_add_employee(
    IN p_employee_id INT,
    IN p_role ENUM('receptionist', 'housekeeping'),
    IN p_first_name VARCHAR(255),
    IN p_last_name VARCHAR(255)
)
BEGIN
    INSERT INTO employee (employee_ID, role)
    VALUES (p_employee_id, p_role);

    INSERT INTO employee_name (employee_ID, e_first_name, e_last_name)
    VALUES (p_employee_id, p_first_name, p_last_name);
END //



CALL admin_check_room_status(1);
CALL admin_add_room(102, 1, 'Clean', TRUE);
CALL admin_modify_room(104, 1, 'Dirty', FALSE);
CALL admin_handle_unpaid_booking(1);
CALL admin_view_user_accounts();
CALL admin_generate_revenue_report('2024-01-01', '2024-12-31');
CALL admin_view_all_bookings();
CALL admin_view_housekeeping_records();
CALL admin_view_popular_room_types();
CALL admin_view_employees();


-- RECEPTIONIST

DROP PROCEDURE IF EXISTS receptionist_view_pending_bookings;
DROP PROCEDURE IF EXISTS receptionist_confirm_booking;
DROP PROCEDURE IF EXISTS receptionist_view_available_rooms;
DROP PROCEDURE IF EXISTS receptionist_process_payment;
DROP PROCEDURE IF EXISTS receptionist_assign_housekeeping;
DROP PROCEDURE IF EXISTS receptionist_schedule_after_checkout;
DROP PROCEDURE IF EXISTS receptionist_view_housekeepers_availability;
DROP PROCEDURE IF EXISTS receptionist_modify_booking;
DROP PROCEDURE IF EXISTS guest_view_available_rooms;


CREATE PROCEDURE receptionist_view_pending_bookings()
BEGIN
    SELECT
        b.booking_id,
        gn.g_first_name,
        gn.g_last_name,
        r.room_id,
        rt.type_name,
        b.num_Of_Guests,
        b.check_in,
        b.check_out,
        rt.max_occupancy,
        r.isClean,
        r.isAvailable
    FROM booking b
    JOIN guest g ON b.guest_id = g.guest_id
    JOIN guest_name gn ON g.guest_id = gn.guest_id
    JOIN room r ON b.room_id = r.room_id
    JOIN room_type rt ON r.type_id = rt.type_id
    WHERE b.b_status = 'pending'
    ORDER BY b.check_in;
END //

CREATE PROCEDURE receptionist_confirm_booking(
    IN p_booking_id INT,
    IN p_receptionist_id INT
)
BEGIN
    DECLARE v_room_id INT;
    DECLARE v_is_available BOOLEAN;
    DECLARE v_num_guests INT;
    DECLARE v_max_occupancy INT;

    SELECT b.room_id, r.isAvailable, b.num_Of_Guests, rt.max_occupancy
    INTO v_room_id, v_is_available, v_num_guests, v_max_occupancy
    FROM booking b
    JOIN room r ON b.room_id = r.room_id
    JOIN room_type rt ON r.type_id = rt.type_id
    WHERE b.booking_id = p_booking_id;

    IF v_is_available = TRUE AND v_num_guests <= v_max_occupancy THEN
        UPDATE booking
        SET b_status = 'confirmed'
        WHERE booking_id = p_booking_id;

        UPDATE room
        SET isAvailable = FALSE
        WHERE room_id = v_room_id;

        SELECT 'Booking confirmed successfully' as message;
    ELSE
        SELECT 'Cannot confirm booking - room unavailable or exceeds capacity' as message;
    END IF;
END //

CREATE PROCEDURE receptionist_view_available_rooms(
    IN p_check_in DATE,
    IN p_check_out DATE,
    IN p_num_guests INT
)
BEGIN
    SELECT
        r.room_id,
        rt.type_name,
        rt.max_occupancy,
        rt.price,
        r.isClean
    FROM room r
    JOIN room_type rt ON r.type_id = rt.type_id
    WHERE r.isAvailable = TRUE
    AND rt.max_occupancy >= p_num_guests
    AND r.room_id NOT IN (
        SELECT room_id
        FROM booking
        WHERE b_status = 'confirmed'
        AND (check_in BETWEEN p_check_in AND p_check_out
        OR check_out BETWEEN p_check_in AND p_check_out)
    );
END //


CREATE PROCEDURE receptionist_process_payment(
    IN p_booking_id INT,
    IN p_amount INT,
    IN p_payment_method ENUM('cash', 'card'),
    IN p_payment_time ENUM('check out', 'advance')
)
BEGIN
    INSERT INTO payment (booking_id, pay_method, pay_time, amount)
    VALUES (p_booking_id, p_payment_method, p_payment_time, p_amount);
END //

CREATE PROCEDURE receptionist_assign_housekeeping(
    IN p_room_id INT,
    IN p_housekeeper_id INT,
    IN p_cleaning_date DATE
)
BEGIN
    IF EXISTS (
        SELECT 1 FROM housekeeping
        WHERE employee_id = p_housekeeper_id
    ) THEN
        INSERT INTO schedulee (
            employee_id,
            room_id,
            cleaning_date,
            status
        )
        VALUES (
            p_housekeeper_id,
            p_room_id,
            p_cleaning_date,
            'Not Started'
        );

        UPDATE room
        SET isClean = 'In Progress'
        WHERE room_id = p_room_id;
    END IF;
END //

CREATE PROCEDURE receptionist_schedule_after_checkout()
BEGIN
    INSERT INTO schedulee (employee_id, room_id, cleaning_date, status)
    SELECT
        (SELECT h.employee_id
         FROM housekeeping h
         LEFT JOIN schedulee s ON h.employee_id = s.employee_id
         WHERE s.cleaning_date = CURDATE()
         GROUP BY h.employee_id
         ORDER BY COUNT(s.schedulee_id) ASC
         LIMIT 1),
        b.room_id,
        b.check_out,
        'Not Started'
    FROM booking b
    WHERE b.check_out = CURDATE()
    AND b.b_status = 'confirmed';
END //

CREATE PROCEDURE receptionist_view_housekeepers_availability(IN p_date DATE)
BEGIN
    SELECT
        e.employee_id,
        en.e_first_name,
        en.e_last_name,
        COUNT(s.schedulee_id) as tasks_assigned,
        GROUP_CONCAT(DISTINCT s.cleaning_date) as scheduled_dates
    FROM housekeeping h
    JOIN employee e ON h.employee_id = e.employee_id
    JOIN employee_name en ON e.employee_id = en.employee_id
    LEFT JOIN schedulee s ON h.employee_id = s.employee_id
        AND s.cleaning_date = p_date
    GROUP BY e.employee_id
    ORDER BY tasks_assigned ASC;
END //

CREATE PROCEDURE receptionist_modify_booking(
    IN p_booking_id INT,
    IN p_check_in DATE,
    IN p_check_out DATE,
    IN p_num_guests INT
)
BEGIN
    UPDATE booking
    SET check_in = p_check_in,
        check_out = p_check_out,
        num_Of_Guests = p_num_guests
    WHERE booking_id = p_booking_id
    AND b_status != 'cancelled';
END //


CALL receptionist_view_pending_bookings();
CALL receptionist_confirm_booking(1, 101);
CALL receptionist_view_available_rooms('2024-03-01', '2024-03-05', 2);
CALL receptionist_process_payment(1, 500, 'card', 'advance');
CALL receptionist_assign_housekeeping(101, 1, '2024-03-01');
CALL receptionist_schedule_after_checkout();
CALL receptionist_view_housekeepers_availability('2024-03-01');
CALL receptionist_modify_booking(1, '2024-03-01', '2024-03-05', 2);

-- HOUSEKEEPING

DROP PROCEDURE IF EXISTS housekeeping_view_rooms;
DROP PROCEDURE IF EXISTS housekeeping_view_pending_tasks;
DROP PROCEDURE IF EXISTS housekeeping_view_completed_tasks;
DROP PROCEDURE IF EXISTS housekeeping_complete_task;
DROP PROCEDURE IF EXISTS housekeeping_view_schedule;
DROP PROCEDURE IF EXISTS housekeeping_view_today_tasks;


CREATE PROCEDURE housekeeping_view_rooms(IN p_employee_id INT)
BEGIN
    IF EXISTS (SELECT 1 FROM housekeeping WHERE employee_id = p_employee_id) THEN
        SELECT
            r.room_id,
            rt.type_name,
            r.isClean,
            r.isAvailable,
            CASE
                WHEN b.check_out = CURDATE() THEN 'Checkout Today'
                WHEN b.check_out > CURDATE() THEN 'Occupied'
                ELSE 'Available'
            END as room_status
        FROM room r
        JOIN room_type rt ON r.type_id = rt.type_id
        LEFT JOIN booking b ON r.room_id = b.room_id
            AND b.b_status = 'confirmed'
            AND CURDATE() BETWEEN b.check_in AND b.check_out
        ORDER BY r.room_id;
    ELSE
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Unauthorized access';
    END IF;
END //

CREATE PROCEDURE housekeeping_view_pending_tasks(IN p_employee_id INT)
BEGIN
    SELECT
        s.schedulee_id,
        s.room_id,
        r.isClean as current_room_status,
        s.cleaning_date,
        s.status,
        rt.type_name as room_type
    FROM schedulee s
    JOIN room r ON s.room_id = r.room_id
    JOIN room_type rt ON r.type_id = rt.type_id
    WHERE s.employee_id = p_employee_id
    AND s.status IN ('Not Started', 'In Progress')
    ORDER BY s.cleaning_date, s.room_id;
END //

CREATE PROCEDURE housekeeping_view_completed_tasks(
    IN p_employee_id INT,
    IN p_start_date DATE,
    IN p_end_date DATE
)
BEGIN
    SELECT
        s.schedulee_id,
        s.room_id,
        s.cleaning_date,
        s.status,
        rt.type_name as room_type
    FROM schedulee s
    JOIN room r ON s.room_id = r.room_id
    JOIN room_type rt ON r.type_id = rt.type_id
    WHERE s.employee_id = p_employee_id
    AND s.status = 'Completed'
    AND s.cleaning_date BETWEEN p_start_date AND p_end_date
    ORDER BY s.cleaning_date DESC;
END //

CREATE PROCEDURE housekeeping_complete_task(
    IN p_employee_id INT,
    IN p_schedule_id INT
)
BEGIN
    DECLARE v_room_id INT;

    SELECT room_id INTO v_room_id
    FROM schedulee
    WHERE schedulee_id = p_schedule_id
    AND employee_id = p_employee_id;

    UPDATE schedulee
    SET status = 'Completed'
    WHERE schedulee_id = p_schedule_id
    AND employee_id = p_employee_id;

    UPDATE room
    SET isClean = 'Clean',
        isAvailable = TRUE
    WHERE room_id = v_room_id;
END //

CREATE PROCEDURE housekeeping_view_schedule(
    IN p_employee_id INT,
    IN p_start_date DATE,
    IN p_end_date DATE
)
BEGIN
    SELECT
        s.schedulee_id,
        s.room_id,
        rt.type_name as room_type,
        s.cleaning_date,
        s.status,
        r.isClean as current_room_status
    FROM schedulee s
    JOIN room r ON s.room_id = r.room_id
    JOIN room_type rt ON r.type_id = rt.type_id
    WHERE s.employee_id = p_employee_id
    AND s.cleaning_date BETWEEN p_start_date AND p_end_date
    ORDER BY s.cleaning_date, s.room_id;
END //

CREATE PROCEDURE housekeeping_view_today_tasks(IN p_employee_id INT)
BEGIN
    SELECT
        s.schedulee_id,
        s.room_id,
        rt.type_name as room_type,
        s.status,
        r.isClean as current_room_status
    FROM schedulee s
    JOIN room r ON s.room_id = r.room_id
    JOIN room_type rt ON r.type_id = rt.type_id
    WHERE s.employee_id = p_employee_id
    AND s.cleaning_date = CURDATE()
    ORDER BY
        CASE s.status
            WHEN 'In Progress' THEN 1
            WHEN 'Not Started' THEN 2
            WHEN 'Completed' THEN 3
        END,
        s.room_id;
END //

CALL housekeeping_view_rooms(1);
CALL housekeeping_view_pending_tasks(1);
CALL housekeeping_view_completed_tasks(1, '2024-03-01', '2024-03-31');
CALL housekeeping_complete_task(1, 101);
CALL housekeeping_view_schedule(1, '2024-03-01', '2024-03-31');
CALL housekeeping_view_today_tasks(1);

-- GUEST

CREATE PROCEDURE guest_view_available_rooms(
    IN p_check_in DATE,
    IN p_check_out DATE,
    IN p_num_guests INT
)
BEGIN
    SELECT
        rt.type_name,
        rt.max_occupancy,
        rt.bed_count,
        rt.bed_type,
        rt.price,
        rt.description,
        COUNT(r.room_id) as available_rooms
    FROM room_type rt
    JOIN room r ON rt.type_id = r.type_id
    WHERE r.isAvailable = TRUE
    AND r.isClean = 'Clean'
    AND r.room_id NOT IN (
        SELECT room_id
        FROM booking
        WHERE b_status = 'confirmed'
        AND (
            (check_in BETWEEN p_check_in AND p_check_out)
            OR (check_out BETWEEN p_check_in AND p_check_out)
            OR (check_in <= p_check_in AND check_out >= p_check_out)
        )
    )
    GROUP BY rt.type_id, rt.max_occupancy,rt.price
    HAVING
        CASE
            WHEN rt.max_occupancy >= p_num_guests THEN TRUE
            ELSE FALSE
        END
    ORDER BY rt.price;
END //

DROP PROCEDURE IF EXISTS guest_add_booking;

CREATE PROCEDURE guest_add_booking(
    IN p_guest_id INT,
    IN p_room_id INT,
    IN p_num_guests INT,
    IN p_check_in DATE,
    IN p_check_out DATE
)
BEGIN
    DECLARE v_max_occupancy INT;
    DECLARE v_is_available BOOLEAN;

    SELECT rt.max_occupancy, r.isAvailable
    INTO v_max_occupancy, v_is_available
    FROM room r
    JOIN room_type rt ON r.type_id = rt.type_id
    WHERE r.room_id = p_room_id;

    IF p_check_in >= CURDATE()
    AND p_check_out > p_check_in
    AND p_num_guests <= v_max_occupancy
    AND v_is_available = TRUE THEN
        INSERT INTO booking (
            guest_id,
            room_id,
            num_Of_Guests,
            check_in,
            check_out,
            b_status
        )
        VALUES (
            p_guest_id,
            p_room_id,
            p_num_guests,
            p_check_in,
            p_check_out,
            'pending'
        );

        SELECT 'Booking created successfully' as message;
    ELSE
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Invalid booking parameters';
    END IF;
END //

DROP PROCEDURE guest_view_my_bookings;
CREATE PROCEDURE guest_view_my_bookings(IN p_guest_id INT)
BEGIN
    SELECT
        b.booking_id,
        r.room_id,
        rt.type_name,
        rt.price,
        b.num_Of_Guests,
        b.check_in,
        b.check_out,
        b.b_status,
        COALESCE(p.amount, 'No payment') as payment_status,
        COALESCE(p.pay_time, 'Not paid') as payment_time
    FROM booking b
    JOIN room r ON b.room_id = r.room_id
    JOIN room_type rt ON r.type_id = rt.type_id
    LEFT JOIN payment p ON b.booking_id = p.booking_id
    WHERE b.guest_id = p_guest_id
    ORDER BY b.check_in DESC;
END //

DROP PROCEDURE  IF EXISTS guest_cancel_booking;
CREATE PROCEDURE guest_cancel_booking(
    IN p_guest_id INT,
    IN p_booking_id INT
)
BEGIN
    IF EXISTS (
        SELECT 1
        FROM booking
        WHERE booking_id = p_booking_id
        AND guest_id = p_guest_id
        AND check_in > CURDATE()
        AND b_status = 'pending'
    ) THEN
        UPDATE booking
        SET b_status = 'cancelled'
        WHERE booking_id = p_booking_id;

        UPDATE room r
        JOIN booking b ON r.room_id = b.room_id
        SET r.isAvailable = TRUE
        WHERE b.booking_id = p_booking_id;

        SELECT 'Booking cancelled successfully' as message;
    ELSE
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Cannot cancel booking';
    END IF;
END //


DROP PROCEDURE IF EXISTS guest_check_payment_status;
CREATE PROCEDURE guest_check_payment_status(
    IN p_guest_id INT,
    IN p_booking_id INT
)
BEGIN
    SELECT
        b.booking_id,
        b.check_in,
        b.check_out,
        rt.price as room_price,
        COALESCE(p.amount, 0) as amount_paid,
        CASE
            WHEN p.pay_id IS NULL THEN 'No payment made'
            WHEN p.pay_time = 'advance' THEN 'Paid in advance'
            ELSE 'Paid at checkout'
        END as payment_status
    FROM booking b
    JOIN room r ON b.room_id = r.room_id
    JOIN room_type rt ON r.type_id = rt.type_id
    LEFT JOIN payment p ON b.booking_id = p.booking_id
    WHERE b.guest_id = p_guest_id
    AND b.booking_id = p_booking_id;
END //



CALL guest_view_available_rooms('2024-03-01', '2024-03-05', 4);
CALL guest_add_booking(1, 101, 2, '2024-03-01', '2024-03-05');
CALL guest_view_my_bookings(1);
CALL guest_cancel_booking(1, 1);
CALL guest_check_payment_status(1, 1);

DELIMITER ;

-- DATA INSERTION
INSERT INTO hotel (hotel_ID, hotel_name) VALUES
(1, 'Luxury Palace'),
(2, 'Budget Inn'),
(3, 'Resort & Spa');

INSERT INTO address (hotel_ID, street, city, district) VALUES
(1, '123 Main St', 'New York', 'Manhattan'),
(2, '456 Side St', 'Chicago', 'Loop'),
(3, '789 Beach Rd', 'Miami', 'South Beach');

INSERT INTO loyalty (loyalty_id, loyalty_rank) VALUES
(1, 'bronze'),
(2, 'silver'),
(3, 'gold');

INSERT INTO guest (guest_ID, birth_date, b_points, loyalty_id, phone_number) VALUES
(1, '1990-01-01', 5, 1, '1234567890'),
(2, '2000-12-31', 25, 2, '9876543210'),
(3, '1950-06-15', 75, 3, '5555555555');



INSERT INTO guest_name (guest_ID, g_first_Name, g_last_name) VALUES
(1, 'John', 'Doe'),
(2, 'Jane', 'Smith'),
(4, 'Baby', 'Jones'),
(5, 'Test', 'User');


INSERT INTO room_type (type_ID, type_name, max_occupancy, price, bed_count, bed_type, description) VALUES
(1,  'Single', 1, 100, 1, 'Single', 'Basic room'),
(2,  'Double', 2, 200, 2, 'Queen', 'Comfort room'),
(3,  'Suite', 4, 500, 2, 'King', 'Luxury suite');


INSERT INTO room (room_ID, type_ID, isClean, isAvailable) VALUES
(1, 1, 'Clean', TRUE),
(2, 2, 'Dirty', FALSE),
(3, 3, 'In Progress', TRUE);



INSERT INTO booking (booking_ID, guest_ID, room_ID, num_Of_Guests, check_in, check_out, b_status) VALUES
(1, 1, 1, 1, '2024-01-01', '2024-01-05', 'confirmed'),
(2, 2, 2, 2, '2024-02-01', '2024-01-31', 'confirmed'),
(3, 3, 3, 6, '2024-03-01', '2024-03-05', 'pending'),
(4, 4, 1, 0, '2024-04-01', '2024-04-05', 'cancelled'),
(5, 1, 1, 1, NULL, '2024-05-05', 'confirmed');


INSERT INTO payment (pay_ID, booking_ID, pay_method, pay_time, amount) VALUES
(1, 1, 'cash', 'check out', 500),
(2, 2, 'card', 'advance', 1000);

INSERT INTO employee (employee_ID, role) VALUES
(1, 'receptionist'),
(2, 'housekeeping'),
(3, 'receptionist');

INSERT INTO employee_name (employee_ID, e_first_name, e_last_name) VALUES
(1, 'Alice', 'Johnson'),
(2, 'Bob', 'Williams'),
(1, 'Jane', 'Doe');


INSERT INTO housekeeping (employee_ID) VALUES
(2);

INSERT INTO administrator (admin_ID) VALUES
(1);

INSERT INTO schedulee (schedulee_ID, employee_ID, room_ID, cleaning_date, status) VALUES
(1, 2, 1, '2024-01-01', 'Completed'),
(2, 2, 2, '2024-01-02', 'In Progress'),
(3, 1, 3, '2024-01-03', 'Not Started'),
(4, 2, 999, '2024-01-04', 'Completed');
