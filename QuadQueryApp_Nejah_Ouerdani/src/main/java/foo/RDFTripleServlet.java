package foo;

import java.io.InputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;

@WebServlet(name = "RDFTripleServlet", urlPatterns = { "/processTurtle" })
public class RDFTripleServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");

        // Load Turtle file
        String turtleFilePath = "/2024_medalists_nuts_only.ttl";
        Model model = ModelFactory.createDefaultModel();
        try (InputStream fis = getClass().getResourceAsStream(turtleFilePath)) {
            if (fis == null) {
                throw new FileNotFoundException("Resource not found: " + turtleFilePath);
            }
            model.read(fis, null, "TTL");
        }


        // Initialize Datastore
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        // Iterate through the statements (triples)
        StmtIterator iterator = model.listStatements();
        while (iterator.hasNext()) {
            Statement stmt = iterator.next();
            
            // Extract components
            String subject = stmt.getSubject().toString();
            String predicate = stmt.getPredicate().toString();
            String object = stmt.getObject().toString();

            // Create Datastore entity
            Entity entity = new Entity("RDFTriple");
            entity.setProperty("subject", subject);
            entity.setProperty("predicate", predicate);
            entity.setProperty("object", object);
            entity.setProperty("graphName", "defaultGraph"); // Update if you use named graphs

            datastore.put(entity);

            // Print confirmation
            response.getWriter().println("<li>Stored triple: " 
                + subject + " " + predicate + " " + object + "</li>");
        }

        response.getWriter().println("<p>All triples stored successfully!</p>");
    }
}
