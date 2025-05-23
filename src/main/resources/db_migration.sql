-- Skripta za migraciju baze podataka koja dodaje podršku za tipove poruka i lokacije

-- Dodavanje polja za tip poruke u tabelu messages
ALTER TABLE messages
ADD COLUMN message_type VARCHAR(10) NOT NULL DEFAULT 'TEXT';

-- Dodavanje polja za koordinate lokacije u tabelu messages
ALTER TABLE messages
ADD COLUMN latitude DOUBLE PRECISION NULL;

ALTER TABLE messages
ADD COLUMN longitude DOUBLE PRECISION NULL;

-- Dodavanje polja za tekstualnu adresu lokacije
ALTER TABLE messages
ADD COLUMN address VARCHAR(512) NULL;

-- Dodavanje polja za tip poslednje poruke u tabelu conversations
ALTER TABLE conversations
ADD COLUMN last_message_type VARCHAR(10) NULL;

-- Postavljanje svih postojećih poruka kao tekstualne
UPDATE messages SET message_type = 'TEXT';

-- Komentar za dokumentaciju
-- Za podršku za nove tipove poruka, potrebno je pokrenuti ovu skriptu
-- pre pokretanja nove verzije aplikacije.
-- Skriptu je potrebno pokrenuti direktno na bazi podataka. 