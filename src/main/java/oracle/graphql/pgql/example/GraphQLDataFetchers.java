package oracle.graphql.pgql.example;

import javax.annotation.PostConstruct;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import graphql.schema.DataFetcher;
import oracle.pg.rdbms.pgql.PgqlConnection;
import oracle.pg.rdbms.pgql.PgqlPreparedStatement;
import oracle.pg.rdbms.pgql.PgqlResultSet;
import oracle.pg.rdbms.pgql.PgqlToSqlException;
import org.metaborg.util.functions.CheckedFunction1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GraphQLDataFetchers {

  Logger log = LoggerFactory.getLogger(GraphQLDataFetchers.class);

  @Value("${jdbc.url}")
  private String jdbcUrl;

  @Value("${jdbc.user}")
  private String jdbcUser;

  @Value("${jdbc.pass}")
  private String jdbcPass;

  @PostConstruct
  public void init() throws Exception {
    log.info("jdbcUrl = {} user = {}", jdbcUrl, jdbcUser);
    this.<Void, Exception>withPGQLConnection(conn -> {
      try {
        conn.createStatement().execute(Util.getResource("drop.pgql"));
      } catch (PgqlToSqlException e) {
        log.info("drop failed: {}", e.getMessage());
      }
      log.info("creating property graph books in database");
      conn.createStatement().execute(Util.getResource("create.pgql"));
      log.info("insert data into books graph");
      conn.createStatement().execute(Util.getResource("insert.pgql"));
      return null;
    });
  }

  private <R, E extends Exception> R withPGQLConnection(CheckedFunction1<PgqlConnection, R, E> func)
      throws E, SQLException {
    try (Connection conn = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass)) {
      conn.setAutoCommit(false);
      PgqlConnection pgql = PgqlConnection.getConnection(conn);
      return func.apply(pgql);
    }
  }

  public DataFetcher getBookByIdDataFetcher() {
    return dataFetchingEnvironment -> {
      Long bookId = Long.valueOf(dataFetchingEnvironment.getArgument("id"));
      return withPGQLConnection(conn -> {
        String query = "SELECT b.id, b.name, b.pageCount FROM MATCH (b:Book) ON books WHERE b.id = ?";
        PgqlPreparedStatement statement = conn.prepareStatement(query);
        statement.setLong(1, bookId);
        log.info("executing {} with bookId = {}", query, bookId);
        PgqlResultSet rs = statement.executeQuery();
        if (!rs.next()) {
          log.info("no book with ID {} found", bookId);
          return null;
        }
        Map<String, String> book = ImmutableMap //
            .of("id", String.valueOf(rs.getLong(1)), //
                "name", rs.getString(2), //
                "pageCount", String.valueOf(rs.getLong(3)));
        log.info("found book = {}", book);
        return book;
      });
    };
  }

  public DataFetcher getAuthorDataFetcher() {
    return dataFetchingEnvironment -> {
      Map<String, String> book = dataFetchingEnvironment.getSource();
      long bookId = Long.valueOf(book.get("id"));
      return withPGQLConnection(conn -> {
        String query = "SELECT a.id, a.firstName, a.lastName FROM MATCH (b:Book) -[:written_by]-> (a:Author) ON books"
            + " WHERE b.id = ?";
        PgqlPreparedStatement statement = conn.prepareStatement(query);
        statement.setLong(1, bookId);
        log.info("executing {} with bookId = {}", query, bookId);
        PgqlResultSet rs = statement.executeQuery();
        if (!rs.next()) {
          log.info("no author with bookID = {} found", bookId);
          return null;
        }
        Map<String, String> author = ImmutableMap //
            .of("id", String.valueOf(rs.getLong(1)), //
                "firstName", rs.getString(2), //
                "lastName", rs.getString(3));
        log.info("found author = {}", author);
        return author;
      });
    };
  }
}
