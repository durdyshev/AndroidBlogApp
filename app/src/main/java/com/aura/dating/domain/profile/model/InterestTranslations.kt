package com.aura.dating.domain.profile.model

object InterestTranslations {
    private val translations = mapOf(
        "Music" to mapOf("tr" to "Müzik", "ru" to "Музыка", "tk" to "Saz"),
        "Travel" to mapOf("tr" to "Seyahat", "ru" to "Путешествия", "tk" to "Syýahat"),
        "Gaming" to mapOf("tr" to "Oyun", "ru" to "Игры", "tk" to "Oýunlar"),
        "Coffee" to mapOf("tr" to "Kahve", "ru" to "Кофе", "tk" to "Kofe"),
        "Fitness" to mapOf("tr" to "Fitness", "ru" to "Фитнес", "tk" to "Fitnes"),
        "Art" to mapOf("tr" to "Sanat", "ru" to "Искусство", "tk" to "Sungat"),
        "Photography" to mapOf("tr" to "Fotoğrafçılık", "ru" to "Фотография", "tk" to "Suratçylyk"),
        "Cooking" to mapOf("tr" to "Yemek Yapma", "ru" to "Кулинария", "tk" to "Nahargalyk"),
        "Movies" to mapOf("tr" to "Sinema", "ru" to "Кино", "tk" to "Film"),
        "Reading" to mapOf("tr" to "Kitap Okuma", "ru" to "Чтение", "tk" to "Kitap okamak"),
        "Nature" to mapOf("tr" to "Doğa", "ru" to "Природа", "tk" to "Tebigat"),
        "Pets" to mapOf("tr" to "Evcil Hayvanlar", "ru" to "Животные", "tk" to "Öý haýwanlary"),
        "Sports" to mapOf("tr" to "Spor", "ru" to "Спорт", "tk" to "Sport"),
        "Technology" to mapOf("tr" to "Teknoloji", "ru" to "Технологии", "tk" to "Tehnologiýa"),
        "Fashion" to mapOf("tr" to "Moda", "ru" to "Мода", "tk" to "Moda"),
        "Foodie" to mapOf("tr" to "Gurme", "ru" to "Еда", "tk" to "Tagam"),
        "Hiking" to mapOf("tr" to "Doğa Yürüyüşü", "ru" to "Походы", "tk" to "Gezelenç"),
        "Yoga" to mapOf("tr" to "Yoga", "ru" to "Йога", "tk" to "Ýoga"),
        "Dancing" to mapOf("tr" to "Dans", "ru" to "Танцы", "tk" to "Tans"),
        "Anime" to mapOf("tr" to "Anime", "ru" to "Аниме", "tk" to "Anime")
    )

    fun getTranslation(name: String, lang: String): String? {
        val entry = translations.entries.find { it.key.equals(name, ignoreCase = true) }
        return entry?.value?.get(lang)
    }
}
