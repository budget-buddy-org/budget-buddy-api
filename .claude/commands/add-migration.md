Create a new numbered Liquibase migration SQL file and register it in the changelog master. Accepts a short description as the argument.

**Usage:** `/add-migration <description>` (e.g. `/add-migration add_notes_to_transactions`)

**Steps:**

1. List `src/main/resources/db/changelog/` to find all existing migration files. Identify the highest numeric prefix (e.g. if `007_...sql` exists, next is `008`). Zero-pad to at least 3 digits.

2. Create the file `src/main/resources/db/changelog/<next-number>_<description>.sql` with:
   ```sql
   -- liquibase formatted sql

   -- changeset author:<next-number>_<description>
   -- Your SQL here

   -- rollback
   -- Rollback SQL here (DROP TABLE, DROP COLUMN, etc.)
   ```
   Leave the SQL body as a placeholder with a comment describing what needs to be written — do not invent schema changes.

3. Register the new file in `src/main/resources/db/changelog/db.changelog-master.yaml` by appending an `include` entry in the same format as existing entries.

4. Read back both files and confirm the registration looks correct.

5. Remind the user to fill in the actual SQL before running migrations.
