# 📄 Data Transfer Documentation — PostgreSQL Chunking Strategy

## 1. Current Situation

* **Source table:** `source_schema.sample_data`

* **Target table:** `target_schema.sample_data`

* **Row count (COUNT(\*)):** \~1,100,000 rows

* **Table size (pg\_total\_relation\_size):** \~94 MB

* **Average row size:**

  ```
  94 MB / 1.1M rows ≈ 85 bytes per row (on disk)
  ```

* **Heap configured (JVM -Xmx):** 500 MB (example limit)

* **Spring Boot Service:** Reads from source table in **pages (chunks)** and writes into target schema.

---

## 2. Why We Need Chunking

If we used a single `SELECT *` / `findAll()`:

* **Disk size:** 94 MB → seems small.
* **In-memory object size:**

    * Each row \~200 bytes after expansion into Java objects (`Long`, `String`, object headers, references).
    * 1.1M rows × \~200 bytes ≈ **220 MB** heap.
    * With List + persistence overhead, could easily cross **250–300 MB** heap.

👉 Currently, this *might just work*.
👉 But if the table grows 10x or 100x, heap will overflow (OutOfMemoryError).

**Chunking avoids this by only holding a slice of data at once.**

---

## 3. Our Current Chunking Strategy

We implemented **rows-per-chunk calculation** based on table size and heap constraints:

* **Step 1:** Compute `avgRowSize = totalBytes / totalRows`
* **Step 2:** Estimate heap cost per row = `avgRowSize × expansionFactor (≈ 3x)`
* **Step 3:** Decide safe target heap per chunk (e.g., 50 MB)
* **Step 4:** Compute rows per chunk = `targetHeap / perRowHeap`
* **Step 5:** Clamp between `MIN_ROWS` (5k) and `MAX_ROWS` (250k)

For our current table:

* **avgRowSize:** \~85 bytes (disk)
* **perRowHeap:** \~255 bytes (with expansion factor 3x)
* **targetHeap:** 50 MB
* **rowsPerChunk:** \~200k rows (≈ 17 MB disk, \~50 MB heap)
* **pages:** 1,100,000 ÷ 200k ≈ 6 chunks


Perfect — let’s expand the doc with a **section about the two kinds of chunking you’re asking about**:

* **Row-based chunking (PageRequest / LIMIT/OFFSET)**
* **Fetch-size–based streaming (cursor-level)**
* **Size-based (MB target)**

## 3a. Chunking Options

There are **three practical ways** chunking can be done in PostgreSQL + Spring Boot:

### 1. **Chunking by Rows (our current method)**

* Use `PageRequest` or `LIMIT/OFFSET`.
* Example: 200k rows per page.
* Each chunk is predictable in *number of rows*, but size (MB) varies if rows are wider/narrower.
* Heap safety comes from bounding rows.

### 2. **Chunking by JDBC Fetch Size (streaming)**

* Set `statement.setFetchSize(N)` (e.g., 5,000).
* PostgreSQL sends rows in batches of N from the server.
* **But**:

    * With `findAll()`, Hibernate will still accumulate all rows in heap eventually.
    * To benefit, you must stream with `Query.stream()` or iterate, not build one giant `List`.
* Good for **true streaming** pipelines where you process row-by-row.

### 3. **Chunking by Data Size (MB target)**

* Estimate row size from `pg_total_relation_size / COUNT(*)`.
* Choose target MB (e.g., 50 MB ≈ 200k rows here).
* More intuitive because it aligns directly with heap budget.
* This is essentially what we’ve implemented: “rows per chunk” derived from MB target.
---

## 4. How to Adjust When Volume Increases

### Example: 10M rows (850 MB on disk)

* avgRowSize ≈ 85 B
* rowsPerChunk ≈ 200k rows
* chunks = 10,000,000 ÷ 200,000 ≈ 50 chunks
  ✅ Still manageable with 500 MB heap.

### Example: 100M rows (8.5 GB on disk)

* chunks = 100,000,000 ÷ 200,000 ≈ 500 chunks
  ✅ Works fine (takes longer, but stable).

### Example: 1B rows (85 GB on disk)

* chunks = \~5000
  ⚠️ Runtime may be long → need **parallelism** (multi-threaded / multi-node chunk processing).

---

## 5. How to Align With JVM Heap

* **Heap size (-Xmx)**: define safe fraction for chunks (e.g., 10–20% of heap per chunk).

    * If heap = 500 MB → chunk target = 50 MB.
    * If heap = 2 GB → chunk target = 200 MB.

Formula:

```
rowsPerChunk = (targetHeapBytes) / (avgRowSize × expansionFactor)
```

Where:

* `avgRowSize` from `pg_total_relation_size / COUNT(*)`
* `expansionFactor` \~2–4x (depends on Java object overhead)

---

## 6. When Table Sizes Explode Further

* **Option A: Key Range Splitting**
  Use `WHERE id BETWEEN X AND Y` instead of offset/limit, allows parallel transfers.

* **Option B: Streaming (Cursor Fetch)**
  Use JDBC fetch size to stream rows without materializing all in memory.

* **Option C: Parallel Workers**
  Multiple threads or Flink tasks, each processing different ranges.

* **Option D: Hybrid Strategy**
  Combine chunking + streaming for ultra-large tables.

---

## 7. Checklist for Production

✅ Always log: total rows, table size, avg row size, chosen chunk size
✅ Never rely on default `findAll()` for large tables
✅ Set a heap-aware formula for rows per chunk
✅ Clamp chunk sizes to avoid micro-chunks or jumbo-chunks
✅ For 100M+ rows: introduce parallelism or streaming

---

⚡ **Summary:**
We are currently chunking **\~200k rows (\~17 MB disk, \~50 MB heap) per page**. This keeps heap stable under 500 MB. Without chunking, the current table might work but larger tables will crash. By tying chunk size calculation to `pg_total_relation_size` and JVM heap, we get an adaptive and future-proof strategy.
