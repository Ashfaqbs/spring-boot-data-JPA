package com.example.demo.chucnking_stgs.service;

import java.util.stream.Collectors;

import com.example.demo.chucnking_stgs.model.SourceData;
import org.aspectj.lang.annotation.After;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.example.demo.chucnking_stgs.model.TargetData;
import com.example.demo.chucnking_stgs.repo.SourceDataRepository;
import com.example.demo.chucnking_stgs.repo.TargetDataRepository;

import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class DataTransferService {

    @Autowired
    private SourceDataRepository sourceRepo;

    @Autowired
    private TargetDataRepository targetRepo;

    @Autowired
    private JdbcTemplate jdbc;

    // Target chunk size on *disk* (≈9 MB per fetch)
    private static final long TARGET_DISK_MB = 9L;
    private static final long TARGET_DISK_BYTES = TARGET_DISK_MB * 1024 * 1024;

    public void transferData() {
        final String FQTN = "source_schema.sample_data";

        // 1) Gather stats
        long totalRows = sourceRepo.count();
        Long totalBytes = jdbc.queryForObject(
                "SELECT pg_total_relation_size(?::regclass)", Long.class, FQTN);
        if (totalBytes == null) totalBytes = 0L;

        System.out.println("Total rows  : " + totalRows);
        System.out.println("Table size  : " + human(totalBytes));

        if (totalRows == 0) {
            System.out.println("No rows to transfer.");
            return;
        }

        // 2) Estimate average row size on disk
        long avgRowSize = Math.max(1, totalBytes / totalRows); // bytes/row
        System.out.println("Avg row size: " + avgRowSize + " B/row (disk est)");

        // 3) Compute rows-per-chunk from target ~9 MB on disk
        long rowsPerChunkL = Math.max(1, TARGET_DISK_BYTES / avgRowSize);

        // Clamp to keep it sane (avoid micro-chunks / jumbo-chunks)
        final int MIN_ROWS = 5_000;
        final int MAX_ROWS = 300_000;
        int chunkSize = (int) Math.max(MIN_ROWS, Math.min(MAX_ROWS, rowsPerChunkL));

        // Recompute the *estimated* disk size for the clamped chunk
        long estDiskPerChunk = chunkSize * avgRowSize;
        System.out.printf(
                "Chunk size  : %,d rows (target ~%d MB; est ~%s on disk)%n",
                chunkSize, TARGET_DISK_MB, human(estDiskPerChunk)
        );

        // 4) Page through and copy
        int pages = (int) Math.ceil((double) totalRows / chunkSize);
        System.out.println("Planned pages: " + pages);

        for (int page = 0; page < pages; page++) {
            var pageable = PageRequest.of(page, chunkSize, Sort.by("id").ascending());
            var chunk = sourceRepo.findAllBy(pageable);

            // actual rows this page (last page can be smaller)
            int actualRows = chunk.size();
            long actualDiskBytes = actualRows * avgRowSize;

            System.out.printf(
                    "Processing chunk %d/%d -> rows=%d (~%s on disk)%n",
                    page + 1, pages, actualRows, human(actualDiskBytes)
            );

            var targetList = chunk.stream().map(src -> {
                TargetData tgt = new TargetData();
                tgt.setId(src.getId());
                tgt.setName(src.getName());
                tgt.setValue(src.getValue());
                return tgt;
            }).collect(Collectors.toList());

            targetRepo.saveAll(targetList);
        }

        System.out.println("Transfer completed.");
    }

    // Utility to print human-readable sizes
    private static String human(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int z = (63 - Long.numberOfLeadingZeros(bytes)) / 10;
        return String.format("%.1f %sB",
                (double) bytes / (1L << (z * 10)), " KMGTPE".charAt(z));
    }
}

/*
EG :
### Step 1: Gather stats

```java
        totalRows = 1,100,000
totalBytes = 94 MB = 98,304,000 bytes
```

        ### Step 2: Estimate average row size

```java
        avgRowSize = totalBytes / totalRows
        = 98,304,000 / 1,100,000 ≈ 89 bytes/row
```

        ### Step 3: Target heap per chunk

```java
        targetHeap = 50 MB (constant = 52,428,800 bytes)
rowsPerChunk = targetHeap / avgRowSize
             = 52,428,800 / 89 ≈ 589,000 rows
```

        ### Step 4: Clamp chunk size

```java
        rowsPerChunk = 589,000 → exceeds max clamp (200,000)
chunkSize = 200,000
        ```

        ### Step 5: Calculate total pages

```java
        pages = ceil(1,100,000 / 200,000) = 6
```

        ### Step 6: Transfer loop

* **Page 1:** fetch 200,000 rows → \~17 MB on disk
* **Page 2:** fetch 200,000 rows → \~17 MB
* **Page 3:** fetch 200,000 rows → \~17 MB
* **Page 4:** fetch 200,000 rows → \~17 MB
* **Page 5:** fetch 200,000 rows → \~17 MB
* **Page 6 (last):** fetch 100,000 rows → \~8 MB

Each chunk is copied to the target schema, then released from heap before the next begins.

---

        # 3. What Happens During Transfer (in memory)

1. **Load chunk into Java list** (`List<SourceData>`)

        * Only that chunk lives in heap at a time.
2. **Map to TargetData**

        * Creates \~200k Java objects, still well under heap limit.
3. **Persist to target schema** with `saveAll()`

        * Writes in batch to DB.
4. **Release references**

        * After the loop iteration, chunk list can be garbage collected.
5. Repeat until all pages are processed.

At no point does the application hold **all 1.1M rows** — only one chunk at a time.


        ✅ End Result: Your 94 MB table is transferred in \~6 safe chunks, each \~17 MB disk (\~50 MB heap).
        Without chunking, you’d risk \~250–300 MB heap use in one go, which is fine today but not future-proof.
 */