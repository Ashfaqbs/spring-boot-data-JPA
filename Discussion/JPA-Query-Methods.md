## Intro 

ref link : https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html

## 1. How `findByName` Works Internally

When we write:

```java
Payment findByName(String name);
```

Spring Data JPA does the following under the hood:

1. **Parse Method Name**

   * It looks at `findByName` and splits it into:

     * **Prefix**: `findBy` (means SELECT query).
     * **Property**: `Name`.

2. **Match Property to Entity Fields**

   * JPA inspects our entity class (`Payment`).
   * It looks for a field named `name` (or a getter like `getName()`).
   * It finds our `private String name;` field.
   * ✅ So it matches correctly.

3. **Generate SQL Query**

   * Spring generates a JPQL query like:

     ```sql
     SELECT p FROM Payment p WHERE p.name = :name
     ```
   * This is converted into native SQL:

     ```sql
     SELECT * FROM payment WHERE name = ?;
     ```

So **we don’t need to write SQL** — Spring builds it from the method name.

---

## 2. What if the Field is Named Differently? (`fullName` Example)

Suppose our entity had:

```java
@Column(name = "full_name")
private String fullName;
```

Now if we want to query by it, our repository method would be:

```java
Payment findByFullName(String fullName);
```

Spring will:

* Match `FullName` part of the method name with the entity’s **Java field** `fullName`.
* Ignore the actual DB column name (`full_name`). The `@Column(name="...")` is only for mapping Java field ↔ DB column, not for query derivation.
* Generate SQL:

  ```sql
  SELECT * FROM payment WHERE full_name = ?;
  ```

---

## 3. What About Long Names? (`fullAddressIncludingPincode` Example)

If we have:

```java
@Column(name = "full_address_including_pincode")
private String fullAddressIncludingPincode;
```

Repository method:

```java
Payment findByFullAddressIncludingPincode(String address);
```

* Spring matches `FullAddressIncludingPincode` in the method name with our Java field `fullAddressIncludingPincode`.
* It generates JPQL based on entity property, not DB column name.

---

## 4. Key Rule to Remember

👉 **Spring Data JPA always uses entity field names (Java variables) when parsing repository method names.**
Not the DB column names.

* If our field is `fullName`, we must use `findByFullName`.
* If our field is `cardNumber`, we must use `findByCardNumber`.

The `@Column(name="...")` only affects how the field is mapped to the DB, not how repository queries are derived.

---

## 5. Alternatives When Names Get Too Long

If field names become huge (`fullAddressIncludingPincode`), our repository method name will also become huge:

```java
Payment findByFullAddressIncludingPincode(String address);
```

To avoid verbose names:

* **Use `@Query` annotation**:

  ```java
  @Query("SELECT p FROM Payment p WHERE p.fullAddressIncludingPincode = :address")
  Payment findByAddress(@Param("address") String address);
  ```
* Or **use Specification/Criteria API** if queries are complex.

---


## Internals 

Here’s how Spring Data JPA wires a method like `PaymentRepository#findByName(String name)` all the way down to SQL—and how naming, long property names, and more complex operators are handled under the hood.

# 1) Boot-time wiring (repositories → proxies)

1. Spring finds our `PaymentRepository` because it extends `JpaRepository`.
2. `JpaRepositoryFactory` creates a **JDK dynamic proxy** for that interface.

   * Target class: `SimpleJpaRepository<Payment, Integer>` (the default base class).
   * The proxy intercepts every repository method call and routes it through a **`QueryLookupStrategy`**.

# 2) How Spring decides “what query to run”

`QueryLookupStrategy` chooses one of three strategies (configurable via `spring.data.jpa.repositories.bootstrap-mode` / XML / Java config):

* **USE\_DECLARED\_QUERY**: If there’s an `@Query` on the method (JPQL/native), use it.
* **CREATE**: Otherwise, try to **derive** the query from the method name.
* **CREATE\_IF\_NOT\_FOUND**: Prefer `@Query`, else derive.

For derived queries, Spring builds a **`QueryMethod`**/`JpaQueryMethod` model for our method (return type, parameters, pageable/sort flags, etc.), then parses the **method name** with a `PartTree`.

# 3) Parsing the method name (derivation engine)

The derivation engine takes our method name and splits it into semantic parts:

* **Prefix**: `findBy`, `readBy`, `getBy`, `existsBy`, `countBy`, `deleteBy`, `removeBy`, `findTop10By`, etc.
* **Predicate**: One or more property parts joined by `And` / `Or` with operators like `Is`, `Equals`, `Containing` (LIKE %x%), `StartingWith`, `EndingWith`, `GreaterThan`, `LessThan`, `Between`, `True`, `False`, `In`, `NotIn`, `Before`, `After`, `IgnoreCase`, etc.
* **Query modifiers**: `Distinct`, `Top`, `First`, plus optional `OrderBy<Field>Asc/Desc`.

Example (long name included):

```java
Payment findTop5ByFullAddressIncludingPincodeIgnoreCaseAndBillValueGreaterThanOrderByBillValueDesc(
    String address, int minBill);
```

Parsed as:

* limit: `Top5`
* predicate 1: property `fullAddressIncludingPincode` with `IgnoreCase` and operator `Equals`
* predicate 2: property `billValue` with operator `GreaterThan`
* sort: `OrderBy billValue DESC`

If **any property** in the name doesn’t match an entity attribute (by **Java field/getter name**, not column name), startup fails with a `PropertyReferenceException`—useful early feedback.

# 4) How properties are matched

* Matching is against **entity property names** (Java field/getter), not DB column names.
* `@Column(name = "full_address_including_pincode")` only controls ORM mapping (Java ↔ SQL column); it **doesn’t** change the name the derivation engine expects.
* So:

  * Field: `private String fullName;`
  * Method: `findByFullName(...)` ✅
  * `findByFull_Name(...)` ❌ (column name is irrelevant)
* Nested paths are supported: `findByCustomer_Address_City(...)` walks associations via **JPA Metamodel**/**BeanWrapper**, creating a **`PropertyPath`** per segment.

# 5) Building the JPQL/Criteria query

After parsing, Spring creates a `JpaQueryCreator` which translates the `PartTree` into JPQL (or a Criteria query) targeting the entity model:

* Base: `SELECT p FROM Payment p`
* Predicates: `WHERE p.name = :name`
* Sorting: `ORDER BY p.billValue DESC`
* Limits: applied via JPA query hints / `setMaxResults`
* Pagination: builds **two** queries if needed—content query + count query (auto-generated unless we provide a custom one).

For `findByName` the JPQL is roughly:

```jpql
SELECT p FROM Payment p WHERE p.name = :name
```

# 6) Executing the query (JPA → provider → JDBC)

1. The repository invokes through `EntityManager` (from our `JpaTransactionManager`).
2. Provider (e.g., **Hibernate**) parses JPQL/HQL → generates SQL based on the mapped metadata.
3. Parameters are bound (positional or named).
4. SQL executes via JDBC on our `DataSource`.
5. Rows → entities using the mapping (fields, `@Column`, associations, converters).
6. Level-2 (shared) cache and Level-1 (per-EntityManager) cache may short-circuit DB hits depending on our config. Query cache is optional (provider-specific).

# 7) Transactions & flush behavior

* Read methods are, by default, `@Transactional(readOnly = true)` at the repository interface level (Spring Data sets sensible defaults).
* Write operations (`save`, `delete`) use default `@Transactional` (write).
* Flush mode matters for consistency; JPQL queries may trigger auto-flush if pending changes could affect results.

# 8) How long or “weird” names impact us

* Use **descriptive Java property names** in the entity (`fullAddressIncludingPincode`), then write `findByFullAddressIncludingPincode(...)`. Lengthy, but explicit and correct.
* If method names get unwieldy (hard to read/maintain), prefer:

  * `@Query` with JPQL:

    ```java
    @Query("select p from Payment p where lower(p.fullAddressIncludingPincode) = lower(:addr)")
    Payment findByAddress(@Param("addr") String addr);
    ```
  * **Specifications** (type-safe, composable predicates).
  * **Querydsl** (fluent, generated Q-types).

# 9) Special keywords we’ll use a lot

* **IgnoreCase**: Applies lower/upper on supported types (`String`). For “contains ignoring case”, use `ContainingIgnoreCase`.
* **Distinct**: De-duplicates results in JPQL.
* **ExistsBy**/`CountBy`: Directly produces `select count(...)` or `select case when (count > 0)`.
* **In/NotIn**: Binds collections.
* **IsNull/IsNotNull**, **True/False** for booleans.
* **OrderBy** suffix if we don’t want to pass a `Sort`.

# 10) Validation & error cases

* If a property doesn’t exist: `PropertyReferenceException` at startup.
* If an operator is incompatible (e.g., `Containing` on a non-String without converter): fails at parse or runtime.
* Ambiguity with nested paths is resolved by walking the metamodel; if still ambiguous, we’ll get a clear boot-time error.

# 11) TL;DR for our cases

* Keep our entity variables meaningful (`name`, `fullName`, `fullAddressIncludingPincode`).
* Use repository methods that **exactly** mirror the **Java property names**:

  * `findByName(String name)`
  * `findByFullName(String fullName)`
  * `findByFullAddressIncludingPincode(String value)`
* we never need to rename properties to cryptic short names like `N`. The parser keys off Java property names, not DB columns.

---

## Quick, practical patterns

**Multiple fields**

```java
Optional<Payment> findByNameAndEmail(String name, String email);
```

**Contains + ignore case**

```java
List<Payment> findByAddressContainingIgnoreCase(String fragment);
```

**Top + sort**

```java
List<Payment> findTop3ByBillValueGreaterThanOrderByBillValueDesc(int min);
```

**Nested property**

```java
List<Payment> findByCustomerAddressCity(String city);
```

**Readable alternative to a long method**

```java
@Query("""
  select p from Payment p
  where lower(p.fullAddressIncludingPincode) like lower(concat('%', :q, '%'))
  """)
List<Payment> searchAddress(@Param("q") String q);
```

---



## Examples 

Below is a compact, end-to-end mini-playground showing how Spring Data JPA parses repository method names into JPQL and then into SQL. 
Using a single simple entity, a small data set (assumed preloaded), and many query shapes across **prefixes**, **predicates**, and **modifiers**. Focus is on how the method name maps to the **Java property** names and how that becomes SQL.

---

# 0) Entity + mapping

```java
@Entity
@Table(name = "customers")
public class Customer {
  @Id @GeneratedValue(strategy = IDENTITY)
  private Long id;

  @Column(name = "full_name")
  private String name;

  @Column(name = "email")
  private String email;

  @Column(name = "city")
  private String city;

  @Column(name = "age")
  private Integer age;

  @Column(name = "active")
  private Boolean active;

  @Column(name = "signup_at")
  private LocalDateTime signupAt;

  @Column(name = "total_spent")
  private BigDecimal totalSpent;

  // getters/setters
}
```

> Key: Method-name parsing uses **Java property names** (`name`, `email`, `city`, `age`, `active`, `signupAt`, `totalSpent`) — not the DB column names.

---

# 1) Sample rows (assume pre-inserted)

| id | name        | email                                             | city      | age | active | signup\_at          | total\_spent |
| -- | ----------- | ------------------------------------------------- | --------- | --- | ------ | ------------------- | ------------ |
| 1  | Alice Jones | [alice@exa.com](mailto:alice@exa.com)             | Bengaluru | 28  | true   | 2024-12-10 10:00:00 | 250.00       |
| 2  | Bob Singh   | [bob.singh@exa.com](mailto:bob.singh@exa.com)     | Mumbai    | 35  | true   | 2023-11-01 09:00:00 | 1200.00      |
| 3  | Carol Nair  | [carol.nair@exa.com](mailto:carol.nair@exa.com)   | Bengaluru | 41  | false  | 2022-01-05 12:30:00 | 750.00       |
| 4  | David Kumar | [david.k@exa.com](mailto:david.k@exa.com)         | Delhi     | 23  | true   | 2025-02-14 18:45:00 | 90.00        |
| 5  | Eve Iyer    | [eve.iyer@exa.com](mailto:eve.iyer@exa.com)       | Hyderabad | 31  | false  | 2025-06-01 07:15:00 | 4000.00      |
| 6  | Farah Ahmed | [farah.ahmed@exa.com](mailto:farah.ahmed@exa.com) | Mumbai    | 29  | true   | 2024-07-20 16:10:00 | 600.00       |

---

# 2) Repository interface (snippets)

```java
public interface CustomerRepository extends JpaRepository<Customer, Long> {
  // we’ll fill examples below
}
```

---

# 3) Prefixes, Predicates, Modifiers — with outputs & SQL

For each example: **Method** → **Example call** → **Result** (based on sample rows) → **JPQL** → **SQL**.

## A. `findBy` / `readBy` / `getBy` (same semantics for reads)

### A1) Simple equality

**Method**

```java
Optional<Customer> findByEmail(String email);
```

**Call**
`findByEmail("bob.singh@exa.com")`

**Result** → Row id 2

**JPQL**
`select c from Customer c where c.email = :email`

**SQL**

```sql
select * from customers c where c.email = ?;
```

---

### A2) Multiple predicates with `And`

**Method**

```java
List<Customer> findByCityAndActive(String city, boolean active);
```

**Call**
`findByCityAndActive("Mumbai", true)`

**Result** → Rows id 2 and 6

**JPQL**
`select c from Customer c where c.city = :city and c.active = :active`

**SQL**

```sql
select * from customers c
where c.city = ? and c.active = ?;
```

---

### A3) `Or` with grouping (method name implies left-to-right)

**Method**

```java
List<Customer> findByCityOrAgeGreaterThan(String city, int age);
```

**Call**
`findByCityOrAgeGreaterThan("Delhi", 40)`

**Result** → Rows in Delhi (id 4) OR age > 40 (id 3 → age 41).
So ids 3 and 4.

**JPQL**
`select c from Customer c where c.city = :city or c.age > :age`

**SQL**

```sql
select * from customers c
where c.city = ? or c.age > ?;
```

---

### A4) Case-insensitive contains (LIKE %x%)

**Method**

```java
List<Customer> findByNameContainingIgnoreCase(String q);
```

**Call**
`findByNameContainingIgnoreCase("nair")`

**Result** → Row id 3 (“Carol Nair”)

**JPQL**
`select c from Customer c where lower(c.name) like lower(concat('%', :q, '%'))`

**SQL**

```sql
select * from customers c
where lower(c.full_name) like lower(concat('%', ?, '%'));
```

---

### A5) `StartingWith` and `EndingWith`

**Method**

```java
List<Customer> findByEmailEndingWith(String domain);
List<Customer> findByNameStartingWithIgnoreCase(String prefix);
```

**Calls**

* `findByEmailEndingWith("@exa.com")` → All rows (all end with @exa.com).
* `findByNameStartingWithIgnoreCase("da")` → Row id 4 (“David Kumar”).

**SQL (EndingWith)**

```sql
select * from customers c
where c.email like concat('%', ?);
```

**SQL (StartingWith IgnoreCase)**

```sql
select * from customers c
where lower(c.full_name) like lower(concat(?, '%'));
```

---

### A6) Comparisons: `GreaterThan`, `LessThan`, `Between`

**Method**

```java
List<Customer> findByAgeGreaterThan(int age);
List<Customer> findByTotalSpentBetween(BigDecimal min, BigDecimal max);
```

**Calls**

* `findByAgeGreaterThan(30)` → ids 2,3,5
* `findByTotalSpentBetween(100, 800)` → ids 1,3,6

**SQL (GreaterThan)**

```sql
select * from customers c where c.age > ?;
```

**SQL (Between)**

```sql
select * from customers c where c.total_spent between ? and ?;
```

---

### A7) Dates: `Before` / `After`

**Method**

```java
List<Customer> findBySignupAtAfter(LocalDateTime t);
List<Customer> findBySignupAtBefore(LocalDateTime t);
```

**Calls**

* `findBySignupAtAfter(2025-01-01T00:00)` → ids 4,5
* `findBySignupAtBefore(2024-01-01T00:00)` → ids 2,3

**SQL (After)**

```sql
select * from customers c where c.signup_at > ?;
```

---

### A8) `In` / `NotIn`

**Method**

```java
List<Customer> findByCityIn(Collection<String> cities);
List<Customer> findByCityNotIn(Collection<String> cities);
```

**Calls**

* `findByCityIn(["Bengaluru", "Mumbai"])` → ids 1,2,3,6
* `findByCityNotIn(["Bengaluru", "Mumbai"])` → ids 4,5

**SQL (In)**

```sql
select * from customers c where c.city in (?, ?);
```

---

### A9) Booleans: `True` / `False`

**Method**

```java
List<Customer> findByActiveTrue();
List<Customer> findByActiveFalse();
```

**Calls**

* `findByActiveTrue()` → ids 1,2,4,6
* `findByActiveFalse()` → ids 3,5

**SQL (True)**

```sql
select * from customers c where c.active = true;
```

---

## B. Existence / Counting

### B1) `existsBy`

**Method**

```java
boolean existsByEmail(String email);
```

**Call**
`existsByEmail("alice@exa.com")` → `true` (id 1)

**JPQL (provider often optimizes)**
`select count(c) from Customer c where c.email = :email`

**SQL (typical)**

```sql
select case when count(*) > 0 then 1 else 0 end
from customers c where c.email = ?;
```

---

### B2) `countBy`

**Method**

```java
long countByCity(String city);
```

**Call**
`countByCity("Mumbai")` → `2` (ids 2,6)

**SQL**

```sql
select count(*) from customers c where c.city = ?;
```

---

## C. Deletions

### C1) `deleteBy` (returns deleted count if `long`/`int` return type)

**Method**

```java
long deleteByActiveFalse();
```

**Call**
`deleteByActiveFalse()` → deletes ids 3 and 5 → returns `2`

**JPQL**
`delete from Customer c where c.active = false`

**SQL**

```sql
delete from customers where active = false;
```

> Note: Requires a transactional boundary. After deletion, those rows are gone.

### C2) `removeBy`

Equivalent semantics to `deleteBy` in Spring Data, naming preference only.

---

## D. Limits, distincts, and ordering

### D1) `findTop10By` / `findFirst…`

**Method**

```java
List<Customer> findTop3ByActiveTrueOrderByTotalSpentDesc();
```

**Call**
`findTop3ByActiveTrueOrderByTotalSpentDesc()`

**Eligible active rows**: (1:250), (2:1200), (4:90), (6:600)
Sorted desc by `totalSpent` → ids \[2,6,1] (top 3)

**JPQL**
`select c from Customer c where c.active = true order by c.totalSpent desc`

**SQL**

```sql
select * from customers c
where c.active = true
order by c.total_spent desc
fetch first 3 rows only;         -- vendor-specific: LIMIT 3 / TOP 3
```

---

### D2) `Distinct`

**Method**

```java
List<String> findDistinctCityByActiveTrue();
```

> Return projections work too; this would typically be `@Query` or an interface projection. With method derivation, `Distinct` ensures de-duplication.

**JPQL**
`select distinct c.city from Customer c where c.active = true`

**SQL**

```sql
select distinct c.city from customers c where c.active = true;
```

---

## E. Complex predicate with IgnoreCase and Between

**Method**

```java
List<Customer> findByCityIgnoreCaseAndTotalSpentBetweenOrderByAgeAsc(
    String city, BigDecimal min, BigDecimal max);
```

**Call**
`findByCityIgnoreCaseAndTotalSpentBetweenOrderByAgeAsc("mumbai", 500, 2000)`

**Matches**: Mumbai + total\_spent between 500 and 2000 → ids 2 (1200), 6 (600)
Order by age asc → \[6 (29), 2 (35)] → ids \[6, 2]

**JPQL**
`select c from Customer c where lower(c.city) = lower(:city) and c.totalSpent between :min and :max order by c.age asc`

**SQL**

```sql
select * from customers c
where lower(c.city) = lower(?)
  and c.total_spent between ? and ?
order by c.age asc;
```

---

## F. `readBy` / `getBy` synonyms

**Method**

```java
Optional<Customer> readByName(String name);
Optional<Customer> getByName(String name);
```

**Call**
`readByName("Alice Jones")` or `getByName("Alice Jones")` → id 1

**SQL**

```sql
select * from customers c where c.full_name = ?;
```

---

# 4) How the mapping is decided (quick mental model)

1. Spring parses the **method name** into a tree of parts (fields + operators).
2. It validates that each **property** exists on the **entity** (Java field/getter).
3. It builds a **JPQL** (or Criteria) query from the parts.
4. JPA provider (e.g., Hibernate) converts JPQL → **vendor SQL**, binding parameters.
5. Results are mapped back into **entities** or **projections**.

> If we rename a Java field (e.g., `signupAt` → `registeredAt`), update the method names accordingly (`findByRegisteredAtAfter(...)`). Column names via `@Column(name= "...")` do not influence method-name parsing.

---

# 5) Bonus: Short vs. long names (readability tips)

* If a name gets comically long, switch to:

  * `@Query` with JPQL (clear and explicit), or
  * **Specifications** to compose predicates (AND/OR), or
  * **Querydsl** for fluent, type-safe queries.
* Keep **entity property** names precise and meaningful; derived methods will follow naturally.

---
