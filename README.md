# GraphQL Example using PGQL on Oracle Database

This is a fork of the 
[Getting started with GraphQL Java and Spring Boot](https://www.graphql-java.com/tutorials/getting-started-with-spring-boot/)
example but using Oracle's graph query language [PGQL](http://pgql-lang.org) to fetch the data requested by the GraphQL
query from an Oracle Database.

## How to use

Download Oracle Graph Client 20.4.0 from [here](https://www.oracle.com/database/technologies/spatialandgraph/property-graph-features/graph-server-and-client/graph-server-and-client-downloads.html)
and unzip into the current directory. Then tun the `bootRun` Gradle task, passing in JDBC connection info to an 
Oracle Database as command line arguments like this:

```
./gradlew bootRun -Pargs='--jdbc.url=jdbc:oracle:thin:@<host>:<port>/<service>,--jdbc.user=<user>,--jdbc.pass=<pass>'
``` 

After the command completes, the GraphQL endpoint will listen on [http://localhost:8080/graphql](http://localhost:8080/graphql)
and you can try it out using any GraphQL client of your choice. Here an example using [Altair GraphQL client](https://altair.sirmuel.design/):

![Alt text](/screenshot.png?raw=true)

## How it works

When the server starts up, it registers the following GraphQL schema at the GraphQL Java library:

```
type Query {
    bookById(id: ID): Book
}

type Book {
    id: ID
    name: String
    pageCount: Int
    author: Author
}

type Author {
    id: ID
    firstName: String
    lastName: String
}
```

The following graph is being created in the Oracle Database during startup:

```
CREATE PROPERTY GRAPH books;

INSERT INTO books
    VERTEX book1 LABELS (Book) PROPERTIES (book1.id = 1, book1.name = 'Harry Potter and the Philosopher''s Stone', book1.pageCount = 223),
    VERTEX book2 LABELS (Book) PROPERTIES (book2.id = 2, book2.name = 'Moby Dick', book2.pageCount = 635),
    VERTEX book3 LABELS (Book) PROPERTIES (book3.id = 3, book3.name = 'Interview with the vampire', book3.pageCount = 371),
    VERTEX author1 LABELS (Author) PROPERTIES (author1.id = 1, author1.firstName = 'Joanne', author1.lastName = 'Rowling'),
    VERTEX author2 LABELS (Author) PROPERTIES (author2.id = 2, author2.firstName = 'Herman', author2.lastName = 'Melville'),
    VERTEX author3 LABELS (Author) PROPERTIES (author3.id = 3, author3.firstName = 'Anne', author3.lastName = 'Rice'),
    EDGE book1author BETWEEN book1 AND author1 LABELS (written_by),
    EDGE book2author BETWEEN book2 AND author2 LABELS (written_by),
    EDGE book3author BETWEEN book3 AND author3 LABELS (written_by);
```

If the graph already exists, it will be deleted first. 

Once a GraphQL is received, the data fetchers use a PGQL query to retrieve the requested data from the Oracle Database.
Look at `GraphQLDataFetchers.java` for how the queries look like.