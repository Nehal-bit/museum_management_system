-- ============================================================
-- Migration: Add number_of_tickets to bookings table
-- Run this ONCE against museum_db before restarting the app.
-- ============================================================

-- Step 1: Add the column (nullable first so existing rows don't fail)
ALTER TABLE bookings
    ADD COLUMN IF NOT EXISTS number_of_tickets INT NOT NULL DEFAULT 1;

-- Step 2: Back-fill existing rows that have 0 (legacy data)
UPDATE bookings
SET number_of_tickets = 1
WHERE number_of_tickets = 0;

-- Step 3: Add a CHECK constraint to prevent invalid values going forward
-- (MySQL 8.0.16+ enforces CHECK constraints)
ALTER TABLE bookings
    ADD CONSTRAINT chk_bookings_tickets_positive
    CHECK (number_of_tickets >= 1);
