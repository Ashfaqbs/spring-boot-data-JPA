
## 🧠 SCENARIO SETUP (What we simulated)

We wanted to **simulate two users** buying the **same item** (only 1 quantity available).  
So we:

- Used a REST API `/inventory/buy/{id}` to **buy** the item (reduce quantity)
- Inserted just **1 item** in the DB
- Triggered two **parallel threads** via another API `/inventory/simulate`  
  → Simulating two people clicking "Buy Now" at once.

Our goal was to **avoid race conditions** and ensure **correct data update**.

---

## 🔁 FLOW FROM ENTRYPOINT TO DATABASE

### 1. `POST /inventory/simulate` (Simulate Two Users Buying)

- Starts two threads using `ExecutorService`
- Each thread hits `/inventory/buy/1` using `RestTemplate`

```java
restTemplate.postForObject("http://localhost:8080/inventory/buy/1", null, String.class);
```

---

### 2. Thread Hits `/inventory/buy/{id}`

Each thread hits this controller endpoint:

```java
@PostMapping("/inventory/buy/{id}")
public ResponseEntity<String> buyItem(@PathVariable Long id) {
    return inventoryService.buyItem(id);
}
```

Calls service layer...

---

### 3. `InventoryService.buyItem(id)`

Here’s where the logic kicks in:

```java
@Transactional
public ResponseEntity<String> buyItem(Long id) {
    InventoryItem item = inventoryRepository.findByIdWithLock(id).orElse(null);
    if (item == null) return ResponseEntity.status(404).body("Item not found");

    if (item.getQuantity() > 0) {
        item.setQuantity(item.getQuantity() - 1);
        inventoryRepository.save(item);
        return ResponseEntity.ok("Purchase successful");
    } else {
        return ResponseEntity.status(400).body("Item out of stock");
    }
}
```

---

### 4. ⚠️ JPA Method Uses Lock

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT i FROM InventoryItem i WHERE i.id = :id")
Optional<InventoryItem> findByIdWithLock(@Param("id") Long id);
```

This tells **JPA/Hibernate**:

> “Hey man, before reading this row, place a DB lock on it. Don’t let other transactions touch it until I’m done.”


That quote — _"Hey man, before reading this row, place a DB lock on it. Don’t let other transactions touch it until I’m done."_ — is essentially how **pessimistic locking** works conceptually.

It’s **Hibernate + JPA** that tell the database this via the annotation:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
```

That translates into:

```sql
SELECT * FROM inventory_item WHERE id = ? FOR UPDATE;
```

This is sent by Hibernate to the **PostgreSQL engine**, which is the one actually locking the row. So:

- **JPA/Hibernate** is the messenger (sends lock instructions).
- **PostgreSQL** is the enforcer (locks the row physically until transaction completes).


🔒 It uses **`SELECT ... FOR UPDATE`** under the hood in Postgres.

---

### 🔄 What Happens at Thread Level?

- **Thread A** calls `buy/1`, hits DB, locks the row
- **Thread B** tries `buy/1`, but it’s **forced to wait** until Thread A commits/rolls back
- If Thread A reduced quantity from 1 → 0, then Thread B will read that and return "Out of stock"
- Both threads **don't corrupt the data**, because locking prevents simultaneous update

---

## 🤜 USE CASE SCENARIO (When do we use this)

Imagine:

- **E-commerce stock**: only 1 item left, 2 people buying
- **Bank account**: 2 withdrawals at the same time
- **Ticket booking system**: last seat on the bus/train

If we don’t use pessimistic lock → race condition → **overselling**, **negative balance**, etc.

---

## ✅ What Did We See in Our Simulation?

- 1st thread: `"Purchase successful"`
- 2nd thread: `"Item out of stock"`

Meaning pessimistic lock **worked**:
- **One thread entered the critical section**
- **Second waited for the lock**
- **Read the updated state**
- Returned correct result

No double sell. No inconsistent DB.

---

## 🔎 Summary

| Layer         | Role                                                                 |
|---------------|----------------------------------------------------------------------|
| `/simulate`   | Triggers concurrent requests                                          |
| Threads       | Each simulates an API user                                           |
| `/buy/{id}`   | Processes the purchase logic                                         |
| Service Layer | Grabs the row with pessimistic lock                                  |
| DB            | Pessimistically locks the row (`SELECT ... FOR UPDATE`)              |
| Outcome       | 1 user buys successfully, 1 gets "Out of stock" — no race condition  |

---

output: 


DB insert a record 

INSERT INTO inventory_item(id, name, quantity) VALUES (1, 'Macbook', 1);
commit;
SELECT id, "name", quantity
FROM public.inventory_item;
id|name   |quantity|
--+-------+--------+
1|Macbook|       1|


http://localhost:8080/inventory/simulate

Item out of stock
Purchase successful




---

## 🔥 When to Use Optimistic vs Pessimistic Locking (With Situations)

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

### ✅ **2. Pessimistic Locking – “High Risk Conflicts, Critical Updates”**

💡 **Use it when**:
- we **can’t afford failure** in update.
- we expect **frequent write conflicts**.
- We're doing **financial transactions**, **booking systems**, etc.

📦 **Use case**:
> **Banking - Transfer Funds**  
Two users are transferring from the **same account** simultaneously.

Thread 1: tries to deduct ₹10,000  
Thread 2: also tries to deduct ₹10,000  
If both run without locking → **data corruption** (overdrawn balance).

So, we use:
```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
```
Now, first thread locks the row, **second one waits** until it’s free.

🧠 **Why pessimistic?**  
we want *guaranteed consistency*, even at the cost of performance.

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