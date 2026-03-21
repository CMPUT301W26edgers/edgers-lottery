package com.example.edgers_lottery.models;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class SeedEvents {
    public static void seed() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Event[] events = {
                new Event("Summer Music Festival",
                        "A weekend of live music featuring local and international artists across multiple stages.",
                        "2026-03-18", "12:00 PM", "Hawrelak Park, Edmonton",
                        null, 500, "2026-03-01", "2026-03-15"),

                new Event("Tech Startup Pitch Night",
                        "Entrepreneurs pitch their ideas to a panel of investors. Networking and refreshments included.",
                        "2026-03-19", "6:00 PM", "Edmonton Convention Centre",
                        null, 150, "2026-03-01", "2026-03-17"),

                new Event("Community 5K Run",
                        "A fun run through downtown Edmonton open to all ages and skill levels.",
                        "2026-03-20", "8:00 AM", "Downtown Edmonton",
                        null, 300, "2026-03-01", "2026-03-18"),

                new Event("Art Gallery Opening",
                        "Opening night for a new contemporary art exhibit featuring works from Alberta artists.",
                        "2026-03-21", "7:00 PM", "Art Gallery of Alberta",
                        null, 200, "2026-03-01", "2026-03-19"),

                new Event("Winter Food Fair",
                        "Sample dishes from over 50 local restaurants and food vendors in one place.",
                        "2026-03-14", "11:00 AM", "Northlands Park, Edmonton",
                        null, 1000, "2026-03-01", "2026-03-20"),

                new Event("Edmonton Comic & Pop Culture Expo",
                        "Celebrate comics, gaming, sci-fi, and fantasy with guest panels, cosplay, and vendors.",
                        "2026-03-18", "9:00 AM", "Edmonton EXPO Centre",
                        null, 2000, "2026-03-01", "2026-03-16"),

                new Event("Outdoor Yoga in the Park",
                        "A free community yoga session for all skill levels. Bring your own mat.",
                        "2026-03-19", "9:00 AM", "Victoria Park, Edmonton",
                        null, 100, "2026-03-01", "2026-03-17"),

                new Event("Junior Coding Bootcamp",
                        "A two-day coding workshop for kids aged 10-15. Learn Python and build your first app.",
                        "2026-03-01", "10:00 AM", "Edmonton Public Library - Stanley Milner Branch",
                        null, 40, "2026-03-01", "2026-03-18"),

                new Event("Craft Beer Festival",
                        "Sample over 100 craft beers from Alberta breweries. Food trucks and live music included.",
                        "2026-03-21", "2:00 PM", "Churchill Square, Edmonton",
                        null, 800, "2026-03-01", "2026-03-19"),

                new Event("Halloween Haunted House",
                        "Navigate through a terrifying haunted house experience. Not recommended for young children.",
                        "2026-03-02", "6:00 PM", "Fort Edmonton Park",
                        null, 600, "2026-03-01", "2026-03-20"),

                new Event("Photography Workshop",
                        "Learn composition, lighting, and editing techniques from professional photographers.",
                        "2026-03-23", "1:00 PM", "The Hive Edmonton",
                        null, 30, "2026-03-01", "2026-03-21"),

                new Event("Indigenous Cultural Celebration",
                        "Experience traditional music, dance, food, and art from Indigenous communities across Alberta.",
                        "2026-03-24", "10:00 AM", "Borden Park, Edmonton",
                        null, 500, "2026-03-01", "2026-03-22"),

                new Event("Farmers Market Opening Day",
                        "The first day of the summer farmers market season. Fresh produce, baked goods, and crafts.",
                        "2026-03-25", "8:00 AM", "Old Strathcona Farmers Market",
                        null, 400, "2026-03-01", "2026-03-23"),

                new Event("Trivia Night Championship",
                        "Compete in the ultimate trivia championship across categories like history, science, and pop culture.",
                        "2026-03-18", "7:00 PM", "The Pint Edmonton",
                        null, 120, "2026-03-01", "2026-03-16"),

                new Event("Charity Gala Dinner",
                        "A black-tie fundraising dinner supporting local homeless shelters. Silent auction included.",
                        "2026-03-19", "6:30 PM", "Fairmont Hotel Macdonald, Edmonton",
                        null, 250, "2026-03-01", "2026-03-17"),

                new Event("Outdoor Film Screening",
                        "Watch a classic film under the stars. Blankets and snacks welcome.",
                        "2026-03-20", "8:30 PM", "Hawrelak Park Amphitheatre, Edmonton",
                        null, 350, "2026-03-01", "2026-03-18"),

                new Event("Marathon & Half Marathon",
                        "Annual road race through Edmonton's river valley. Categories for all fitness levels.",
                        "2026-03-21", "7:00 AM", "Legislature Grounds, Edmonton",
                        null, 1500, "2026-03-01", "2026-03-19"),

                new Event("DIY Home Renovation Workshop",
                        "Hands-on workshop covering tiling, drywall, and basic plumbing for homeowners.",
                        "2026-03-22", "10:00 AM", "NAIT Main Campus, Edmonton",
                        null, 60, "2026-03-01", "2026-03-20"),

                new Event("Jazz & Blues Night",
                        "An evening of smooth jazz and blues from talented local musicians.",
                        "2026-03-23", "8:00 PM", "Yardbird Suite, Edmonton",
                        null, 180, "2026-03-01", "2026-03-21"),

                new Event("Spring Countdown Party",
                        "Ring in the spring with live entertainment, a DJ, and a midnight fireworks show.",
                        "2026-03-24", "9:00 PM", "Rogers Place, Edmonton",
                        null, 3000, "2026-03-01", "2026-03-22")
        };

        for (Event event : events) {
            db.collection("events")
                    .add(event)
                    .addOnSuccessListener(documentReference -> {
                        android.util.Log.d("SeedEvents", "Added: " + event.getName() + " ID: " + documentReference.getId());
                    })
                    .addOnFailureListener(e -> {
                        android.util.Log.e("SeedEvents", "Failed to add: " + event.getName(), e);
                    });
        }
    }
    public static void unseed() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference events = db.collection("events");
        events.get()
                .addOnSuccessListener(querySnapshot -> {
                    for (com.google.firebase.firestore.DocumentSnapshot document : querySnapshot.getDocuments()) {
                        events.document(document.getId()).delete();
                        android.util.Log.d("SeedEvents", "Deleted: " + document.getId());
                    }
                }
                )
                .addOnFailureListener(e -> {
                    android.util.Log.e("SeedEvents", "Failed to delete events", e);
                });
    }
}
