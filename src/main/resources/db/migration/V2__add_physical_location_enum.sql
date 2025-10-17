-- Convert physical_location to use enum values and update constraints
-- V2: Add support for standardized physical location enum

-- First, update the column length (enum values are shorter)
ALTER TABLE books MODIFY COLUMN physical_location VARCHAR(50);

-- Update existing data to match enum values where possible
UPDATE books SET physical_location = 'LIVING_ROOM' WHERE UPPER(physical_location) IN ('LIVING ROOM', 'LIVING_ROOM', 'LIVINGROOM');
UPDATE books SET physical_location = 'BEDROOM' WHERE UPPER(physical_location) IN ('BEDROOM', 'BED ROOM', 'BED_ROOM');
UPDATE books SET physical_location = 'MASTER_BEDROOM' WHERE UPPER(physical_location) IN ('MASTER BEDROOM', 'MASTER_BEDROOM', 'MASTERBEDROOM');
UPDATE books SET physical_location = 'GUEST_BEDROOM' WHERE UPPER(physical_location) IN ('GUEST BEDROOM', 'GUEST_BEDROOM', 'GUESTBEDROOM');
UPDATE books SET physical_location = 'HOME_OFFICE' WHERE UPPER(physical_location) IN ('HOME OFFICE', 'HOME_OFFICE', 'HOMEOFFICE', 'OFFICE');
UPDATE books SET physical_location = 'STUDY' WHERE UPPER(physical_location) IN ('STUDY', 'STUDY ROOM');
UPDATE books SET physical_location = 'LIBRARY' WHERE UPPER(physical_location) IN ('LIBRARY', 'BOOK ROOM');
UPDATE books SET physical_location = 'BASEMENT' WHERE UPPER(physical_location) IN ('BASEMENT', 'CELLAR');
UPDATE books SET physical_location = 'ATTIC' WHERE UPPER(physical_location) IN ('ATTIC', 'LOFT');
UPDATE books SET physical_location = 'GARAGE' WHERE UPPER(physical_location) IN ('GARAGE', 'CAR PORT', 'CARPORT');
UPDATE books SET physical_location = 'KITCHEN' WHERE UPPER(physical_location) IN ('KITCHEN');
UPDATE books SET physical_location = 'DINING_ROOM' WHERE UPPER(physical_location) IN ('DINING ROOM', 'DINING_ROOM', 'DININGROOM');
UPDATE books SET physical_location = 'FAMILY_ROOM' WHERE UPPER(physical_location) IN ('FAMILY ROOM', 'FAMILY_ROOM', 'FAMILYROOM');
UPDATE books SET physical_location = 'DEN' WHERE UPPER(physical_location) IN ('DEN');
UPDATE books SET physical_location = 'CLOSET' WHERE UPPER(physical_location) IN ('CLOSET', 'WALK-IN CLOSET', 'WALKIN CLOSET');
UPDATE books SET physical_location = 'HALLWAY' WHERE UPPER(physical_location) IN ('HALLWAY', 'HALL WAY', 'HALL', 'CORRIDOR');
UPDATE books SET physical_location = 'BATHROOM' WHERE UPPER(physical_location) IN ('BATHROOM', 'BATH ROOM', 'BATH');
UPDATE books SET physical_location = 'STORAGE_ROOM' WHERE UPPER(physical_location) IN ('STORAGE ROOM', 'STORAGE_ROOM', 'STORAGEROOM', 'STORAGE');
UPDATE books SET physical_location = 'BOOKSHELF_A' WHERE UPPER(physical_location) IN ('BOOKSHELF A', 'BOOKSHELF_A', 'SHELF A', 'SHELF_A');
UPDATE books SET physical_location = 'BOOKSHELF_B' WHERE UPPER(physical_location) IN ('BOOKSHELF B', 'BOOKSHELF_B', 'SHELF B', 'SHELF_B');
UPDATE books SET physical_location = 'BOOKSHELF_C' WHERE UPPER(physical_location) IN ('BOOKSHELF C', 'BOOKSHELF_C', 'SHELF C', 'SHELF_C');

-- Set any remaining unmatched values to OTHER
UPDATE books SET physical_location = 'OTHER' 
WHERE physical_location IS NOT NULL 
  AND physical_location NOT IN (
    'LIVING_ROOM', 'BEDROOM', 'MASTER_BEDROOM', 'GUEST_BEDROOM', 
    'HOME_OFFICE', 'STUDY', 'LIBRARY', 'BASEMENT', 'ATTIC', 'GARAGE',
    'KITCHEN', 'DINING_ROOM', 'FAMILY_ROOM', 'DEN', 'CLOSET', 
    'HALLWAY', 'BATHROOM', 'STORAGE_ROOM', 'BOOKSHELF_A', 
    'BOOKSHELF_B', 'BOOKSHELF_C', 'OTHER'
  );

-- Add constraint to ensure only valid enum values
ALTER TABLE books ADD CONSTRAINT chk_physical_location CHECK (
  physical_location IS NULL OR 
  physical_location IN (
    'LIVING_ROOM', 'BEDROOM', 'MASTER_BEDROOM', 'GUEST_BEDROOM',
    'HOME_OFFICE', 'STUDY', 'LIBRARY', 'BASEMENT', 'ATTIC', 'GARAGE',
    'KITCHEN', 'DINING_ROOM', 'FAMILY_ROOM', 'DEN', 'CLOSET',
    'HALLWAY', 'BATHROOM', 'STORAGE_ROOM', 'BOOKSHELF_A',
    'BOOKSHELF_B', 'BOOKSHELF_C', 'OTHER'
  )
);

-- Create index for physical location filtering
CREATE INDEX idx_physical_location ON books(physical_location);