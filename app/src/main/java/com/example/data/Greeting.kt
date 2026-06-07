package com.example.data

import java.util.Calendar

data class Greeting(
    val id: Int,
    val text: String,
    val author: String? = null,
    val category: Category,
    val season: Season? = null
) {
    enum class Category {
        HAPPY_UPLIFTING,
        GENTLE_GROUNDING,
        REFLECTIVE_THOUGHTFUL
    }

    enum class Season {
        SPRING, SUMMER, AUTUMN, WINTER
    }
}

object GreetingProvider {
    val morningGreetings = listOf(
        // Happy and Uplifting (50%)
        Greeting(
            id = 1,
            text = "Today is a playground of possibilities. Let your curiosity lead the way, and find joy in the smallest discoveries.",
            category = Greeting.Category.HAPPY_UPLIFTING
        ),
        Greeting(
            id = 2,
            text = "May your cup override with warmth, your steps be light, and your face catch the sunshine. Today is a gift just waiting to unfold.",
            category = Greeting.Category.HAPPY_UPLIFTING
        ),
        Greeting(
            id = 3,
            text = "Every deep breath in is a quiet celebration of being here. Smile softly, move gently, and trust the day ahead.",
            category = Greeting.Category.HAPPY_UPLIFTING
        ),
        Greeting(
            id = 4,
            text = "Let yourself hope with courage today. Something beautiful is quietly shifting into alignment just for you.",
            category = Greeting.Category.HAPPY_UPLIFTING
        ),
        Greeting(
            id = 5,
            text = "There is a beautiful momentum building in your life. Run with your ideas, hold onto gratitude, and shine your brightest.",
            category = Greeting.Category.HAPPY_UPLIFTING
        ),
        Greeting(
            id = 6,
            text = "You carry a spark that can light up any room. Trust your resilience, celebrate your journey, and make today wonderful.",
            category = Greeting.Category.HAPPY_UPLIFTING
        ),
        Greeting(
            id = 7,
            text = "Open your eyes to the friendly universe around you. Today offers a fresh page to write a story filled with laughter, progress, and peace.",
            category = Greeting.Category.HAPPY_UPLIFTING
        ),
        Greeting(
            id = 8,
            text = "Look for the kindness today. In a smile from a stranger, a warm cup, or a quiet victory—goodness is everywhere if we look.",
            category = Greeting.Category.HAPPY_UPLIFTING
        ),

        // Gentle and Grounding (25%)
        Greeting(
            id = 9,
            text = "Listen closely to the morning breeze. It whispers of old forests, deep roots, and the patient beauty of standing still and breathing.",
            category = Greeting.Category.GENTLE_GROUNDING
        ),
        Greeting(
            id = 10,
            text = "Just as the dew softly rests on grass before the sun rises, let yourself find peace in this fleeting moment of stillness. There is no rush.",
            category = Greeting.Category.GENTLE_GROUNDING
        ),
        Greeting(
            id = 11,
            text = "The mountains do not hurry, yet they stand tall. Rivers do not rush, yet they reach the ocean. Trust your own slow, beautiful pace.",
            category = Greeting.Category.GENTLE_GROUNDING
        ),
        Greeting(
            id = 12,
            text = "Plant your feet firmly. Feel the ground support you. You are exactly where you need to be, safe, centered, and deeply rooted.",
            category = Greeting.Category.GENTLE_GROUNDING
        ),

        // Reflective and Thoughtful (25%)
        Greeting(
            id = 13,
            text = "Take a moment to look back with kindness on the path you have walked. You have survived every hard day. You are stronger than you know.",
            category = Greeting.Category.REFLECTIVE_THOUGHTFUL
        ),
        Greeting(
            id = 14,
            text = "What is one tiny promise you can make to yourself today? Let it be a promise of kindness, of rest, or of letting go of what you cannot control.",
            category = Greeting.Category.REFLECTIVE_THOUGHTFUL
        ),
        Greeting(
            id = 15,
            text = "We are all masterpieces in progress. Give yourself permission to be both a work of art and a learning scholar at the exact same time.",
            category = Greeting.Category.REFLECTIVE_THOUGHTFUL
        ),
        Greeting(
            id = 16,
            text = "Behind your eyes lies a quiet depth of wisdom. Quiet the world's noise for ten seconds, and listen to what your heart is trying to tell you.",
            category = Greeting.Category.REFLECTIVE_THOUGHTFUL
        )
    )

    val afternoonGreetings = listOf(
        "The day is still yours.",
        "You can begin again at any moment.",
        "Small steps still matter.",
        "There is still time for something good today.",
        "Take a slow breath. Relax your shoulders. You are doing well.",
        "A quiet heart can navigate any busy afternoon.",
        "May you find a pocket of peace in the hours ahead."
    )

    val birthdayGreetings = listOf(
        "Happy Birthday. Today, the world celebrates the day you arrived. You are a gentle light, a comforting presence, and a wonderful gift. May this year bring you deep peace, quiet joy, and the space to grow exactly as you wish."
    )

    fun getSeasonalGreeting(calendar: Calendar): List<Greeting> {
        val month = calendar.get(Calendar.MONTH) // 0-indexed
        val season = when (month) {
            Calendar.DECEMBER, Calendar.JANUARY, Calendar.FEBRUARY -> Greeting.Season.WINTER
            Calendar.MARCH, Calendar.APRIL, Calendar.MAY -> Greeting.Season.SPRING
            Calendar.JUNE, Calendar.JULY, Calendar.AUGUST -> Greeting.Season.SUMMER
            else -> Greeting.Season.AUTUMN
        }
        return when (season) {
            Greeting.Season.SPRING -> listOf(
                Greeting(101, "Like spring shoots breaking through cold earth, may you feel a gentle renewal surging through your spirit today.", category = Greeting.Category.GENTLE_GROUNDING, season = Greeting.Season.SPRING),
                Greeting(102, "Let yourself blossom quietly. There is a season for growing, a season for waiting, and today is your season for becoming.", category = Greeting.Category.HAPPY_UPLIFTING, season = Greeting.Season.SPRING)
            )
            Greeting.Season.SUMMER -> listOf(
                Greeting(201, "Bask in the abundant warmth of the long summer light. May your heart find a playful, carefree rhythm today.", category = Greeting.Category.HAPPY_UPLIFTING, season = Greeting.Season.SUMMER),
                Greeting(202, "As the summer canopy stands lush and green, remember that you too are in a beautiful season of leafing and life.", category = Greeting.Category.GENTLE_GROUNDING, season = Greeting.Season.SUMMER)
            )
            Greeting.Season.AUTUMN -> listOf(
                Greeting(301, "Nature teaches us how beautiful it is to let things go. Let your worries fall away like autumn gold.", category = Greeting.Category.REFLECTIVE_THOUGHTFUL, season = Greeting.Season.AUTUMN),
                Greeting(302, "As the air turns crisp and gold, gather your quiet thoughts, draw near to your warmth, and celebrate simple harvests.", category = Greeting.Category.GENTLE_GROUNDING, season = Greeting.Season.AUTUMN)
            )
            Greeting.Season.WINTER -> listOf(
                Greeting(401, "In the quiet stillness of winter, the earth rests and prepares for what's next. Give yourself permission to rest and dream in the dark.", category = Greeting.Category.REFLECTIVE_THOUGHTFUL, season = Greeting.Season.WINTER),
                Greeting(402, "The cold outside makes the warmth inside hold sweeter. Find safe, cozy corners for your mind and soul today.", category = Greeting.Category.GENTLE_GROUNDING, season = Greeting.Season.WINTER)
            )
        }
    }

    const val TOTAL_GENERATIVE_QUOTES = 40000

    fun getQuoteAt(index: Int): Greeting {
        // Deterministically construct a deep, soothing mindfulness quote
        val subjects = listOf(
            "A quiet breath", "The silent mind", "Your inner heart", "Deep stillness", "The present moment",
            "A passing cloud", "The gentle breeze", "The morning light", "Soft kindness", "A single step",
            "The flow of time", "A quiet path", "The steady ground", "A patient star", "A cup of warm tea",
            "Your resilient spirit", "A soft smile", "The deep ocean", "Your soul", "The beauty of waiting",
            "Your awareness", "The cycle of nature", "A fallen leaf", "The quiet space within", "The evening shadow"
        )

        val actions = listOf(
            "gently untangles", "quietly illuminates", "tenderly cradles", "softly reveals", "deeply grounds",
            "patiently nourishes", "elegantly whispers", "lovingly protects", "perfectly embraces", "wisely guides",
            "beautifully awakens", "knowingly restores", "harmoniously balances", "serenely heals", "faithfully anchors",
            "smoothly clears"
        )

        val objects = listOf(
            "the weight of yesterday", "the noise of the busy world", "the infinite horizon within",
            "infinite, quiet possibilities", "the simple magic of being alive", "unspoken courage and resilience",
            "a beautiful landscape of peace", "the hidden river of deep joy", "safe, peaceful harbors",
            "clarity in times of change", "the natural rhythm of growth", "unconditional self-acceptance",
            "the quiet poetry of now", "a warm sanctuary of hope", "calm currents in the heart",
            "peace that needs no explanation"
        )

        val transitions = listOf(
            "inviting you to release what is finished.", "showing that you are already complete.",
            "reminding you that there is absolutely no rush.", "offering a soft place to simply notice and exist.",
            "letting you bloom in your own organic time.", "asking nothing of you but matching your true center.",
            "anchoring your steps securely onto the earth.", "gently filling your cup with slow reassurance.",
            "guiding you safely back to your serene soul.", "clearing the path for quiet, patient wisdom.",
            "enabling your thoughts to settle like autumn leaves.", "proving that small steps carry immense light.",
            "breathing comfort into your evening thoughts.", "reconnecting you to the natural flow of life.",
            "opening a door to quiet, steady gratitude.", "wrapping your mind in a soft cloak of comfort."
        )

        val authors = listOf(
            "Sage of the Forest", "Ancient Whisperer", "The Inner Voice", "Sister Peace", "Zen Master Ryokan",
            "A Quiet Mountain", "A Cloud Passing By", "Socrates of the Garden", "Quiet Heart Affirmations",
            "Morning Sun Daily", "The Earthly Spirit", "A Gentle Star", "The Cosmic Cradle", "Ocean Breeze Whisper",
            "The Sage of Solitude", "Universal Breath"
        )

        val categories = listOf(
            Greeting.Category.GENTLE_GROUNDING,
            Greeting.Category.HAPPY_UPLIFTING,
            Greeting.Category.REFLECTIVE_THOUGHTFUL
        )

        val normalizedIndex = index.coerceIn(0, TOTAL_GENERATIVE_QUOTES - 1)

        val sIdx = normalizedIndex % subjects.size
        val aIdx = (normalizedIndex / subjects.size) % actions.size
        val oIdx = (normalizedIndex / (subjects.size * actions.size)) % objects.size
        val tIdx = (normalizedIndex / (subjects.size * actions.size * objects.size)) % transitions.size
        val auIdx = (normalizedIndex / (subjects.size * actions.size * objects.size * transitions.size)) % authors.size
        val cIdx = (normalizedIndex * 7) % categories.size

        val subject = subjects[sIdx]
        val action = actions[aIdx]
        val obj = objects[oIdx]
        val transition = transitions[tIdx]
        val author = authors[auIdx]

        val text = "$subject $action $obj, $transition"

        return Greeting(
            id = normalizedIndex + 10000,
            text = text,
            author = author,
            category = categories[cIdx]
        )
    }
}
