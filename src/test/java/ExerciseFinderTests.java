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

    private static final String GROUP = "Group";
    private static final String GROUP_PUSH = "Push";

    private static final String DIFFICULTY = "Difficulty";
    private static final String DIFFICULTY_SEMI_EASY = "Semi-easy";
    private static final String DIFFICULTY_MODERATE = "Moderate";
    private static final String DIFFICULTY_SEMI_HARD = "Semi-hard";

    private static final String EXERCISE = "Exercise";

    private static final String VARIATION = "Variation";

    private static final String MUSCLE = "Muscle";
    private static final String MUSCLE_PECS = "pecs";
    private static final String MUSCLE_TRICEPS = "triceps";
    private static final String MUSCLE_DELTS = "delts";
    private static final String MUSCLE_ABS = "abs";

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

            Node push = createNode(GROUP, GROUP_PUSH);

            Node basketballPushUps = createNode(EXERCISE, "Basketball Push-ups");

            basketballPushUps.createRelationshipTo(push, IS_FOR);

            Node pecs = createNode(MUSCLE, MUSCLE_PECS);
            Node triceps = createNode(MUSCLE, MUSCLE_TRICEPS);
            Node delts = createNode(MUSCLE, MUSCLE_DELTS);
            Node abs = createNode(MUSCLE, MUSCLE_ABS);

            Node semiEasy = createNode(DIFFICULTY, DIFFICULTY_SEMI_EASY);
            Node moderate = createNode(DIFFICULTY, DIFFICULTY_MODERATE);
            Node semiHard = createNode(DIFFICULTY, DIFFICULTY_SEMI_HARD);

            createVariation("Knees on ground", basketballPushUps, semiEasy, new Node[]{pecs, triceps, delts, abs});
            createVariation("Knees off ground", basketballPushUps, moderate, new Node[]{pecs, triceps, delts, abs});
            createVariation("Both hands on one ball", basketballPushUps, moderate, new Node[]{pecs, triceps, delts, abs});
            createVariation("Feet elevated", basketballPushUps, semiHard, new Node[]{pecs, triceps, delts, abs});
            createVariation("Feet elevated, hands on one ball", basketballPushUps, semiHard, new Node[]{pecs, triceps, delts, abs});

            tx.success();
        }
    }

    private void createVariation(String name, Node exercise, Node difficulty, Node[] muscles) {
        Node variation = createNode(VARIATION, name);

        exercise.createRelationshipTo(variation, HAS);
        variation.createRelationshipTo(difficulty, IS);

        addMusclesToVariation(variation, muscles);
    }

    private void addMusclesToVariation(Node variation, Node[] muscles) {
        for(Node muscle : muscles) {
            variation.createRelationshipTo(muscle, IS_FOR);
        }
    }

    private Node createNode(String labelName, String name) {
        Node node = database.createNode(DynamicLabel.label(labelName));
        node.setProperty("name", name);
        return node;
    }

    @Test
    public void shouldReturnTwoVariationsForPelcMuscleAndModerateDifficultyTest() throws Exception {
        String muscle = MUSCLE_ABS;
        String difficulty = DIFFICULTY_MODERATE;

        Response response = extension.getVariationByMuscleDifficulty(muscle, difficulty);
        List list = objectMapper.readValue((String) response.getEntity(), List.class);

        assertNotNull(response);
        assertEquals(2, list.size());
        assertEquals("[\"Exercise: Basketball Push-ups Variation: Knees off ground\",\"Exercise: Basketball Push-ups Variation: Both hands on one ball\"]", response.getEntity().toString());
    }
}