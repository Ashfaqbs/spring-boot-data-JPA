---

## 🔁 Flow Breakdown: Thread-Level + JPA What’s Happening

### 🧠 Situation:
we have:
- A Spring Boot API
- `@Transactional` service
- `Product` entity with `@Version` (optimistic lock)
- Two threads simulating two users updating stock

---

## ✅ 1. What Happens When we Call the API?

### 🔧 we hit this:
```http
POST /update-stock?productId=1&quantity=10
```

### 🔩 Spring Boot Internally Does:
1. Creates **two separate HTTP request threads** (or simulates it using `Thread t1`, `t2`).
2. Each thread calls `productService.updateStock(1, 10);`.

---

## 🧵 THREAD 1 FLOW (let’s call it **T1**):
1. Enters the method under `@Transactional` → Spring opens a **Hibernate session** + **DB transaction**.
2. Calls `findById(1)`:
    - JPA tries `SELECT * FROM product WHERE id = 1`.
    - we don’t have any product in the DB → it throws `RuntimeException("Product not found")`.
3. Transaction is rolled back (since exception thrown).
4. Thread dies with message: `Update failed: Product not found`.

---

## 🧵 THREAD 2 FLOW (T2):
- Same steps as above → hits DB → product not found → same exception.

---

## 🧠 Why This Error?

we never inserted a product with `id = 1`.

### Run This SQL in Postgres:
```sql
INSERT INTO productol (name, stock, version)
VALUES ('Laptop', 100, 0);
```

📝 our table name is `productol`, so it should match our entity name. we may need to use `@Table(name = "productol")` on our entity.

---

## 🔬 What Happens When Product Exists?

Once a row is there, and we hit the API:

1. T1 starts a transaction, loads product row, caches version `v0`.
2. T2 starts a transaction, also loads the row, caches `v0`.
3. T1 updates `stock`, calls `save()`, Hibernate does:
   ```sql
   UPDATE productol SET stock = stock + 10, version = 1 WHERE id = 1 AND version = 0
   ```
    - Since version matches, it updates.
4. T2 tries the same update:
   ```sql
   ... WHERE id = 1 AND version = 0
   ```
    - But version is now `1` in DB → **no row matched** → Hibernate throws `OptimisticLockException`.

That’s how optimistic lock works: it trusts, then **verifies before update**.

---

## 🚀 Recap Thread Flow + JPA:

| Step | Thread 1 | Thread 2 |
|------|----------|----------|
| 1 | Start transaction | Start transaction |
| 2 | Load entity with version = 0 | Load entity with version = 0 |
| 3 | Update + version = 1 | Tries update + fails (version mismatch) |
| 4 | Commit | Rollback |

---

Want to do a **real-time run?** Just insert that row and re-trigger the API.  
---


---

## 🔥 When to Use Optimistic

---

### ✅ **1. Optimistic Locking – “Rare Conflicts, High Read Scenarios”**

💡 **Use it when**:
- **Conflicts are rare**, but we still want **data integrity**.
- we have **many readers, fewer writers**.
- we want **maximum performance** without locking DB rows.

📦 **Use case**:
> **E-commerce Inventory Update (concurrent checkouts)**  
Users are checking out at the same time. Let’s say there's **1 item left in stock**.
- Two users try to buy it at the same time.
- Both load the product with `stock=1`, version `v0`.
- One succeeds, version becomes `v1`.
- The second user fails — boom — we catch it, show: “Sorry, out of stock.”

🧠 **Why optimistic?**  
It’s fast, non-blocking, good for high-traffic apps.

---

## 📊 When to Pick Which One?

| Factor              | Optimistic Locking               | Pessimistic Locking               |
|---------------------|----------------------------------|----------------------------------|
| Conflicts Expected? | No (rare)                        | Yes (frequent)                   |
| Performance         | High                             | Medium to Low                    |
| Blocking?           | No (fail fast)                   | Yes (waits, locks row)           |
| Use Case            | Inventory updates, Profile edits | Money transfers, Seat booking    |

---

### 💥 Real-World Analogy:

- **Optimistic Locking** is like *Google Docs* → two people edit, last one to save wins, others get “conflict detected.”
- **Pessimistic Locking** is like a *library book* → only one person can borrow at a time.

---

### 🚨 Bonus Tip:
**Spring Data JPA** throws `OptimisticLockException` when version mismatch, and `PessimisticLockException` when thread is blocked too long (timeout).

---


Code output:
when data is absent 

SELECT id, "name", stock, "version"
no data

api call
localhost:8080/update-stock?productId=1&quantity=10 

response 
api response
Triggered two updates
app loggs
Update failed: Product not found
Update failed: Product not found



when data is present

SELECT id, "name", stock, "version"
FROM public.productol;
INSERT INTO productol (name, stock, version) VALUES ('iPhone 15', 100, 0);
commit;

1	iPhone 15	100	0


api call
localhost:8080/update-stock?productId=1&quantity=10

reponse
api response
Triggered two updates
app logs

Updated stock successfully
Update failed: Row was updated or deleted by another transaction (or unsaved-value mapping was incorrect): [com.ashfaq.examples.locks.OptimisticLocking.ProductOL#1]