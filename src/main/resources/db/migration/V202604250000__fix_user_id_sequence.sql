-- Create sequence for usr.id
CREATE SEQUENCE usr_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- Set sequence owner to usr.id column
ALTER TABLE usr ALTER COLUMN id SET DEFAULT nextval('usr_id_seq');

-- Link the sequence to the table
ALTER SEQUENCE usr_id_seq OWNED BY usr.id;

-- Set the current value of the sequence based on existing data
SELECT setval('usr_id_seq', COALESCE((SELECT MAX(id) FROM usr), 1));