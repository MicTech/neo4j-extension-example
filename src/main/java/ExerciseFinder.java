import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.helpers.collection.MapUtil;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Path("yayog")
public class ExerciseFinder {

    private final GraphDatabaseService database;
    private final ExecutionEngine engine;

    public ExerciseFinder(@Context GraphDatabaseService database) {
        this.database = database;
        this.engine = new ExecutionEngine(database);
    }

    @Path("/muscle/{name}/difficulty/{level}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getVariationByMuscleDifficulty(@PathParam("name") String muscleName,
                                                   @PathParam("level") String difficultyLevel) throws IOException {
        final Map<String, Object> params = MapUtil.map("muscleName", muscleName, "difficultyLevel", difficultyLevel);

        String query = "MATCH (v:Variation)-[:IS]->(d:Difficulty {name: {difficultyLevel}}),(v:Variation)-[:IS_FOR]->(m:Muscle {name: {muscleName}}), (v:Variation)<-[:HAS]-(e:Exercise) RETURN v,e";

        List results = new ArrayList();

        try (Transaction tx = database.beginTx()) {
            ExecutionResult result = engine.execute(query, params);

            for(Map<String,Object> row : result){
                Node variation = (Node) row.get("v");
                Node exercise = (Node) row.get("e");

                results.add(String.format("Exercise: %1$s Variation: %2$s", exercise.getProperty("name"), variation.getProperty("name")));
            }

            tx.success();
        }

        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writeValueAsString(results);
        return Response.status(Response.Status.OK).entity(jsonString).build();
    }
}