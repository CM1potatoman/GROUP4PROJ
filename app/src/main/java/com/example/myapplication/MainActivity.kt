package com.example.myapplication

import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.AnimationDrawable
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.Button
import android.widget.ImageView
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
    lateinit var progressTextView: TextView
    lateinit var faustImage: ImageView
    lateinit var xiImage: ImageView
    lateinit var spinmaelImage: ImageView

    lateinit var option1: Button
    lateinit var option2: Button
    lateinit var option3: Button
    lateinit var option4: Button

    var bgm: MediaPlayer? = null
    var menuMusic: MediaPlayer? = null
    var sfxId: Int = 0
    var tts: TextToSpeech? = null
    
    var isHaruMode = false

    // questions
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

        tts = TextToSpeech(this, this)

        startScreen = findViewById(R.id.startLayout)
        categoryScreen = findViewById(R.id.categoryLayout)
        quizScreen = findViewById(R.id.quizLayout)
        resultScreen = findViewById(R.id.resultLayout)

        questionTextView = findViewById(R.id.questionText)
        scoreResultTextView = findViewById(R.id.detailedScore)
        progressTextView = findViewById(R.id.progressText)
        faustImage = findViewById(R.id.faustSprite)
        xiImage = findViewById(R.id.xiSprite)
        spinmaelImage = findViewById(R.id.spinmaelImage)

        option1 = findViewById(R.id.option1)
        option2 = findViewById(R.id.option2)
        option3 = findViewById(R.id.option3)
        option4 = findViewById(R.id.option4)

        playMenuMusic()
        loadSpinmael()

        // Juicy buttons setup
        setupJuicyButton(findViewById(R.id.startButton))
        
        // Find them as View to avoid cast errors, since they are LinearLayout IDs now
        val cat1 = findViewById<View>(R.id.category1Btn)
        val cat2 = findViewById<View>(R.id.category2Btn)
        val cat3 = findViewById<View>(R.id.category3Btn)
        
        setupJuicyView(cat1)
        setupJuicyView(cat2)
        setupJuicyView(cat3)
        
        setupJuicyButton(option1)
        setupJuicyButton(option2)
        setupJuicyButton(option3)
        setupJuicyButton(option4)
        setupJuicyButton(findViewById(R.id.restartButton))

        findViewById<Button>(R.id.startButton).setOnClickListener {
            startScreen.animate().alpha(0f).setDuration(300).withEndAction {
                startScreen.visibility = View.GONE
                startScreen.alpha = 1f
                categoryScreen.visibility = View.VISIBLE
                categoryScreen.alpha = 0f
                categoryScreen.animate().alpha(1f).setDuration(300).start()
            }.start()
        }

        cat1.setOnClickListener {
            startQuiz(generalQuestions, R.raw.bluearchive, R.raw.koyukiuwah, false)
        }
        cat2.setOnClickListener {
            startQuiz(umamusumeQuestions, R.raw.heliosrap, R.raw.wei, true)
        }
        cat3.setOnClickListener {
            startQuiz(projectMoonQuestions, R.raw.lor, R.raw.dice, false)
        }

        option1.setOnClickListener { check(0) }
        option2.setOnClickListener { check(1) }
        option3.setOnClickListener { check(2) }
        option4.setOnClickListener { check(3) }

        findViewById<Button>(R.id.restartButton).setOnClickListener {
            stopBgm()
            playMenuMusic()
            startScreen.visibility = View.VISIBLE
            resultScreen.visibility = View.GONE
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.US
        }
    }

    fun setupJuicyButton(button: Button) {
        button.setOnTouchListener { view, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    view.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).start()
                }
                android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                    view.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start()
                }
            }
            false
        }
    }

    fun setupJuicyView(view: View) {
        view.setOnTouchListener { v, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).start()
                }
                android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                    v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start()
                }
            }
            false
        }
    }

    fun loadSpinmael() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                val source = ImageDecoder.createSource(resources, R.drawable.spinmael)
                val drawable = ImageDecoder.decodeDrawable(source)
                spinmaelImage.setImageDrawable(drawable)
                if (drawable is AnimatedImageDrawable) drawable.start()
            } catch (e: Exception) { }
        }
    }

    fun playMenuMusic() {
        if (menuMusic == null) {
            menuMusic = MediaPlayer.create(this, R.raw.menu)
            menuMusic?.isLooping = true
            menuMusic?.setVolume(0.2f, 0.2f)
            menuMusic?.start()
        }
    }

    fun stopMenuMusic() {
        menuMusic?.stop()
        menuMusic?.release()
        menuMusic = null
    }

    fun startQuiz(list: List<Question>, music: Int, sound: Int, haru: Boolean) {
        stopMenuMusic()
        currentList = list.shuffled()
        qNum = 0
        score = 0
        sfxId = sound
        isHaruMode = haru

        // Transition Animation
        categoryScreen.animate()
            .translationX(-categoryScreen.width.toFloat() - 100f)
            .alpha(0f)
            .setDuration(500)
            .withEndAction {
                categoryScreen.visibility = View.GONE
                categoryScreen.translationX = 0f
                categoryScreen.alpha = 1f
            }.start()

        quizScreen.translationX = 1000f 
        quizScreen.alpha = 0f
        quizScreen.visibility = View.VISIBLE

        faustImage.alpha = 0f
        xiImage.alpha = 0f

        quizScreen.animate()
            .translationX(0f)
            .alpha(1f)
            .setDuration(600)
            .withEndAction {
                faustImage.animate().alpha(1f).setDuration(300).start()
                xiImage.animate().alpha(1f).setDuration(300).start()
            }
            .start()

        stopBgm()
        bgm = MediaPlayer.create(this, music)
        bgm?.isLooping = true
        bgm?.setVolume(0.15f, 0.15f)
        bgm?.start()

        faustImage.setImageDrawable(null)
        faustImage.background = null
        xiImage.setImageDrawable(null)
        xiImage.background = null
        xiImage.visibility = View.GONE

        if (isHaruMode) {
            faustImage.animate().rotation(0f).setDuration(0).start()
            xiImage.animate().rotation(0f).setDuration(0).start()
            xiImage.visibility = View.VISIBLE
            xiImage.scaleX = -1f

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                try {
                    val sourceH = ImageDecoder.createSource(resources, R.drawable.haru)
                    val drawableH = ImageDecoder.decodeDrawable(sourceH)
                    faustImage.setImageDrawable(drawableH)
                    if (drawableH is AnimatedImageDrawable) drawableH.start()

                    val sourceT = ImageDecoder.createSource(resources, R.drawable.teio)
                    val drawableT = ImageDecoder.decodeDrawable(sourceT)
                    xiImage.setImageDrawable(drawableT)
                    if (drawableT is AnimatedImageDrawable) drawableT.start()
                } catch (e: Exception) { }
            }
        } else {
            xiImage.visibility = View.VISIBLE
            xiImage.scaleX = 1f
            faustImage.setBackgroundResource(R.drawable.faust_anim)
            val animF = faustImage.background as AnimationDrawable
            animF.start()
            xiImage.setBackgroundResource(R.drawable.xi_anim)
            val animX = xiImage.background as AnimationDrawable
            animX.start()
            wobbleMascots()
        }
        next()
    }

    fun wobbleMascots() {
        if (quizScreen.visibility != View.VISIBLE || isHaruMode) return
        faustImage.animate().rotation(3f).setDuration(800).withEndAction {
            faustImage.animate().rotation(-3f).setDuration(800).withEndAction {
                if (quizScreen.visibility == View.VISIBLE && !isHaruMode) wobbleMascots()
            }.start()
        }.start()
        xiImage.animate().rotation(3f).setDuration(800).withEndAction {
            xiImage.animate().rotation(-3f).setDuration(800).start()
        }.start()
    }

    fun next() {
        val q = currentList[qNum]
        questionTextView.text = q.text
        option1.text = q.options[0]
        option2.text = q.options[1]
        option3.text = q.options[2]
        option4.text = q.options[3]
        progressTextView.text = "Question " + (qNum + 1) + " of " + currentList.size
        tts?.speak(q.text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun check(idx: Int) {
        val q = currentList[qNum]
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
            stopBgm()
            quizScreen.visibility = View.GONE
            resultScreen.visibility = View.VISIBLE
            val res = "You got " + score + " out of " + currentList.size + " correct!"
            scoreResultTextView.text = res
            tts?.speak(res, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    fun stopBgm() {
        bgm?.stop()
        bgm?.release()
        bgm = null
    }

    override fun onDestroy() {
        super.onDestroy()
        stopBgm()
        stopMenuMusic()
        tts?.stop()
        tts?.shutdown()
    }
}

data class Question(
    val text: String,
    val options: List<String>,
    val correctIndices: List<Int>
)