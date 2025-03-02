## Understanding @IdClass in JPA

- When an entity has a composite key (multiple columns as a primary key), we can use @IdClass.
- The class referenced in @IdClass should have fields matching the primary key columns in the entity.
- The existsById() method in Spring Data JPA is used to check if a record exists based on its primary key.


- Call the endpoint:

http://localhost:8080/entity/exists?id1=1&id2=A123

```
Output:

true
```