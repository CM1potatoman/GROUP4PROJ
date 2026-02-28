package com.example.myapplication

import android.media.MediaPlayer
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import java.util.Locale

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {

    lateinit var startScreen: LinearLayout
    lateinit var categoryScreen: LinearLayout
    lateinit var quizScreen: LinearLayout
    lateinit var resultScreen: LinearLayout

    lateinit var questionTextView: TextView
    lateinit var scoreResultTextView: TextView

    lateinit var option1: Button
    lateinit var option2: Button
    lateinit var option3: Button
    lateinit var option4: Button

    var bgm: MediaPlayer? = null
    var sfxId: Int = 0
    var tts: TextToSpeech? = null

    // my questions lists
    val generalQuestions = listOf(
        Question("Who is the strongest PDF of all time??", listOf("Diddy", "Epstein", "Manucom", "Trump"), listOf(2)),
        Question("Who wins in a eating Competition?", listOf("Jeff Regjidor", "Mortera", "Nikako Avocado", "Oguri Cap"), listOf(0)),
        Question("What is 2 + 2?", listOf("3", "4", "5", "Idk 22?"), listOf(1, 3)),
        Question("Strongest Sorcerers OAT!?", listOf("Goatjo", "Fraudkuna", "Yuji", "Bumta"), listOf(1)),
        Question("What is the largest ocean?", listOf("Atlantic", "Indian", "Arctic", "Pacific"), listOf(3)),
        Question("W?", listOf("Shakespeare", "Dickens", "Hemingway", "Austen"), listOf(0)),
        Question("If you have 3 apples and I take 2, how many apples do you have?", listOf("1", "2", "3", "Depression"), listOf(3)),
        Question("Which Student is the Cutest and funniest??", listOf("Kisaki", "Koyuki", "Hoshino", "John paul esperanza"), listOf(1)),
        Question("What is Rhed Marcus Yang Rustia favourite pasttime??", listOf("Reading", "Writing", "Playing", "Sleeping"), listOf(0)),
        Question("Is cereal a soup?", listOf("Yes", "No", "Maybe?", "Don't ask me"), listOf(0))
    )

    val umamusumeQuestions = listOf(
        Question("Who is the 'Silent Suzuka' based on?", listOf("Silence Suzuka", "Special Week", "Gold Ship", "Rice Shower"), listOf(0)),
        Question("Which horse girl is known for her chaotic behavior?", listOf("Grass Wonder", "El Condor Pasa", "Gold Ship", "Mejiro McQueen"), listOf(2)),
        Question("What is the name of the main academy?", listOf("Tracen Academy", "Uma Academy", "Derby School", "Runners High"), listOf(0)),
        Question("Who is Special Week's rival?", listOf("Silence Suzuka", "El Condor Pasa", "Grass Wonder", "Seiun Sky"), listOf(2)),
        Question("Which girl has a 'Demon' mode?", listOf("Rice Shower", "Grass Wonder", "Winning Ticket", "Narita Brian"), listOf(1)),
        Question("What is the distance of the Arima Kinen?", listOf("2000m", "2400m", "2500m", "3200m"), listOf(2)),
        Question("Who is the 'Emperor'?", listOf("Symboli Rudolf", "Tokai Teio", "Air Groove", "Emperor of the Imperium of Man"), listOf(0)),
        Question("Which girl is obsessed with F0OD?", listOf("Special Week", "Oguri Cap", "Smart Falcon", "Mejiro Dober"), listOf(1)),
        Question("Who is known as the 'Blue Rose'?", listOf("Rice Shower", "Mejiro Mcqueen", "Super Creek", "Nice Nature"), listOf(0)),
        Question("What do you call the girls' performance after a race?", listOf("Idol Show", "Winning Live", "Victory Dance", "Encore"), listOf(1))
    )

    val projectMoonQuestions = listOf(
        Question("What is the name of the AI in Lobotomy Corporation?", listOf("Angela", "Carmen", "Binah", "Gebura"), listOf(0)),
        Question("In Library of Ruina, what do you turn guests into?", listOf("Books", "Cards", "Light", "Dust"), listOf(0)),
        Question("What is the main setting of Project Moon games?", listOf("The City", "The Nest", "The Backstreets", "The District"), listOf(0)),
        Question("Who is the Red Mist?", listOf("Gebura", "Kali", "Binah", "Roland"), listOf(0, 1)),
        Question("What is the name of the protagonist in Library of Ruina?", listOf("Roland", "Argalia", "Finn", "Philip"), listOf(0)),
        Question("What does L.C. stand for?", listOf("Light Corp", "Life Corp", "Lobotomy Corp", "Liberty Corp"), listOf(2)),
        Question("Who is the 'Black Silence'?", listOf("Roland", "Angelica", "The Blue Reverberation", "Olivier"), listOf(0, 1)),
        Question("Which wing is responsible for the 'T' Corp?", listOf("Time", "Train", "Technology", "Truth"), listOf(0)),
        Question("What is the name of the facility in Limbus Company?", listOf("The Bus", "Mephistopheles", "Charon", "Dante"), listOf(1)),
        Question("How many Sinner's are in Limbus Company?", listOf("10", "11", "12", "13"), listOf(2))
    )

    var currentList = listOf<Question>()
    var qNum = 0
    var score = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // setup tts
        tts = TextToSpeech(this, this)

        // get the screens from layout
        startScreen = findViewById(R.id.startLayout)
        categoryScreen = findViewById(R.id.categoryLayout)
        quizScreen = findViewById(R.id.quizLayout)
        resultScreen = findViewById(R.id.resultLayout)

        questionTextView = findViewById(R.id.questionText)
        scoreResultTextView = findViewById(R.id.detailedScore)

        // buttons for answers
        option1 = findViewById(R.id.option1)
        option2 = findViewById(R.id.option2)
        option3 = findViewById(R.id.option3)
        option4 = findViewById(R.id.option4)

        // start button logic
        findViewById<Button>(R.id.startButton).setOnClickListener {
            startScreen.visibility = View.GONE
            categoryScreen.visibility = View.VISIBLE
        }

        // choosing a category
        findViewById<Button>(R.id.category1Btn).setOnClickListener {
            startIt(generalQuestions, R.raw.bluearchive, R.raw.koyukiuwah)
        }
        findViewById<Button>(R.id.category2Btn).setOnClickListener {
            startIt(umamusumeQuestions, R.raw.heliosrap, R.raw.wei)
        }
        findViewById<Button>(R.id.category3Btn).setOnClickListener {
            startIt(projectMoonQuestions, R.raw.lor, R.raw.dice)
        }

        // clicking answers
        option1.setOnClickListener { check(0) }
        option2.setOnClickListener { check(1) }
        option3.setOnClickListener { check(2) }
        option4.setOnClickListener { check(3) }

        // retry button
        findViewById<Button>(R.id.restartButton).setOnClickListener {
            if (bgm != null) {
                bgm?.stop()
                bgm?.release()
                bgm = null
            }
            startScreen.visibility = View.VISIBLE
            resultScreen.visibility = View.GONE
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.US
        }
    }

    fun startIt(list: List<Question>, music: Int, sound: Int) {
        currentList = list.shuffled()
        qNum = 0
        score = 0
        sfxId = sound
        
        categoryScreen.visibility = View.GONE
        quizScreen.visibility = View.VISIBLE
        
        // play the bgm
        if (bgm != null) {
            bgm?.stop()
            bgm?.release()
        }
        bgm = MediaPlayer.create(this, music)
        bgm?.isLooping = true
        bgm?.setVolume(0.15f, 0.15f) // make it quiet
        bgm?.start()

        next()
    }

    fun next() {
        val q = currentList[qNum]
        questionTextView.text = q.text
        option1.text = q.options[0]
        option2.text = q.options[1]
        option3.text = q.options[2]
        option4.text = q.options[3]

        // make it speak
        tts?.speak(q.text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun check(idx: Int) {
        val q = currentList[qNum]

        // play sfx
        val s = MediaPlayer.create(this, sfxId)
        s.setOnCompletionListener { it.release() }
        s.start()

        if (q.correctIndices.contains(idx)) {
            score = score + 1
        }

        qNum = qNum + 1

        if (qNum < currentList.size) {
            next()
        } else {
            // finish
            if (bgm != null) {
                bgm?.stop()
                bgm?.release()
                bgm = null
            }
            quizScreen.visibility = View.GONE
            resultScreen.visibility = View.VISIBLE
            val res = "You got " + score + " out of " + currentList.size + " correct!"
            scoreResultTextView.text = res
            tts?.speak(res, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bgm?.release()
        tts?.stop()
        tts?.shutdown()
    }
}

data class Question(
    val text: String,
    val options: List<String>,
    val correctIndices: List<Int>
)