package sanju.newproject;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import sanju.newproject.ml.MLEngine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class MLEngineTest {

    private MLEngine engine;

    @Before
    public void setUp() {
        engine = new MLEngine();
    }

    @Test
    public void testWhyMLIsImportantOfflineAnswer() {
        String answer = engine.getOfflineDetailedAnswer("Why ML is Important to our daily life");
        assertTrue("Expected daily life importance points", answer.contains("Why Machine Learning is Important in Daily Life"));
        assertTrue("Expected Personal Assistants", answer.contains("Personal Assistants"));
        assertTrue("Expected Smart Recommendations", answer.contains("Recommendations"));
        assertTrue("Expected Healthcare", answer.contains("Healthcare"));
    }

    @Test
    public void testFullFormFastOffline() {
        final AtomicReference<String> result = new AtomicReference<>();
        final CountDownLatch latch = new CountDownLatch(1);

        engine.getAnswer("knn full form", ans -> {
            result.set(ans);
            latch.countDown();
        });

        assertEquals("KNN Full Form is K-Nearest Neighbors.", result.get());
    }

    @Test
    public void testOfflineKnnDetails() {
        String answer = engine.getOfflineDetailedAnswer("What is knn");
        assertTrue("Expected KNN detailed definition", answer.contains("KNN (K-Nearest Neighbors) - Detailed"));
        assertTrue("Expected Euclidean distance details", answer.contains("Euclidean"));
    }

    @Test
    public void testGetAnswerWithImageNoJsonError() throws InterruptedException {
        final AtomicReference<String> result = new AtomicReference<>();
        final CountDownLatch latch = new CountDownLatch(1);

        engine.getAnswerWithImage("Explain this ML diagram", null, null, ans -> {
            result.set(ans);
            latch.countDown();
        });

        assertTrue("Expected async completion", latch.await(5, TimeUnit.SECONDS));
        assertNotNull("Result should not be null", result.get());
        assertFalse("Output must NOT contain raw JSON error string", result.get().contains("API_KEY_INVALID"));
        assertFalse("Output must NOT contain raw error JSON structure", result.get().contains("\"error\":"));
    }

    @Test
    public void testProcessAndAnalyzeBitmapRawDataNull() {
        String answer = engine.processAndAnalyzeBitmapRawData(null, "Explain image");
        assertEquals("Could not decode image pixels.", answer);
    }
}
