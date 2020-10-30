package oracle.graphql.pgql.example;

import javax.annotation.PostConstruct;

import java.io.IOException;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import static oracle.graphql.pgql.example.Util.getResource;
import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;

@Component
public class GraphQLProvider {

  @Autowired
  GraphQLDataFetchers graphQLDataFetchers;

  private GraphQL graphQL;

  @PostConstruct
  public void init() throws IOException {
    GraphQLSchema graphQLSchema = buildSchema(getResource("schema.graphqls"));
    this.graphQL = GraphQL.newGraphQL(graphQLSchema).build();
  }

  private GraphQLSchema buildSchema(String sdl) {
    TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(sdl);
    RuntimeWiring runtimeWiring = buildWiring();
    SchemaGenerator schemaGenerator = new SchemaGenerator();
    return schemaGenerator.makeExecutableSchema(typeRegistry, runtimeWiring);
  }

  private RuntimeWiring buildWiring() {
    return RuntimeWiring.newRuntimeWiring() //
        .type(newTypeWiring("Query") //
            .dataFetcher("bookById", graphQLDataFetchers.getBookByIdDataFetcher())) //
        .type(newTypeWiring("Book") //
            .dataFetcher("author", graphQLDataFetchers.getAuthorDataFetcher())) //
        .build();
  }

  @Bean
  public GraphQL graphQL() {
    return graphQL;
  }

}
