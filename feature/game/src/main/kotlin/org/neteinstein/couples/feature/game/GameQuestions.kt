package org.neteinstein.couples.feature.game

/**
 * Static "Do we know each other well?" card content for the Game tab - unlike
 * [org.neteinstein.couples.feature.home.HomeScreen]'s questions, this set is fixed, unfiltered,
 * and not persisted/tracked as used, so it lives here as plain Kotlin rather than seeded into the
 * Room database. Mirrors [org.neteinstein.couples.data.source.QuestionSeedData]'s one-list-per-
 * language shape so every supported app language ("en", "pt", "es", "de", "fr") has a full,
 * matching set of 23 questions.
 */
object GameQuestions {
    private val englishQuestions =
        listOf(
            GameQuestion(id = 1, text = "What's my favorite dish?"),
            GameQuestion(id = 2, text = "What's my favorite movie?"),
            GameQuestion(id = 3, text = "What's my favorite music?"),
            GameQuestion(id = 4, text = "What's my favorite flower?"),
            GameQuestion(id = 5, text = "What's my favorite book?"),
            GameQuestion(id = 6, text = "What's my full name?"),
            GameQuestion(id = 7, text = "What's my birthday?"),
            GameQuestion(id = 8, text = "What date did we meet?"),
            GameQuestion(id = 9, text = "What's my favorite color?"),
            GameQuestion(id = 10, text = "What was the first meal we ate together? And what did we drink?"),
            GameQuestion(id = 11, text = "What's the app I spend the most time on, on my phone?"),
            GameQuestion(id = 12, text = "When and where did we first kiss?"),
            GameQuestion(id = 13, text = "What do I love about you?"),
            GameQuestion(id = 14, text = "What would I like you to have less of?"),
            GameQuestion(id = 15, text = "What do I see you doing in 20 years?"),
            GameQuestion(id = 16, text = "How can you make me happy?"),
            GameQuestion(id = 17, text = "Feet or hands?"),
            GameQuestion(id = 18, text = "What's my favorite guilty pleasure?"),
            GameQuestion(id = 19, text = "What's the sentence I say to you the most?"),
            GameQuestion(id = 20, text = "Who's my celebrity crush?"),
            GameQuestion(id = 21, text = "What's the color of my underwear right now?"),
            GameQuestion(id = 22, text = "Love or passion?"),
            GameQuestion(id = 23, text = "What part of my body do I love for you to touch? And which part do I not really like?"),
        )

    private val portugueseQuestions =
        listOf(
            GameQuestion(id = 1, text = "Qual é o meu prato favorito?"),
            GameQuestion(id = 2, text = "Qual é o meu filme favorito?"),
            GameQuestion(id = 3, text = "Qual é a minha música favorita?"),
            GameQuestion(id = 4, text = "Qual é a minha flor favorita?"),
            GameQuestion(id = 5, text = "Qual é o meu livro favorito?"),
            GameQuestion(id = 6, text = "Qual é o meu nome completo?"),
            GameQuestion(id = 7, text = "Quando é o meu aniversário?"),
            GameQuestion(id = 8, text = "Em que data nos conhecemos?"),
            GameQuestion(id = 9, text = "Qual é a minha cor favorita?"),
            GameQuestion(id = 10, text = "Qual foi a primeira refeição que comemos juntos? E o que bebemos?"),
            GameQuestion(id = 11, text = "Qual é a aplicação em que passo mais tempo no telemóvel?"),
            GameQuestion(id = 12, text = "Quando e onde nos beijámos pela primeira vez?"),
            GameQuestion(id = 13, text = "O que é que eu adoro em ti?"),
            GameQuestion(id = 14, text = "O que é que eu gostava que tivesses menos?"),
            GameQuestion(id = 15, text = "O que é que eu te vejo a fazer daqui a 20 anos?"),
            GameQuestion(id = 16, text = "Como é que me podes fazer feliz?"),
            GameQuestion(id = 17, text = "Pés ou mãos?"),
            GameQuestion(id = 18, text = "Qual é o meu prazer culpado favorito?"),
            GameQuestion(id = 19, text = "Qual é a frase que mais te digo?"),
            GameQuestion(id = 20, text = "Quem é a minha paixão de celebridade?"),
            GameQuestion(id = 21, text = "De que cor é a minha roupa interior agora?"),
            GameQuestion(id = 22, text = "Amor ou paixão?"),
            GameQuestion(id = 23, text = "Que parte do meu corpo adoro que me toques? E qual é a que não gosto muito?"),
        )

    private val spanishQuestions =
        listOf(
            GameQuestion(id = 1, text = "¿Cuál es mi plato favorito?"),
            GameQuestion(id = 2, text = "¿Cuál es mi película favorita?"),
            GameQuestion(id = 3, text = "¿Cuál es mi música favorita?"),
            GameQuestion(id = 4, text = "¿Cuál es mi flor favorita?"),
            GameQuestion(id = 5, text = "¿Cuál es mi libro favorito?"),
            GameQuestion(id = 6, text = "¿Cuál es mi nombre completo?"),
            GameQuestion(id = 7, text = "¿Cuándo es mi cumpleaños?"),
            GameQuestion(id = 8, text = "¿En qué fecha nos conocimos?"),
            GameQuestion(id = 9, text = "¿Cuál es mi color favorito?"),
            GameQuestion(id = 10, text = "¿Cuál fue la primera comida que compartimos? ¿Y qué bebimos?"),
            GameQuestion(id = 11, text = "¿Cuál es la aplicación en la que paso más tiempo en el móvil?"),
            GameQuestion(id = 12, text = "¿Cuándo y dónde nos besamos por primera vez?"),
            GameQuestion(id = 13, text = "¿Qué es lo que más me gusta de ti?"),
            GameQuestion(id = 14, text = "¿Qué me gustaría que tuvieras menos?"),
            GameQuestion(id = 15, text = "¿Dónde te veo dentro de 20 años?"),
            GameQuestion(id = 16, text = "¿Cómo puedes hacerme feliz?"),
            GameQuestion(id = 17, text = "¿Pies o manos?"),
            GameQuestion(id = 18, text = "¿Cuál es mi placer culpable favorito?"),
            GameQuestion(id = 19, text = "¿Cuál es la frase que más te digo?"),
            GameQuestion(id = 20, text = "¿Quién es mi crush famoso?"),
            GameQuestion(id = 21, text = "¿De qué color es mi ropa interior ahora mismo?"),
            GameQuestion(id = 22, text = "¿Amor o pasión?"),
            GameQuestion(id = 23, text = "¿Qué parte de mi cuerpo me encanta que toques? ¿Y cuál no me gusta tanto?"),
        )

    private val frenchQuestions =
        listOf(
            GameQuestion(id = 1, text = "Quel est mon plat préféré ?"),
            GameQuestion(id = 2, text = "Quel est mon film préféré ?"),
            GameQuestion(id = 3, text = "Quelle est ma musique préférée ?"),
            GameQuestion(id = 4, text = "Quelle est ma fleur préférée ?"),
            GameQuestion(id = 5, text = "Quel est mon livre préféré ?"),
            GameQuestion(id = 6, text = "Quel est mon nom complet ?"),
            GameQuestion(id = 7, text = "Quand est mon anniversaire ?"),
            GameQuestion(id = 8, text = "À quelle date nous sommes-nous rencontrés ?"),
            GameQuestion(id = 9, text = "Quelle est ma couleur préférée ?"),
            GameQuestion(id = 10, text = "Quel a été le premier repas que nous avons partagé ? Et qu'avons-nous bu ?"),
            GameQuestion(id = 11, text = "Quelle est l'application sur laquelle je passe le plus de temps sur mon téléphone ?"),
            GameQuestion(id = 12, text = "Quand et où nous sommes-nous embrassés pour la première fois ?"),
            GameQuestion(id = 13, text = "Qu'est-ce que j'aime chez toi ?"),
            GameQuestion(id = 14, text = "Qu'aimerais-je que tu aies moins ?"),
            GameQuestion(id = 15, text = "Où me vois-tu dans 20 ans ?"),
            GameQuestion(id = 16, text = "Comment peux-tu me rendre heureux(se) ?"),
            GameQuestion(id = 17, text = "Pieds ou mains ?"),
            GameQuestion(id = 18, text = "Quel est mon plaisir coupable préféré ?"),
            GameQuestion(id = 19, text = "Quelle est la phrase que je te dis le plus souvent ?"),
            GameQuestion(id = 20, text = "Quelle célébrité est mon crush ?"),
            GameQuestion(id = 21, text = "De quelle couleur sont mes sous-vêtements en ce moment ?"),
            GameQuestion(id = 22, text = "Amour ou passion ?"),
            GameQuestion(id = 23, text = "Quelle partie de mon corps j'adore que tu touches ? Et laquelle je n'aime pas vraiment ?"),
        )

    private val germanQuestions =
        listOf(
            GameQuestion(id = 1, text = "Was ist mein Lieblingsgericht?"),
            GameQuestion(id = 2, text = "Was ist mein Lieblingsfilm?"),
            GameQuestion(id = 3, text = "Was ist meine Lieblingsmusik?"),
            GameQuestion(id = 4, text = "Was ist meine Lieblingsblume?"),
            GameQuestion(id = 5, text = "Was ist mein Lieblingsbuch?"),
            GameQuestion(id = 6, text = "Wie lautet mein vollständiger Name?"),
            GameQuestion(id = 7, text = "Wann ist mein Geburtstag?"),
            GameQuestion(id = 8, text = "An welchem Datum haben wir uns kennengelernt?"),
            GameQuestion(id = 9, text = "Was ist meine Lieblingsfarbe?"),
            GameQuestion(id = 10, text = "Was war die erste Mahlzeit, die wir zusammen gegessen haben? Und was haben wir getrunken?"),
            GameQuestion(id = 11, text = "Auf welcher App verbringe ich die meiste Zeit auf meinem Handy?"),
            GameQuestion(id = 12, text = "Wann und wo haben wir uns zum ersten Mal geküsst?"),
            GameQuestion(id = 13, text = "Was liebe ich an dir?"),
            GameQuestion(id = 14, text = "Wovon hätte ich gerne weniger bei dir?"),
            GameQuestion(id = 15, text = "Wo siehst du mich in 20 Jahren?"),
            GameQuestion(id = 16, text = "Wie kannst du mich glücklich machen?"),
            GameQuestion(id = 17, text = "Füße oder Hände?"),
            GameQuestion(id = 18, text = "Was ist mein liebstes Guilty Pleasure?"),
            GameQuestion(id = 19, text = "Was ist der Satz, den ich am häufigsten zu dir sage?"),
            GameQuestion(id = 20, text = "Wer ist mein Promi-Schwarm?"),
            GameQuestion(id = 21, text = "Welche Farbe hat meine Unterwäsche gerade?"),
            GameQuestion(id = 22, text = "Liebe oder Leidenschaft?"),
            GameQuestion(id = 23, text = "Welchen Teil meines Körpers liebe ich, wenn du ihn berührst? Und welchen mag ich nicht so gern?"),
        )

    private val byLanguageCode =
        mapOf(
            "en" to englishQuestions,
            "pt" to portugueseQuestions,
            "es" to spanishQuestions,
            "fr" to frenchQuestions,
            "de" to germanQuestions,
        )

    fun forLanguage(languageCode: String): List<GameQuestion> = byLanguageCode[languageCode] ?: englishQuestions
}
