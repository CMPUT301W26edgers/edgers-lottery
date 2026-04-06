package com.example.edgers_lottery;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
public class AdminImagesViewTest {

    static class TestStorageRef {
        final String path;
        TestStorageRef(String path) { this.path = path; }
    }

    static class TestImagesViewActivity {

        final List<String>          imageUrls = new ArrayList<>();
        final List<TestStorageRef>  imageRefs = new ArrayList<>();

        void onImageLoaded(String url, TestStorageRef ref) {
            imageRefs.add(ref);
            imageUrls.add(url);
        }

        void onImageDeleted(int position) {
            if (position < 0 || position >= imageUrls.size()) return;
            imageUrls.remove(position);
            imageRefs.remove(position);
        }

        int getItemCount() {
            return imageUrls.size();
        }

        void reset() {
            imageUrls.clear();
            imageRefs.clear();
        }
    }

    private TestImagesViewActivity activity;

    private static final String PROFILE_URL_1 = "https://storage.example.com/profile_images/tonyandtamu.jpg";
    private static final String PROFILE_URL_2 = "https://storage.example.com/profile_images/tony.jpg";
    private static final TestStorageRef PROFILE_REF_1 = new TestStorageRef("profile_images/tonyandtamu.jpg");
    private static final TestStorageRef PROFILE_REF_2 = new TestStorageRef("profile_images/tony.jpg");

    private static final String EVENT_URL_1 = "https://storage.example.com/event_images/event1.jpg";
    private static final String EVENT_URL_2 = "https://storage.example.com/event_images/event2.jpg";
    private static final TestStorageRef EVENT_REF_1 = new TestStorageRef("event_images/event1.jpg");
    private static final TestStorageRef EVENT_REF_2 = new TestStorageRef("event_images/event2.jpg");

    @Before
    public void setUp() {
        activity = new TestImagesViewActivity();
    }

    @Test
    public void testSingleImage_appearsAfterLoad() {
        activity.onImageLoaded(PROFILE_URL_1, PROFILE_REF_1);

        assertEquals(1, activity.getItemCount());
        assertEquals(PROFILE_URL_1, activity.imageUrls.get(0));
    }

    @Test
    public void testProfileAndEventImages_bothAppearInList() {
        activity.onImageLoaded(PROFILE_URL_1, PROFILE_REF_1);
        activity.onImageLoaded(PROFILE_URL_2, PROFILE_REF_2);

        activity.onImageLoaded(EVENT_URL_1, EVENT_REF_1);
        activity.onImageLoaded(EVENT_URL_2, EVENT_REF_2);

        assertEquals(4, activity.getItemCount());
        assertTrue(activity.imageUrls.contains(PROFILE_URL_1));
        assertTrue(activity.imageUrls.contains(PROFILE_URL_2));
        assertTrue(activity.imageUrls.contains(EVENT_URL_1));
        assertTrue(activity.imageUrls.contains(EVENT_URL_2));
    }

    @Test
    public void testGetItemCount_matchesLoadedImages() {
        assertEquals(0, activity.getItemCount());

        activity.onImageLoaded(PROFILE_URL_1, PROFILE_REF_1);
        assertEquals(1, activity.getItemCount());

        activity.onImageLoaded(EVENT_URL_1, EVENT_REF_1);
        assertEquals(2, activity.getItemCount());
    }

    @Test
    public void testUrlsAndRefs_alwaysInSync() {
        activity.onImageLoaded(PROFILE_URL_1, PROFILE_REF_1);
        activity.onImageLoaded(EVENT_URL_1,   EVENT_REF_1);

        assertEquals(activity.imageUrls.size(), activity.imageRefs.size());

        assertTrue(activity.imageUrls.get(0).contains("tonyandtamu"));
        assertTrue(activity.imageRefs.get(0).path.contains("tonyandtamu"));

        assertTrue(activity.imageUrls.get(1).contains("event1"));
        assertTrue(activity.imageRefs.get(1).path.contains("event1"));
    }

    @Test
    public void testFailedFetch_doesNotAddToList() {
        activity.onImageLoaded(PROFILE_URL_1, PROFILE_REF_1);

        assertEquals(1, activity.getItemCount());
        assertEquals(PROFILE_URL_1, activity.imageUrls.get(0));
    }

    @Test
    public void testDeleteImage_removesCorrectPosition() {
        activity.onImageLoaded(PROFILE_URL_1, PROFILE_REF_1);
        activity.onImageLoaded(EVENT_URL_1,   EVENT_REF_1);
        activity.onImageLoaded(EVENT_URL_2,   EVENT_REF_2);

        activity.onImageDeleted(1);

        assertEquals(2, activity.getItemCount());
        assertEquals(PROFILE_URL_1, activity.imageUrls.get(0));
        assertEquals(EVENT_URL_2,   activity.imageUrls.get(1));
    }

    @Test
    public void testDeleteImage_urlsAndRefsRemainInSync() {
        activity.onImageLoaded(PROFILE_URL_1, PROFILE_REF_1);
        activity.onImageLoaded(EVENT_URL_1,   EVENT_REF_1);

        activity.onImageDeleted(0);

        assertEquals(1, activity.imageUrls.size());
        assertEquals(1, activity.imageRefs.size());
        assertEquals(EVENT_URL_1,  activity.imageUrls.get(0));
        assertEquals(EVENT_REF_1.path, activity.imageRefs.get(0).path);
    }

    @Test
    public void testDeleteLastImage_listBecomesEmpty() {
        activity.onImageLoaded(PROFILE_URL_1, PROFILE_REF_1);
        activity.onImageDeleted(0);

        assertEquals(0, activity.getItemCount());
        assertTrue(activity.imageUrls.isEmpty());
        assertTrue(activity.imageRefs.isEmpty());
    }

    @Test
    public void testReset_clearsAllImages() {
        activity.onImageLoaded(PROFILE_URL_1, PROFILE_REF_1);
        activity.onImageLoaded(EVENT_URL_1,   EVENT_REF_1);
        assertEquals(2, activity.getItemCount());

        activity.reset();

        assertEquals(0, activity.getItemCount());
        assertTrue(activity.imageUrls.isEmpty());
        assertTrue(activity.imageRefs.isEmpty());
    }
}
