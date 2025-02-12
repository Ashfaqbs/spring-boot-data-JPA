
## **Scenario Setup**
We have two entities:  

1. `Person`  
2. `Address`  

### **Entity Relationship:**
- `Person` has a **one-to-one** relationship with `Address`.  
- `CascadeType.ALL` is applied on the relationship.

### **Understanding CascadeType.ALL**
When we use `CascadeType.ALL`, it means that **any operation (persist, merge, remove, refresh, detach) performed on the parent (`Person`) will be cascaded to the child (`Address`)**.

---

## **Table Structure (Before Saving Any Data)**  

| Person |  
|--------|  
| id (PK) | name | address_id (FK) |  
|----|------|------------|  
  

| Address |  
|---------|  
| id (PK) | street | city |  
|----|--------|----|  

---

## **Scenario 1: Saving Both Address and Person Explicitly**  

### **Code Execution:**
```java
Person person = new Person();
person.setName("Ashfaq");

Address address = new Address();
address.setStreet("MG Road");
address.setCity("Bangalore");

person.setAddress(address);

// Saving both explicitly
addressRepository.save(address);  
personRepository.save(person);
```
---

### **What Happens in the Database?**
1. **Saving Address:**  
   - A row is inserted into the `Address` table first.
   - Let's assume the new `id` generated for this row is `1`.

| Address |  
|----|--------|-----------|  
| 1  | MG Road | Bangalore |  

2. **Saving Person:**  
   - A row is inserted into the `Person` table with `address_id = 1`.

| Person |  
|----|------|------------|  
| 1  | Ashfaq | 1          |  

---
## **Scenario 2: Saving Only Person Without Saving Address Explicitly**
### **Code Execution:**
```java
Person person = new Person();
person.setName("Rahul");

Address address = new Address();
address.setStreet("Indiranagar");
address.setCity("Bangalore");

person.setAddress(address);

// Saving only the person
personRepository.save(person);
```

---

### **What Happens in the Database?**
- Since `CascadeType.ALL` is applied, **saving `Person` will automatically save `Address` as well**.
- The new `Address` gets inserted first (let’s assume it gets `id = 2`).
- The `Person` row is inserted with `address_id = 2`.

| Address |  
|----|-----------|------------|  
| 1  | MG Road  | Bangalore  |  
| 2  | Indiranagar | Bangalore |  

| Person |  
|----|------|------------|  
| 1  | Ashfaq | 1          |  
| 2  | Rahul  | 2          |  

---
## **Scenario 3: Saving Person Without Assigning Address**
### **Code Execution:**
```java
Person person = new Person();
person.setName("John");

// Saving only the person
personRepository.save(person);
```

---

### **What Happens in the Database?**
- No new row in `Address` table.
- A new `Person` is inserted with `NULL` in `address_id`.

| Address |  
|----|-----------|------------|  
| 1  | MG Road  | Bangalore  |  
| 2  | Indiranagar | Bangalore |  

| Person |  
|----|------|------------|  
| 1  | Ashfaq | 1          |  
| 2  | Rahul  | 2          |  
| 3  | John   | NULL       |  

---
## **Key Takeaways**
1. **Explicitly Saving Both Entities (Scenario 1)**  
   - Both are saved separately.
   - The foreign key is manually set.

2. **Saving Only `Person` (Scenario 2)**  
   - Since `CascadeType.ALL` is enabled, the related `Address` is also persisted automatically.

3. **Saving `Person` Without `Address` (Scenario 3)**  
   - The `Person` is saved, but `address_id` is `NULL` since no `Address` is linked.

---
### **Why Does Cascade Matter?**
- If `CascadeType.ALL` was **not** used, Scenario 2 would have thrown an exception because `Person` references `Address`, but `Address` wouldn't be persisted automatically.
- With `CascadeType.ALL`, Hibernate ensures the dependent entity (`Address`) is also saved when `Person` is saved.

---
### **Additional Experiments**
If we now delete a `Person`, the corresponding `Address` will also be deleted automatically due to `CascadeType.ALL`. we can test this with:

```java
personRepository.deleteById(1);
```

This will remove:
- The `Person` with `id = 1`.
- The corresponding `Address` with `id = 1`.

---
### **Final Thoughts**
Understanding cascade types is crucial when working with JPA to avoid unnecessary explicit saves and ensure correct entity lifecycle management.

