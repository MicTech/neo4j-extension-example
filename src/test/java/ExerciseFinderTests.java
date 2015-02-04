import org.codehaus.jackson.map.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.*;
import org.neo4j.test.TestGraphDatabaseFactory;

import javax.ws.rs.core.Response;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

public class ExerciseFinderTests {

    private GraphDatabaseService database;
    private ExerciseFinder extension;
    private ObjectMapper objectMapper;

    private static final RelationshipType HAS = DynamicRelationshipType.withName("HAS");
    private static final RelationshipType IS = DynamicRelationshipType.withName("IS");
    private static final RelationshipType IS_FOR = DynamicRelationshipType.withName("IS_FOR");

    @Before
    public void setUp() {
        database = new TestGraphDatabaseFactory().newImpermanentDatabase();
        extension = new ExerciseFinder(database);
        objectMapper = new ObjectMapper();

        prepareDb(database);
    }

    @After
    public void tearDown() {
        database.shutdown();
    }

    private void prepareDb(GraphDatabaseService database) {
        try (Transaction tx = database.beginTx()) {

            Node push = createNode("Group", "Push");

            Node basketballPushUps = createNode("Exercise", "Basketball Push-ups");

            basketballPushUps.createRelationshipTo(push, IS_FOR);

            Node pecs = createNode("Muscle", "pecs");
            Node triceps = createNode("Muscle", "triceps");
            Node delts = createNode("Muscle", "delts");
            Node abs = createNode("Muscle", "abs");

            Node semiEasy = createNode("Difficulty", "Semi-easy");
            Node moderate = createNode("Difficulty", "Moderate");
            Node semiHard = createNode("Difficulty", "Semi-hard");

            Node kneesOnGround = createNode("Variation", "Knees on ground");

            basketballPushUps.createRelationshipTo(kneesOnGround, HAS);
            kneesOnGround.createRelationshipTo(semiEasy, IS);
            kneesOnGround.createRelationshipTo(pecs, IS_FOR);
            kneesOnGround.createRelationshipTo(triceps, IS_FOR);
            kneesOnGround.createRelationshipTo(delts, IS_FOR);
            kneesOnGround.createRelationshipTo(abs, IS_FOR);


            Node kneesOffGround = createNode("Variation", "Knees off ground");

            basketballPushUps.createRelationshipTo(kneesOffGround, HAS);
            kneesOffGround.createRelationshipTo(moderate, IS);
            kneesOffGround.createRelationshipTo(pecs, IS_FOR);
            kneesOffGround.createRelationshipTo(triceps, IS_FOR);
            kneesOffGround.createRelationshipTo(delts, IS_FOR);
            kneesOffGround.createRelationshipTo(abs, IS_FOR);


            Node bothHandsOnOneBall = createNode("Variation", "Both hands on one ball");

            basketballPushUps.createRelationshipTo(bothHandsOnOneBall, HAS);
            bothHandsOnOneBall.createRelationshipTo(moderate, IS);
            bothHandsOnOneBall.createRelationshipTo(pecs, IS_FOR);
            bothHandsOnOneBall.createRelationshipTo(triceps, IS_FOR);
            bothHandsOnOneBall.createRelationshipTo(delts, IS_FOR);
            bothHandsOnOneBall.createRelationshipTo(abs, IS_FOR);


            Node feetElevated = createNode("Variation", "Feet elevated");

            basketballPushUps.createRelationshipTo(feetElevated, HAS);
            feetElevated.createRelationshipTo(semiHard, IS);
            feetElevated.createRelationshipTo(pecs, IS_FOR);
            feetElevated.createRelationshipTo(triceps, IS_FOR);
            feetElevated.createRelationshipTo(delts, IS_FOR);
            feetElevated.createRelationshipTo(abs, IS_FOR);


            Node feetElevatedHandsOnOneBall = createNode("Variation", "Feet elevated, hands on one ball");

            basketballPushUps.createRelationshipTo(feetElevatedHandsOnOneBall, HAS);
            feetElevatedHandsOnOneBall.createRelationshipTo(semiHard, IS);
            feetElevatedHandsOnOneBall.createRelationshipTo(pecs, IS_FOR);
            feetElevatedHandsOnOneBall.createRelationshipTo(triceps, IS_FOR);
            feetElevatedHandsOnOneBall.createRelationshipTo(delts, IS_FOR);
            feetElevatedHandsOnOneBall.createRelationshipTo(abs, IS_FOR);

            tx.success();
        }
    }

    private Node createNode(String labelName, String name) {
        Node node = database.createNode(DynamicLabel.label(labelName));
        node.setProperty("name", name);
        return node;
    }

    @Test
    public void returnTwoVariationsForPelcMuscleAndModerateDifficultyTest() throws Exception {
        Response response = extension.getVariationByMuscleDifficulty("abs", "Moderate");
        List list = objectMapper.readValue((String) response.getEntity(), List.class);

        assertNotNull(response);
        assertEquals(2, list.size());
        assertEquals("[\"Exercise: Basketball Push-ups Variation: Knees off ground\",\"Exercise: Basketball Push-ups Variation: Both hands on one ball\"]", response.getEntity().toString());
    }
}