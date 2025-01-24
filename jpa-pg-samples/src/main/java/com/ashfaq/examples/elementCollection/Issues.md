Error :

2025-01-24T18:19:25.371+05:30  INFO 32296 --- [jpa-pg-samples] [  restartedMain] c.a.examples.JpaPgSamplesApplication     : Started JpaPgSamplesApplication in 11.15 seconds (process running for 12.187)
Employee ID: 1
Employee Name: John Doe
2025-01-24T18:19:25.962+05:30  INFO 32296 --- [jpa-pg-samples] [  restartedMain] .s.b.a.l.ConditionEvaluationReportLogger :

Error starting ApplicationContext. To display the condition evaluation report re-run your application with 'debug' enabled.
2025-01-24T18:19:25.994+05:30 ERROR 32296 --- [jpa-pg-samples] [  restartedMain] o.s.boot.SpringApplication               : Application run failed

org.hibernate.LazyInitializationException: failed to lazily initialize a collection of role: com.ashfaq.examples.elementCollection.Employee.skills: could not initialize proxy - no Session
at org.hibernate.collection.spi.AbstractPersistentCollection.throwLazyInitializationException(AbstractPersistentCollection.java:635) ~[hibernate-core-6.6.5.Final.jar:6.6.5.Final]
at org.hibernate.collection.spi.AbstractPersistentCollection.withTemporarySessionIfNeeded(AbstractPersistentCollection.java:219) ~[hibernate-core-6.6.5.Final.jar:6.6.5.Final]
at org.hibernate.collection.spi.AbstractPersistentCollection.initialize(AbstractPersistentCollection.java:614) ~[hibernate-core-6.6.5.Final.jar:6.6.5.Final]
at org.hibernate.collection.spi.AbstractPersistentCollection.read(AbstractPersistentCollection.java:138) ~[hibernate-core-6.6.5.Final.jar:6.6.5.Final]
at org.hibernate.collection.spi.PersistentBag.toString(PersistentBag.java:587) ~[hibernate-core-6.6.5.Final.jar:6.6.5.Final]
at java.base/java.lang.String.valueOf(String.java:4556) ~[na:na]
at com.ashfaq.examples.elementCollection.EmployeeService.lambda$testEmployeeQuery$0(EmployeeService.java:43) ~[classes/:na]
at java.base/java.util.ArrayList.forEach(ArrayList.java:1597) ~[na:na]
at com.ashfaq.examples.elementCollection.EmployeeService.testEmployeeQuery(EmployeeService.java:40) ~[classes/:na]
at com.ashfaq.examples.elementCollection.AppRunner1.run(AppRunner1.java:17) ~[classes/:na]
at org.springframework.boot.SpringApplication.lambda$callRunner$5(SpringApplication.java:788) ~[spring-boot-3.4.2.jar:3.4.2]
at org.springframework.util.function.ThrowingConsumer$1.acceptWithException(ThrowingConsumer.java:82) ~[spring-core-6.2.2.jar:6.2.2]
at org.springframework.util.function.ThrowingConsumer.accept(ThrowingConsumer.java:60) ~[spring-core-6.2.2.jar:6.2.2]
at org.springframework.util.function.ThrowingConsumer$1.accept(ThrowingConsumer.java:86) ~[spring-core-6.2.2.jar:6.2.2]
at org.springframework.boot.SpringApplication.callRunner(SpringApplication.java:796) ~[spring-boot-3.4.2.jar:3.4.2]
at org.springframework.boot.SpringApplication.callRunner(SpringApplication.java:787) ~[spring-boot-3.4.2.jar:3.4.2]
at org.springframework.boot.SpringApplication.lambda$callRunners$3(SpringApplication.java:772) ~[spring-boot-3.4.2.jar:3.4.2]
at java.base/java.util.stream.ForEachOps$ForEachOp$OfRef.accept(ForEachOps.java:184) ~[na:na]
at java.base/java.util.stream.SortedOps$SizedRefSortingSink.end(SortedOps.java:357) ~[na:na]
at java.base/java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:571) ~[na:na]
at java.base/java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:560) ~[na:na]
at java.base/java.util.stream.ForEachOps$ForEachOp.evaluateSequential(ForEachOps.java:151) ~[na:na]
at java.base/java.util.stream.ForEachOps$ForEachOp$OfRef.evaluateSequential(ForEachOps.java:174) ~[na:na]
at java.base/java.util.stream.AbstractPipeline.evaluate(AbstractPipeline.java:265) ~[na:na]
at java.base/java.util.stream.ReferencePipeline.forEach(ReferencePipeline.java:636) ~[na:na]
at org.springframework.boot.SpringApplication.callRunners(SpringApplication.java:772) ~[spring-boot-3.4.2.jar:3.4.2]
at org.springframework.boot.SpringApplication.run(SpringApplication.java:325) ~[spring-boot-3.4.2.jar:3.4.2]
at org.springframework.boot.SpringApplication.run(SpringApplication.java:1361) ~[spring-boot-3.4.2.jar:3.4.2]
at org.springframework.boot.SpringApplication.run(SpringApplication.java:1350) ~[spring-boot-3.4.2.jar:3.4.2]
at com.ashfaq.examples.JpaPgSamplesApplication.main(JpaPgSamplesApplication.java:10) ~[classes/:na]
at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[na:na]
at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[na:na]
at org.springframework.boot.devtools.restart.RestartLauncher.run(RestartLauncher.java:50) ~[spring-boot-devtools-3.4.2.jar:3.4.2]

2025-01-24T18:19:26.009+05:30  INFO 32296 --- [jpa-pg-samples] [  restartedMain] o.s.b.w.e.tomcat.GracefulShutdown        : Commencing graceful shutdown. Waiting for active requests to complete
2025-01-24T18:19:26.019+05:30  INFO 32296 --- [jpa-pg-samples] [tomcat-shutdown] o.s.b.w.e.tomcat.GracefulShutdown        : Graceful shutdown complete
2025-01-24T18:19:26.023+05:30  INFO 32296 --- [jpa-pg-samples] [  restartedMain] j.LocalContainerEntityManagerFactoryBean : Closing JPA EntityManagerFactory for persistence unit 'default'
2025-01-24T18:19:26.030+05:30  INFO 32296 --- [jpa-pg-samples] [  restartedMain] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown initiated...
2025-01-24T18:19:26.039+05:30  INFO 32296 --- [jpa-pg-samples] [  restartedMain] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown completed.

Process finished with exit code 0




### Resolution

The error you are encountering, `LazyInitializationException: failed to lazily initialize a collection`, occurs because Hibernate tries to fetch the `skills` collection lazily after the `Session` has been closed. By default, `@ElementCollection` collections are loaded lazily, and once the transaction or session is closed, the proxy cannot initialize the collection.

Here's how you can resolve this issue:

---

### 1. **Eager Fetching for `@ElementCollection`:**
Change the fetch type of `skills` to `EAGER`. This ensures the collection is fetched along with the `Employee` entity.

   ```java
   @Entity
   public class Employee {
       @Id
       @GeneratedValue(strategy = GenerationType.IDENTITY)
       private Long id;

       private String empCode;

       private String name;

       @ElementCollection(fetch = FetchType.EAGER)
       @CollectionTable(name = "employee_skills", joinColumns = @JoinColumn(name = "employee_id"))
       private List<String> skills;

       // Getters and Setters
   }
   ```

However, eager fetching can lead to performance issues if the collection is large or if you don't always need the `skills`.

---

### 2. **Explicit Initialization in Service Layer:**
Keep `FetchType.LAZY` but explicitly initialize the `skills` collection within a transaction.

   ```java
   @Service
   public class EmployeeService {
       @Autowired
       private EmployeeRepository employeeRepository;

       public void testEmployeeQuery() {
           List<Employee> employees = employeeRepository.findAll(); // Or your custom query

           // Initialize lazy collections
           employees.forEach(employee -> {
               Hibernate.initialize(employee.getSkills());
           });

           employees.forEach(employee -> {
               System.out.println("Employee ID: " + employee.getId());
               System.out.println("Employee Name: " + employee.getName());
               System.out.println("Employee Skills: " + employee.getSkills());
           });
       }
   }
   ```

The `Hibernate.initialize` method forces the initialization of lazy-loaded collections.

---

### 3. **Using DTOs or Projections:**
Fetch only the required data using a DTO or projection query. This avoids the need for `@ElementCollection` altogether.

   ```java
   public interface EmployeeSkillProjection {
       String getEmpCode();
       String getName();
       String getSkill();
   }
   ```

   ```java
   @Query("SELECT e.empCode as empCode, e.name as name, s as skill FROM Employee e JOIN e.skills s WHERE e.empCode = :empCode AND s = :skill")
   List<EmployeeSkillProjection> findByEmpCodeAndSkill(@Param("empCode") String empCode, @Param("skill") String skill);
   ```

This approach retrieves only the necessary information without involving lazy-loading issues.

---

### 4. **Enable OpenSessionInView Filter:**
Configure `OpenSessionInView` in your Spring application. This keeps the Hibernate `Session` open for the entire request-response cycle, allowing lazy-loaded collections to be initialized.

Add this to your application properties:
   ```properties
   spring.jpa.open-in-view=true
   ```

**Note:** This approach should be used cautiously, as it can lead to performance issues in larger applications.

---

#### Recommended Solution:
If the `skills` list is relatively small, **Eager Fetching** (Option 1) is the simplest fix. If performance is a concern, **DTOs/Projections** (Option 3) are more efficient and scalable.

---
