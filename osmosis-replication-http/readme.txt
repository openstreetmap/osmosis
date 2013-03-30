Notes for enhancements to replication.

Split current --replicate-apidb task into two halves.
* One half reads from the database and maintains a state file with transaction details.
 * No sequence number maintained here.
 * Move into the Osmosis-ApidbReplication project.
* Second half writes to change files and writes state files with timestamp only.
 * Sequence number allocated sequentially and written to a global state file.
 * Move into the Osmosis-Replicate project.
 * Starts web server (optional).
 * Web server responds to URL http://<server>:<port>/sequenceNumber with current state number.
 * Web server responds to URL http://<server>:<port>/sequenceNumber/upToDate with current state number and keeps sending updated state numbers as they become available.

Create a new --serve-replication task.
* Connects to the above web server to track current sequence number.
* Starts web server.
* Web server responds to URL http://<server>:<port>/replicate with current state number.
