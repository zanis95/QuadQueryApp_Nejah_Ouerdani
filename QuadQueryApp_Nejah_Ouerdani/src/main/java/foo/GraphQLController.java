package foo;

import com.github.andrewoma.dexx.collection.HashMap;
import com.github.andrewoma.dexx.collection.Map;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.datastore.*;
import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;

import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Api(name = "graphql", version = "v1", namespace = @ApiNamespace(ownerDomain = "foo", ownerName = "foo"), scopes = "email")
public class GraphQLController {

    // Datastore service for handling database interactions
    private final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    @ApiMethod(name = "get_most", httpMethod = HttpMethod.GET)
public List<RDFTriple> get_most() {
    List<RDFTriple> result = new ArrayList<>();
    
    // Datastore query
    Query datastoreQuery = new Query("RDFTriple");
    PreparedQuery pq = datastore.prepare(datastoreQuery);
    
    // Fetching entities from Datastore
    List<Entity> entities = pq.asList(FetchOptions.Builder.withLimit(100));
    
    if (entities.isEmpty()) {
        System.out.println("No results found in Datastore.");
    }
    
    // Process each entity
    for (Entity entity : entities) {
        // Debugging: Log the entity to verify its contents
        System.out.println("Entity: " + entity.getKey());
        
        String subject = (String) entity.getProperty("subject");
        String predicate = (String) entity.getProperty("predicate");
        String object = (String) entity.getProperty("object");
        String graph = (String) entity.getProperty("graphName");
        
        // Null checks in case any properties are missing
        if (subject == null || predicate == null || object == null || graph == null) {
            System.out.println("Skipping entity with missing properties.");
            continue; // Skip this entity if any property is missing
        }
        
        // Create RDFTriple and add it to the result list
        RDFTriple rdfTriple = new RDFTriple(subject, predicate, object, graph);
        result.add(rdfTriple);
    }
    
    // Return the result
    return result;
}

    
    @ApiMethod(name = "healthCheck", httpMethod = HttpMethod.GET)
public HealthCheckResponse healthCheck() {
    HealthCheckResponse response = new HealthCheckResponse();
    response.setMessage("OK");
    return response;
}

public static class HealthCheckResponse {
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

    // Main API endpoint for GraphQL
    @ApiMethod(name = "graphqlEndpoint", httpMethod = HttpMethod.POST)
public GraphQLResponse graphqlEndpoint(@Named("query") String query) throws UnauthorizedException {
    // Process the GraphQL query and execute it
    String result = executeGraphQLQuery(query);
    System.out.println("Received query: " + query);

    GraphQLResponse response = new GraphQLResponse();
    response.setResult(result);
    return response;
}

public static class GraphQLResponse {
    private String result;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}


    // Handle the GraphQL query execution and return the response as JSON
    private String executeGraphQLQuery(String query) {
        if (query.contains("RDFTriple")) {
            List<RDFTriple> rdfTriples = fetchRDFTriples(query);
            // Prepare a response map with results and total count
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("results", rdfTriples);
            responseMap.put("totalMatches", rdfTriples.size());
            return new Gson().toJson(responseMap); // Convert to JSON using Gson
        }
        return "{\"error\": \"Invalid query or no results found\"}";
    }

    // Fetch RDFTriple entities from Datastore based on the query parameters
    private List<RDFTriple> fetchRDFTriples(String query) {
        Query datastoreQuery = new Query("RDFTriple");
        
        // Extract query parameters (subject, predicate, object, graph)
        String subjectValue = extractQueryParameter(query, "subject");
        if (subjectValue != null) {
            datastoreQuery.setFilter(new Query.FilterPredicate("subject", FilterOperator.EQUAL, subjectValue));
        }

        String predicateValue = extractQueryParameter(query, "predicate");
        if (predicateValue != null) {
            datastoreQuery.setFilter(new Query.FilterPredicate("predicate", FilterOperator.EQUAL, predicateValue));
        }

        String objectValue = extractQueryParameter(query, "object");
        if (objectValue != null) {
            datastoreQuery.setFilter(new Query.FilterPredicate("object", FilterOperator.EQUAL, objectValue));
        }

        String graphValue = extractQueryParameter(query, "graphName");
        if (graphValue != null) {
            datastoreQuery.setFilter(new Query.FilterPredicate("graphName", FilterOperator.EQUAL, graphValue));
        }

        // Execute the Datastore query and return the results
        System.out.println("Datastore query: " + datastoreQuery);
        return executeQuery(datastoreQuery);
    }

    // Extract a parameter value from the GraphQL query string using regex
    private String extractQueryParameter(String query, String param) {
        String regex = param + "=\"([^\"]+)\""; // Regular expression to extract values
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(query);
        return matcher.find() ? matcher.group(1) : null;
    }

    // Execute the query on Datastore and fetch RDFTriples
    private List<RDFTriple> executeQuery(Query q) {
        List<RDFTriple> rdfTriples = new ArrayList<>();
        PreparedQuery pq = datastore.prepare(q);
        for (Entity result : pq.asIterable()) {
            String subject = (String) result.getProperty("subject");
            String predicate = (String) result.getProperty("predicate");
            String object = (String) result.getProperty("object");
            String graph = (String) result.getProperty("graph");
            rdfTriples.add(new RDFTriple(subject, predicate, object, graph));
        }
        return rdfTriples;
    }

    // RDFTriple class to represent the data structure
    public static class RDFTriple {
        private String subject;
        private String predicate;
        private String object;
        private String graph;

        public RDFTriple(String subject, String predicate, String object, String graph) {
            this.subject = subject;
            this.predicate = predicate;
            this.object = object;
            this.graph = graph;
        }

        public String getSubject() {
            return subject;
        }

        public String getPredicate() {
            return predicate;
        }

        public String getObject() {
            return object;
        }

        public String getGraph() {
            return graph;
        }
    }
}
