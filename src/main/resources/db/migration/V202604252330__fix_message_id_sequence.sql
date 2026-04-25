-- Create sequence for usr.id
CREATE SEQUENCE message_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- Set sequence owner to usr.id column
ALTER TABLE message ALTER COLUMN id SET DEFAULT nextval('message_id_seq');

-- Link the sequence to the table
ALTER SEQUENCE message_id_seq OWNED BY message.id;

-- Set the current value of the sequence based on existing data
SELECT setval('message_id_seq', COALESCE((SELECT MAX(id) FROM message), 1));