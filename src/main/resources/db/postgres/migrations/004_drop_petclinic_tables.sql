-- Drops the unused PetClinic tables (owners, pets, visits, vets, specialties,
-- vet_specialties). Nothing in the application reads or writes them.
-- The `types` table is kept: arguments.type_id still references it.
-- Idempotent; run manually with psql. Destructive: back up first if the data matters.

DROP TABLE IF EXISTS visits;
DROP TABLE IF EXISTS pets;
DROP TABLE IF EXISTS owners;
DROP TABLE IF EXISTS vet_specialties;
DROP TABLE IF EXISTS specialties;
DROP TABLE IF EXISTS vets;
