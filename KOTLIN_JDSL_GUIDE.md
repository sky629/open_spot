# Kotlin JDSL Complete API Reference Guide

Complete guide for using Kotlin JDSL (kotlin-jdsl by LINE) with Spring Data JPA.

## Table of Contents
- [Basic Query Structure](#basic-query-structure)
- [SELECT Clause](#select-clause)
- [FROM Clause & Joins](#from-clause--joins)
- [WHERE Clause & Predicates](#where-clause--predicates)
- [GROUP BY & HAVING](#group-by--having)
- [ORDER BY](#order-by)
- [Pagination (LIMIT/OFFSET)](#pagination-limitoffset)
- [Aggregation Functions](#aggregation-functions)
- [Subqueries](#subqueries)
- [Spring Data Integration](#spring-data-integration)
- [Complete Examples](#complete-examples)

---

## Basic Query Structure

### Standard Query Pattern
```kotlin
val query = jpql {
    select(
        path(Entity::field)
    ).from(
        entity(Entity::class)
    ).where(
        // predicates
    ).groupBy(
        // grouping expressions
    ).having(
        // aggregate predicates
    ).orderBy(
        // ordering
    )
}
```

### Query Execution with EntityManager
```kotlin
val context = JpqlRenderContext()

// Method 1: Using extension functions (Recommended)
val jpaQuery: Query = entityManager.createQuery(query, context)
val result = jpaQuery.resultList

// Method 2: Manual rendering
val renderer = JpqlRenderer()
val rendered = renderer.render(query, context)
val jpaQuery: Query = entityManager.createQuery(rendered.query).apply {
    rendered.params.forEach { (name, value) ->
        setParameter(name, value)
    }
}
val result = jpaQuery.resultList

// Method 3: With parameter override
val queryParams = mapOf("price" to BigDecimal.valueOf(100))
entityManager.createQuery(query, queryParams, context)
```

---

## SELECT Clause

### Basic Selection
```kotlin
// Single field (type can be inferred)
select(path(Book::isbn))

// Multiple fields (requires type specification)
select<CustomDto>(
    path(Author::authorId),
    path(Author::name)
)

// Entire entity
select(entity(Book::class))

// With alias
select(entity(Book::class, "b"))
```

### DTO Projection with new()
```kotlin
data class Row(
    val departmentId: Long,
    val count: Long
)

select(
    new(
        Row::class,
        path(EmployeeDepartment::departmentId),
        count(Employee::employeeId)
    )
)
```

### Expression Aliasing
```kotlin
val bookPrice = expression(BigDecimal::class, "price")

select(
    path(Book::price)(BookPrice::value).`as`(bookPrice)
).from(
    entity(Book::class)
).where(
    bookPrice.eq(BigDecimal.valueOf(100))
)

// Alternative: using string alias
select(
    path(Book::price)(BookPrice::value).`as`(expression("price"))
).from(
    entity(Book::class)
).where(
    expression(BigDecimal::class, "price").eq(BigDecimal.valueOf(100))
)
```

---

## FROM Clause & Joins

### Basic FROM
```kotlin
from(
    entity(Author::class)
)
```

### Joins
```kotlin
// Association Join (using entity relationships)
from(
    entity(Book::class),
    join(Book::authors)
)

// Explicit Join with ON condition
from(
    entity(Author::class),
    join(BookAuthor::class).on(
        path(Author::authorId).equal(path(BookAuthor::authorId))
    )
)

// Join types
join(Book::authors)           // INNER JOIN
leftJoin(Book::authors)       // LEFT JOIN
rightJoin(Book::authors)      // RIGHT JOIN (if supported)
```

### Aliasing Joined Entities
```kotlin
from(
    entity(Book::class),
    join(Book::authors).`as`(entity(BookAuthor::class, "author"))
)
```

---

## WHERE Clause & Predicates

### Comparison Operators
```kotlin
// Equal / Not Equal
path(Book::price).equal(BigDecimal.valueOf(100))
path(Book::price).eq(BigDecimal.valueOf(100))
path(Book::price).notEqual(BigDecimal.valueOf(100))
path(Book::price).ne(BigDecimal.valueOf(100))

// Greater Than / Less Than
path(Book::price).greaterThan(BigDecimal.valueOf(100))
path(Book::price).gt(BigDecimal.valueOf(100))
path(Book::price).greaterThanOrEqualTo(BigDecimal.valueOf(100))
path(Book::price).ge(BigDecimal.valueOf(100))
path(Book::price).lessThan(BigDecimal.valueOf(100))
path(Book::price).lt(BigDecimal.valueOf(100))
path(Book::price).lessThanOrEqualTo(BigDecimal.valueOf(100))
path(Book::price).le(BigDecimal.valueOf(100))
```

### Range Operators
```kotlin
// Between
path(Employee::price).between(
    BigDecimal.valueOf(100),
    BigDecimal.valueOf(200)
)

path(Book::publishDate).between(
    OffsetDateTime.parse("2023-01-01T00:00:00+09:00"),
    OffsetDateTime.parse("2023-06-30T23:59:59+09:00")
)

// Not Between
path(Employee::price).notBetween(
    BigDecimal.valueOf(100),
    BigDecimal.valueOf(200)
)
```

### IN Operator
```kotlin
// In
path(Employee::price).`in`(
    BigDecimal.valueOf(100),
    BigDecimal.valueOf(200)
)

// Not In
path(Employee::price).notIn(
    BigDecimal.valueOf(100),
    BigDecimal.valueOf(200)
)
```

### Pattern Matching (LIKE)
```kotlin
// Like
path(Employee::nickname).like("E%")
path(Employee::nickname).like("E_", escape = '_')

// Not Like
path(Employee::nickname).notLike("E%")
path(Employee::nickname).notLike("E_", escape = '_')
```

### Null Checks
```kotlin
// Is Null
path(Employee::nickname).isNull()

// Is Not Null
path(Employee::nickname).isNotNull()
```

### Collection Checks
```kotlin
// Is Empty
path(Employee::departments).isEmpty()

// Is Not Empty
path(Employee::departments).isNotEmpty()
```

### Logical Operators (AND, OR, NOT)

#### Extension Function Style (No Explicit Parentheses)
```kotlin
// AND
path(Employee::name).eq("Employee01").and(path(Employee::nickname).eq("E01"))

// OR
path(Employee::name).eq("Employee01").or(path(Employee::nickname).eq("E01"))

// NOT
not(path(Employee::name).eq("Employee01"))

// Warning: Order of operations without parentheses
// This generates: Employee.name = 'Employee01' AND Employee.nickname = 'E01' OR Employee.name = 'Employee02' AND Employee.nickname = 'E02'
path(Employee::name).eq("Employee01")
    .and(path(Employee::nickname).eq("E01"))
    .or(path(Employee::name).eq("Employee02").and(path(Employee::nickname).eq("E02")))
```

#### Normal Function Style (WITH Parentheses) - RECOMMENDED
```kotlin
// AND
and(
    path(Employee::name).eq("Employee01"),
    path(Employee::nickname).eq("E01")
)

// OR
or(
    path(Employee::name).eq("Employee01"),
    path(Employee::nickname).eq("E01")
)

// Complex conditions with explicit parentheses
// This generates: (Employee.name = 'Employee01' AND Employee.nickname = 'E01') OR (Employee.name = 'Employee02' AND Employee.nickname = 'E02')
or(
    path(Employee::name).eq("Employee01").and(path(Employee::nickname).eq("E01")),
    path(Employee::name).eq("Employee02").and(path(Employee::nickname).eq("E02"))
)
```

#### Practical Example: Multiple OR Conditions
```kotlin
// Example: Find locations by multiple categories OR within radius
where(
    or(
        path(Location::categoryId).`in`(categoryIds),
        and(
            path(Location::latitude).between(minLat, maxLat),
            path(Location::longitude).between(minLon, maxLon)
        )
    )
)
```

### Subquery Predicates
```kotlin
// EXISTS
exists(subquery)

// NOT EXISTS
notExists(subquery)

// ALL / ANY
val annualSalaries = select(
    path(FullTimeEmployee::annualSalary)(EmployeeSalary::value)
).from(
    entity(FullTimeEmployee::class),
    join(FullTimeEmployee::departments)
).where(
    path(EmployeeDepartment::departmentId).eq(3L)
).asSubquery()

// Greater Than All
path(FullTimeEmployee::annualSalary)(EmployeeSalary::value).gtAll(annualSalaries)

// Less Than Any
path(FullTimeEmployee::annualSalary)(EmployeeSalary::value).ltAny(annualSalaries)
```

### Custom Predicates
```kotlin
customPredicate("{0} MEMBER OF {1}", value(author), path(Book::authors))
```

### Database Functions
```kotlin
// Predicate returning boolean
function(Boolean::class, "myFunction", path(Book::isbn))

// Custom expression
customExpression(String::class, "CAST({0} AS VARCHAR)", path(Book::price))
```

---

## GROUP BY & HAVING

### GROUP BY
```kotlin
groupBy(
    path(EmployeeDepartment::departmentId)
)

// Multiple grouping
groupBy(
    path(Employee::departmentId),
    path(Employee::jobTitle)
)
```

### HAVING
```kotlin
having(
    count(Employee::employeeId).greaterThan(1L)
)

// Complex HAVING conditions
having(
    and(
        count(Employee::employeeId).greaterThan(1L),
        sum(Employee::salary).gt(BigDecimal.valueOf(100000))
    )
)
```

### Complete GROUP BY Example
```kotlin
select(
    path(Employee::employeeId)
).from(
    entity(Employee::class),
    join(Employee::departments)
).groupBy(
    path(Employee::employeeId)
).having(
    count(Employee::employeeId).greaterThan(1L)
).orderBy(
    count(Employee::employeeId).desc(),
    path(Employee::employeeId).asc()
)
```

---

## ORDER BY

### Basic Ordering
```kotlin
orderBy(
    path(Book::isbn).asc()
)

// Descending
orderBy(
    path(Book::price).desc()
)

// Multiple ordering
orderBy(
    path(Book::publishDate).desc(),
    path(Book::isbn).asc()
)
```

### Ordering by Aggregations
```kotlin
orderBy(
    count(Author::authorId).desc(),
    path(Author::authorId).asc()
)
```

---

## Pagination (LIMIT/OFFSET)

### Using JPA Query API (After creating query)
```kotlin
val context = JpqlRenderContext()

val query = jpql {
    select(path(Author::authorId))
        .from(entity(Author::class))
        .orderBy(count(Author::authorId).desc())
}

// Set pagination on JPA Query
entityManager.createQuery(query, context).apply {
    maxResults = 10        // LIMIT
    firstResult = 20       // OFFSET
}
```

### Spring Data Pagination (Recommended for Spring projects)
```kotlin
// Repository interface
interface BookRepository : JpaRepository<Book, Isbn>, KotlinJdslJpqlExecutor

// Usage
val pageable = PageRequest.of(0, 10, Sort.by("isbn").ascending())

val result: Page<Isbn?> = bookRepository.findPage(pageable) {
    select(path(Book::isbn))
        .from(entity(Book::class))
}

// Slice (no total count)
val result: Slice<Isbn?> = bookRepository.findSlice(pageable) {
    select(path(Book::isbn))
        .from(entity(Book::class))
}
```

---

## Aggregation Functions

### COUNT
```kotlin
count(path(Book::price))
countDistinct(path(Book::price))
```

### MIN / MAX
```kotlin
max(path(Book::price))
maxDistinct(path(Book::price))

min(path(Book::price))
minDistinct(path(Book::price))
```

### SUM
```kotlin
sum(path(Book::price))
sumDistinct(path(Book::price))
```

#### SUM Return Types
| Input Type | Return Type |
|------------|-------------|
| Int        | Long        |
| Long       | Long        |
| Float      | Double      |
| Double     | Double      |
| BigInteger | BigInteger  |
| BigDecimal | BigDecimal  |

### AVG
```kotlin
avg(path(Book::price))
avgDistinct(path(Book::price))
```

### Aggregation with GROUP BY
```kotlin
data class Row(
    val departmentId: Long,
    val count: Long
)

select(
    new(
        Row::class,
        path(EmployeeDepartment::departmentId),
        count(Employee::employeeId)
    )
).from(
    entity(Employee::class),
    join(Employee::departments)
).groupBy(
    path(EmployeeDepartment::departmentId)
)
```

---

## Subqueries

### Expression Subquery (asSubquery)
Used in WHERE clauses (IN, ALL, ANY) or as values.

```kotlin
val query = jpql {
    val employeeIds = select<Long>(
        path(EmployeeDepartment::employee)(Employee::employeeId)
    ).from(
        entity(Department::class),
        join(EmployeeDepartment::class)
            .on(path(Department::departmentId).equal(path(EmployeeDepartment::departmentId)))
    ).where(
        path(Department::name).like("%03")
    ).asSubquery()

    deleteFrom(Employee::class)
        .where(path(Employee::employeeId).`in`(employeeIds))
}
```

### Derived Entity Subquery (asEntity)
Used in FROM clause as a table/entity.

```kotlin
data class DerivedEntity(
    val employeeId: Long,
    val count: Long
)

val query = jpql {
    val subquery = select<DerivedEntity>(
        path(Employee::employeeId).`as`(expression("employeeId")),
        count(Employee::employeeId).`as`(expression("count"))
    ).from(
        entity(Employee::class),
        join(Employee::departments)
    ).groupBy(
        path(Employee::employeeId)
    ).having(
        count(Employee::employeeId).greaterThan(1L)
    )

    select(
        count(DerivedEntity::employeeId)
    ).from(
        subquery.asEntity()
    )
}
```

---

## Spring Data Integration

### Repository Setup
```kotlin
interface BookRepository : JpaRepository<Book, Isbn>, KotlinJdslJpqlExecutor
```

### Query Methods

#### findAll - List Results
```kotlin
val result: List<Isbn?> = bookRepository.findAll {
    select(path(Book::isbn))
        .from(entity(Book::class))
}
```

#### findPage - Paginated Results with Total Count
```kotlin
val pageable = PageRequest.of(0, 10)
val result: Page<Isbn?> = bookRepository.findPage(pageable) {
    select(path(Book::isbn))
        .from(entity(Book::class))
}
```

#### findSlice - Paginated Results without Total Count
```kotlin
val pageable = PageRequest.of(0, 10)
val result: Slice<Isbn?> = bookRepository.findSlice(pageable) {
    select(path(Book::isbn))
        .from(entity(Book::class))
}
```

#### findStream - Streaming Results
```kotlin
val result: Stream<Isbn?> = bookRepository.findStream {
    select(path(Book::isbn))
        .from(entity(Book::class))
}
```

### Spring Batch Integration
```kotlin
@Autowired
lateinit var queryProviderFactory: KotlinJdslQueryProviderFactory

val queryProvider = queryProviderFactory.create {
    select(path(Book::isbn))
        .from(entity(Book::class))
}

JpaCursorItemReaderBuilder<Isbn>()
    .entityManagerFactory(entityManagerFactory)
    .queryProvider(queryProvider)
    .saveState(false)
    .build()
```

---

## Complete Examples

### Example 1: Basic Query with Filtering
```kotlin
select(path(Book::isbn))
    .from(entity(Book::class))
    .where(
        path(Book::publishDate).between(
            OffsetDateTime.parse("2023-01-01T00:00:00+09:00"),
            OffsetDateTime.parse("2023-06-30T23:59:59+09:00")
        )
    )
    .orderBy(path(Book::isbn).asc())
```

### Example 2: Join with Aggregation
```kotlin
val context = JpqlRenderContext()

val query = jpql {
    select(path(Author::authorId))
        .from(
            entity(Author::class),
            join(BookAuthor::class).on(
                path(Author::authorId).equal(path(BookAuthor::authorId))
            )
        )
        .groupBy(path(Author::authorId))
        .orderBy(count(Author::authorId).desc())
}

val mostProlificAuthor = entityManager.createQuery(query, context).apply {
    maxResults = 1
}
```

### Example 3: Complex WHERE with OR/AND
```kotlin
select(path(Location::id))
    .from(entity(Location::class))
    .where(
        and(
            path(Location::isActive).eq(true),
            or(
                path(Location::categoryId).`in`(categoryIds),
                path(Location::name).like("%${keyword}%")
            )
        )
    )
    .orderBy(path(Location::createdAt).desc())
```

### Example 4: Spatial Query (PostGIS)
```kotlin
// Custom function for spatial distance
select(path(Location::id))
    .from(entity(Location::class))
    .where(
        and(
            path(Location::isActive).eq(true),
            function(
                Boolean::class,
                "ST_DWithin",
                path(Location::coordinates).cast(Geography::class),
                function(
                    Geography::class,
                    "ST_SetSRID",
                    function(Point::class, "ST_MakePoint", value(longitude), value(latitude)),
                    value(4326)
                ),
                value(radiusMeters)
            )
        )
    )
```

### Example 5: DTO Projection with Aggregation
```kotlin
data class CategoryStats(
    val categoryId: UUID,
    val categoryName: String,
    val locationCount: Long,
    val avgRating: Double?
)

select(
    new(
        CategoryStats::class,
        path(Category::id),
        path(Category::name),
        count(Location::id),
        avg(Location::rating)
    )
).from(
    entity(Category::class),
    leftJoin(Location::class).on(
        path(Location::categoryId).eq(path(Category::id))
    )
).groupBy(
    path(Category::id),
    path(Category::name)
).having(
    count(Location::id).greaterThan(0L)
).orderBy(
    count(Location::id).desc()
)
```

### Example 6: Parameter Handling
```kotlin
// Immutable parameters with value()
select(path(Book::isbn))
    .from(entity(Book::class))
    .where(
        path(Book::price).eq(value(BigDecimal.valueOf(100)))
    )

// Mutable parameters with param()
val context = JpqlRenderContext()

val query = jpql {
    select(path(Book::isbn))
        .from(entity(Book::class))
        .where(path(Book::price).eq(param("price")))
}

val queryParams = mapOf("price" to BigDecimal.valueOf(100))
entityManager.createQuery(query, queryParams, context)
```

### Example 7: Conditional Expression (CASE WHEN)
```kotlin
select(
    path(Book::isbn),
    caseWhen(path(Book::price).lt(BigDecimal.valueOf(100))).then("Cheap")
        .`when`(path(Book::price).lt(BigDecimal.valueOf(200))).then("Medium")
        .`when`(path(Book::price).lt(BigDecimal.valueOf(300))).then("Expensive")
        .`else`("Very Expensive")
).from(entity(Book::class))
```

### Example 8: Update Statement
```kotlin
val query = jpql {
    update(entity(Book::class))
        .set(
            path(Book::price)(BookPrice::value),
            BigDecimal.valueOf(100)
        )
        .set(
            path(Book::salePrice)(BookPrice::value),
            BigDecimal.valueOf(80)
        )
        .where(path(Book::isbn).eq(Isbn("01")))
}
```

### Example 9: Delete Statement with Subquery
```kotlin
val query = jpql {
    val employeeIds = select<Long>(
        path(EmployeeDepartment::employee)(Employee::employeeId)
    ).from(
        entity(Department::class),
        join(EmployeeDepartment::class).on(
            path(Department::departmentId).equal(path(EmployeeDepartment::departmentId))
        )
    ).where(
        path(Department::name).like("%03")
    ).asSubquery()

    deleteFrom(Employee::class)
        .where(path(Employee::employeeId).`in`(employeeIds))
}
```

---

## Debugging Queries

### Enable Debug Logging
Add to your logging configuration:
```yaml
logging:
  level:
    com.linecorp.kotlinjdsl: DEBUG
```

### Sample Debug Output
```log
2023-01-01T00:00:00.000+09:00 DEBUG c.l.kotlinjdsl.render.jpql.JpqlRenderer  : The query is rendered.
SELECT Book.isbn FROM Book AS Book WHERE Book.publishDate BETWEEN :param1 AND :param2 ORDER BY Book.isbn ASC
{param1=2023-01-01T00:00+09:00, param2=2023-06-30T23:59:59+09:00}
```

---

## Key Differences from Other Query DSLs

### vs QueryDSL / jOOQ
- **No Code Generation**: Kotlin JDSL uses KClass and KProperty directly
- **Type Safety**: Still type-safe without Q-classes
- **No Build Step**: No need to regenerate metadata after schema changes

### Workflow Comparison
```text
QueryDSL/jOOQ:
1. Modify tables and entities
2. Compile errors appear
3. Run Maven/Gradle task to regenerate metadata
4. Build queries

Kotlin JDSL:
1. Modify tables and entities
2. Build queries directly (no regeneration needed)
```

---

## Common Patterns & Best Practices

### 1. Use Normal Functions for Complex Logic
```kotlin
// Good: Explicit parentheses with normal functions
or(
    and(condition1, condition2),
    and(condition3, condition4)
)

// Avoid: Extension functions for complex logic (ambiguous order)
condition1.and(condition2).or(condition3.and(condition4))
```

### 2. Prefer Spring Data Integration
```kotlin
// Recommended
interface LocationRepository : JpaRepository<Location, UUID>, KotlinJdslJpqlExecutor

bookRepository.findPage(pageable) { /* query */ }

// Instead of manual EntityManager usage
entityManager.createQuery(query, context)
```

### 3. Type Specification for Multi-Field Projections
```kotlin
// Required for multiple fields
select<LocationDto>(
    path(Location::id),
    path(Location::name)
)

// Optional for single field
select(path(Location::id))
```

### 4. Use DTO Projections for Complex Queries
```kotlin
data class LocationWithCategory(
    val locationId: UUID,
    val locationName: String,
    val categoryName: String
)

select(
    new(
        LocationWithCategory::class,
        path(Location::id),
        path(Location::name),
        path(Category::name)
    )
)
```

---

## References

- Official Repository: https://github.com/line/kotlin-jdsl
- Documentation: https://github.com/line/kotlin-jdsl/tree/main/docs
