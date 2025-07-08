# StudyGroupManager-UDP

A multithreaded, non-blocking Java client-server application using UDP for managing a collection of `StudyGroup` objects. This project supports user authentication, serialized object communication, database persistence (PostgreSQL), and concurrent request processing via thread pools and `ForkJoinPool`.

## 🚀 Features

- ✅ Java-based server & client architecture over UDP
- 🔐 User registration & authentication with SHA-1 password hashing
- 🗃️ PostgreSQL integration for persistent data storage
- ⚙️ Command system for managing StudyGroup entries (add, remove, update, etc.)
- 📦 Object serialization for request/response communication
- ⛓️ Multithreading using `ExecutorService` and `ForkJoinPool`
- 🧠 Uses Stream API for collection filtering
- 📜 Logging support (java.util.logging)

---

## 🧩 Technologies Used

- Java 17+
- Java NIO (DatagramChannel)
- java.util.concurrent (Executors, ForkJoinPool, Future)
- PostgreSQL (via JDBC)
- Object Serialization
- SHA-1 (Java MessageDigest)
- Logging (java.util.logging)

---

## 🧪 Available Commands

Examples of supported commands:

- `register` – Register new user
- `add` – Add a new StudyGroup
- `update <id>` – Update group by ID
- `remove_by_id <id>`
- `clear`
- `show`
- `info`
- `execute_script <file>`
- `add_if_min`
- `remove_lower`

> Commands can be sent from the client to the server in real-time or via script files.

---

## 🗃️ Database Schema

PostgreSQL is used for persistent storage. Schema includes:

- `users` table (with hashed passwords)
- `study_groups` table (linked to users)
- Constraints on uniqueness, types, and ownership

You can connect locally or via SSH tunnel from a machine outside the database host.

```bash
# Example SSH Tunnel
ssh -L 5432:pg:5432 s408076@helios.cs.ifmo.ru
```


🔄 Multithreading & Concurrency
ExecutorService (cached thread pool) handles I/O tasks (receiving requests)

- ForkJoinPool used to process each command asynchronously

- FutureManager tracks long-running tasks and matches results to clients

- Thread-safe communication using synchronized, volatile, and concurrent collections

⚙️ How It Works
git clone https://github.com/AbdelazizBenChamekh/lab500.git
cd lab500
java -jar server.jar




