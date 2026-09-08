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
        "Anime" to mapOf("tr" to "Anime", "ru" to "Аниме", "tk" to "Anime"),
        "Cinema" to mapOf("tr" to "Sinema", "ru" to "Кино", "tk" to "Film"),
        "Dogs" to mapOf("tr" to "Köpekler", "ru" to "Собаки", "tk" to "Itler"),
        "Cats" to mapOf("tr" to "Kediler", "ru" to "Кошки", "tk" to "Pişikler"),
        "Wine" to mapOf("tr" to "Şarap", "ru" to "Вино", "tk" to "Şerap"),
        "Running" to mapOf("tr" to "Koşu", "ru" to "Бег", "tk" to "Ylgaw"),
        "Beer" to mapOf("tr" to "Bira", "ru" to "Пиво", "tk" to "Piwo"),
        "Swimming" to mapOf("tr" to "Yüzme", "ru" to "Плавание", "tk" to "Ýüzmek"),
        "Books" to mapOf("tr" to "Kitaplar", "ru" to "Книги", "tk" to "Kitaplar"),
        "Board Games" to mapOf("tr" to "Kutu Oyunları", "ru" to "Настольные игры", "tk" to "Stol oýunlary"),
        "Outdoors" to mapOf("tr" to "Açık Hava", "ru" to "На свежем воздухе", "tk" to "Açyk howa")
    )

    fun getTranslation(name: String, lang: String): String? {
        val entry = translations.entries.find { it.key.equals(name, ignoreCase = true) }
        return entry?.value?.get(lang)
    }
}
