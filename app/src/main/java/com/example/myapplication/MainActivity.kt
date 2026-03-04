package com.example.myapplication

import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.Typeface
import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.AnimationDrawable
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import java.util.Locale
import kotlin.math.abs

data class RhythmNote(
    val time: Long, 
    val lane: Int, 
    var spawned: Boolean = false, 
    var judged: Boolean = false,
    val isHold: Boolean = false,
    val duration: Long = 0,
    var isBeingHeld: Boolean = false
)

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {

    lateinit var startScreen: LinearLayout
    lateinit var categoryScreen: LinearLayout
    lateinit var quizScreen: LinearLayout
    lateinit var resultScreen: LinearLayout
    lateinit var rhythmLayout: RelativeLayout
    lateinit var noteLaneLayout: FrameLayout
    
    lateinit var rhythmScoreText: TextView
    lateinit var rhythmTimerText: TextView
    
    lateinit var receptorLeft: ImageView
    lateinit var receptorDown: ImageView
    lateinit var receptorUp: ImageView
    lateinit var receptorRight: ImageView

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
    var rhythmScore = 0
    var rhythmTimer: CountDownTimer? = null

    // game data
    private var isRhythmActive = false
    private val travelTime = 2000L 
    private val activeNotes = mutableListOf<View>()
    private val noteMap = mutableListOf<RhythmNote>()
    private val handler = Handler(Looper.getMainLooper())
    private val isHolding = BooleanArray(4) { false }

    private fun generateSongMap() {
        noteMap.clear()
        var time = 3000L
        val endTime = 3000L + 230000L 
        val msPerBeat = 414L 
        
        while (time < endTime) {
            val lane = (0..3).random()
            val relTime = time - 3000L
            val isSpike = relTime in 130000L..190000L
            val rand = (0..100).random()
            
            if (relTime < 30000L) {
                noteMap.add(RhythmNote(time, lane))
                time += msPerBeat
            } else if (isSpike) {
                if (rand > 75) {
                    for (i in 0..5) {
                        noteMap.add(RhythmNote(time, (0..3).random()))
                        time += msPerBeat / 4
                    }
                } else if (rand > 40) {
                    noteMap.add(RhythmNote(time, lane))
                    noteMap.add(RhythmNote(time, (0..3).filter { it != lane }.random()))
                    time += msPerBeat / 2
                } else {
                    noteMap.add(RhythmNote(time, lane, isHold = true, duration = msPerBeat * 4))
                    time += msPerBeat * 2
                }
            } else {
                if (rand > 85) {
                    noteMap.add(RhythmNote(time, lane, isHold = true, duration = msPerBeat * 2))
                    time += msPerBeat * 2
                } else {
                    noteMap.add(RhythmNote(time, lane))
                    time += msPerBeat
                }
            }
        }
    }

    // quiz banks
    val htmlCssQuestions = listOf(
        Question("Which language is used to structure a webpage?", listOf("CSS", "Java", "HTML", "Kotlin"), listOf(2)),
        Question("Which CSS property changes the background color?", listOf("bg-color", "background-color", "color", "font-color"), listOf(1)),
        Question("In HTML, which tag is used to create a hyperlink?", listOf("<link>", "<a>", "<href>", "<url>"), listOf(1)),
        Question("What does CSS mainly control?", listOf("Logic", "Database", "Design and layout", "Server"), listOf(2)),
        Question("Which HTML tag is used to insert an image?", listOf("<image>", "<img>", "<pic>", "<src>"), listOf(1)),
        Question("What does HTML stand for?", listOf("Hyperlinks and Text Marking", "Hyper Text Markup Language", "Home Tool Markup", "Hyper Tool Multi Language"), listOf(1)),
        Question("Which CSS property changes the text color?", listOf("font-color", "text-style", "color", "background-color"), listOf(2)),
        Question("Where is the correct place to link an external CSS file?", listOf("Inside the <body>", "Inside the <head>", "At the bottom", "Inside <footer>"), listOf(1)),
        Question("How do you select an element with id='header' in CSS?", listOf(".header", "#header", "header", "*header"), listOf(1)),
        Question("Which HTML tag is used for the largest heading?", listOf("<heading>", "<h6>", "<h1>", "<head>"), listOf(2))
    )

    val javaQuestions = listOf(
        Question("Which symbol is used to end a statement in Java?", listOf(".", ",", ":", ";"), listOf(3)),
        Question("Which keyword is used to define a class in Java?", listOf("define", "struct", "class", "object"), listOf(2)),
        Question("Which data type stores whole numbers in Java?", listOf("String", "int", "float", "boolean"), listOf(1)),
        Question("Which method is the entry point of a Java program?", listOf("start()", "run()", "main()", "init()"), listOf(2)),
        Question("Which data type stores true/false in Java?", listOf("bool", "boolean", "truth", "binary"), listOf(1)),
        Question("What does OOP stand for?", listOf("Object-Oriented Programming", "Only Object Process", "Object Operating Program", "Open Object Programming"), listOf(0)),
        Question("Which operator is used for addition in Java?", listOf("&", "+", "*", "="), listOf(1)),
        Question("Which keyword is used to create an object in Java?", listOf("new", "create", "make", "object"), listOf(0)),
        Question("Which keyword is used to inherit a class in Java?", listOf("implement", "inherits", "extends", "super"), listOf(2)),
        Question("What does JVM stand for?", listOf("Java Variable Method", "Java Virtual Machine", "Java Verified Mode", "Java Visual Model"), listOf(1))
    )

    val kotlinQuestions = listOf(
        Question("Which keyword declares a variable that can change?", listOf("val", "var", "let", "const"), listOf(1)),
        Question("Which company developed Kotlin?", listOf("Microsoft", "Apple", "Google", "JetBrains"), listOf(3)),
        Question("Kotlin is supported for Android development by which company?", listOf("Apple", "Microsoft", "Google", "IBM"), listOf(2)),
        Question("How do you declare a variable that cannot be changed?", listOf("var", "let", "const", "val"), listOf(3)),
        Question("Which keyword is used to define a function in Kotlin?", listOf("fun", "function", "define", "method"), listOf(0)),
        Question("Kotlin is interoperable with which language?", listOf("Python", "Java", "C++", "Swift"), listOf(1)),
        Question("What is the extension of a Kotlin file?", listOf(".kt", ".java", ".ktl", ".kot"), listOf(0)),
        Question("Which keyword is used for a 'null-safe' call in Kotlin?", listOf("?.", "!!", ":", "?:"), listOf(0)),
        Question("How do you define a constant in Kotlin?", listOf("const val", "final", "static", "fixed"), listOf(0)),
        Question("Which keyword is used for a string template in Kotlin?", listOf("#", "@", "$", "&"), listOf(2))
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
        rhythmLayout = findViewById(R.id.rhythmLayout)
        noteLaneLayout = findViewById(R.id.noteLaneLayout)
        
        rhythmScoreText = findViewById(R.id.rhythmScoreText)
        rhythmTimerText = findViewById(R.id.rhythmTimerText)
        
        receptorLeft = findViewById(R.id.receptorLeft)
        receptorDown = findViewById(R.id.receptorDown)
        receptorUp = findViewById(R.id.receptorUp)
        receptorRight = findViewById(R.id.receptorRight)

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

        setupJuicyView(findViewById(R.id.startButton))
        setupJuicyView(findViewById(R.id.category1Btn))
        setupJuicyView(findViewById(R.id.category2Btn))
        setupJuicyView(findViewById(R.id.category3Btn))
        setupJuicyView(findViewById(R.id.rhythmBtn))
        
        setupJuicyView(option1)
        setupJuicyView(option2)
        setupJuicyView(option3)
        setupJuicyView(option4)
        setupJuicyView(findViewById(R.id.restartButton))

        findViewById<Button>(R.id.startButton).setOnClickListener {
            startScreen.animate().alpha(0f).setDuration(300).withEndAction {
                startScreen.visibility = View.GONE
                startScreen.alpha = 1f
                categoryScreen.visibility = View.VISIBLE
                categoryScreen.alpha = 0f
                categoryScreen.animate().alpha(1f).setDuration(300).start()
            }.start()
        }

        findViewById<View>(R.id.category1Btn).setOnClickListener { startQuiz(htmlCssQuestions, R.raw.bluearchive, R.raw.coin) }
        findViewById<View>(R.id.category2Btn).setOnClickListener { startQuiz(javaQuestions, R.raw.consky, R.raw.coin) }
        findViewById<View>(R.id.category3Btn).setOnClickListener { startQuiz(kotlinQuestions, R.raw.pixel, R.raw.coin) }
        findViewById<View>(R.id.rhythmBtn).setOnClickListener { startRhythmGame() }

        findViewById<Button>(R.id.restartButton).setOnClickListener {
            stopBgm()
            rhythmTimer?.cancel()
            playMenuMusic()
            startScreen.visibility = View.VISIBLE
            resultScreen.visibility = View.GONE
            rhythmLayout.visibility = View.GONE
            quizScreen.visibility = View.GONE
        }

        option1.setOnClickListener { check(0) }
        option2.setOnClickListener { check(1) }
        option3.setOnClickListener { check(2) }
        option4.setOnClickListener { check(3) }

        setupReceptor(receptorLeft, 0)
        setupReceptor(receptorDown, 1)
        setupReceptor(receptorUp, 2)
        setupReceptor(receptorRight, 3)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) tts?.language = Locale.US
    }

    private fun setupReceptor(view: ImageView, lane: Int) {
        view.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isHolding[lane] = true
                    v.alpha = 1.0f
                    checkHit(lane)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    isHolding[lane] = false
                    v.animate().alpha(0.4f).setDuration(100).start()
                }
            }
            true
        }
    }

    fun setupJuicyView(view: View) {
        view.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).start()
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start()
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
                if (drawable is AnimatedImageDrawable) (drawable as AnimatedImageDrawable).start()
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

    // quiz part
    fun startQuiz(list: List<Question>, music: Int, sound: Int) {
        stopMenuMusic()
        currentList = list.shuffled()
        qNum = 0
        score = 0
        sfxId = sound
        categoryScreen.visibility = View.GONE
        quizScreen.visibility = View.VISIBLE
        stopBgm()
        bgm = MediaPlayer.create(this, music)
        bgm?.isLooping = true
        bgm?.setVolume(0.25f, 0.25f)
        bgm?.start()
        nextQuestionMascots()
        next()
    }

    fun nextQuestionMascots() {
        // clear old stuff
        faustImage.setImageDrawable(null)
        faustImage.background = null
        xiImage.setImageDrawable(null)
        xiImage.background = null
        
        // 50/50 for Jov vs Limbus
        if ((0..1).random() == 1) {
            faustImage.setImageResource(R.drawable.jov1)
            xiImage.setImageResource(R.drawable.jov2)
        } else {
            faustImage.setBackgroundResource(R.drawable.faust_anim)
            (faustImage.background as AnimationDrawable).start()
            xiImage.setBackgroundResource(R.drawable.xi_anim)
            (xiImage.background as AnimationDrawable).start()
        }
        wobbleMascots()
    }

    fun next() {
        if (qNum >= currentList.size) return
        val q = currentList[qNum]
        questionTextView.text = q.text
        option1.text = q.options[0]
        option2.text = q.options[1]
        option3.text = q.options[2]
        option4.text = q.options[3]
        progressTextView.text = "Question ${qNum + 1} of ${currentList.size}"
        tts?.speak(q.text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun check(idx: Int) {
        if (qNum >= currentList.size) return
        val q = currentList[qNum]
        val s = MediaPlayer.create(this, sfxId); s.setOnCompletionListener { it.release() }; s.start()
        if (q.correctIndices.contains(idx)) score++
        qNum++
        if (qNum < currentList.size) {
            nextQuestionMascots() // change mascots per question
            next()
        } else endQuiz()
    }

    private fun endQuiz() {
        stopBgm()
        quizScreen.visibility = View.GONE
        resultScreen.visibility = View.VISIBLE
        scoreResultTextView.text = "You got $score out of ${currentList.size} correct!"
    }

    fun wobbleMascots() {
        if (quizScreen.visibility != View.VISIBLE) return
        faustImage.animate().rotation(3f).setDuration(800).withEndAction {
            faustImage.animate().rotation(-3f).setDuration(800).withEndAction { if (quizScreen.visibility == View.VISIBLE) wobbleMascots() }.start()
        }.start()
        xiImage.animate().rotation(3f).setDuration(800).withEndAction {
            xiImage.animate().rotation(-3f).setDuration(800).start()
        }.start()
    }

    // rhythm part
    fun startRhythmGame() {
        stopMenuMusic()
        isRhythmActive = true
        categoryScreen.visibility = View.GONE
        rhythmLayout.visibility = View.VISIBLE
        rhythmScore = 0
        rhythmScoreText.text = "Score: 0"
        generateSongMap()
        stopBgm()
        bgm = MediaPlayer.create(this, R.raw.childrenofthecity); bgm?.start()
        rhythmTimer = object : CountDownTimer(230000, 1000) { 
            override fun onTick(ms: Long) { rhythmTimerText.text = String.format("%d:%02d", ms/60000, (ms%60000)/1000) }
            override fun onFinish() { endRhythmGame() }
        }.start()
        handler.post(gameLoop)
    }

    private val gameLoop = object : Runnable {
        override fun run() {
            if (!isRhythmActive || bgm == null) return
            val curTime = bgm!!.currentPosition.toLong()
            noteMap.forEach { note ->
                if (!note.spawned && curTime >= note.time - travelTime) {
                    note.spawned = true; spawnFallingArrow(note)
                }
                if (!note.judged && curTime > note.time + 200) {
                    note.judged = true; showJudgment("MISS", Color.RED)
                    rhythmScore = (rhythmScore - (if (note.isHold) 420 else 150)).coerceAtLeast(0)
                    rhythmScoreText.text = "Score: $rhythmScore"
                }
                if (note.isHold && note.isBeingHeld) {
                    if (curTime > note.time + note.duration) {
                        note.isBeingHeld = false; note.judged = true; rhythmScore += 250; showJudgment("FINISHED!", Color.CYAN)
                    } else if (!isHolding[note.lane]) {
                        note.isBeingHeld = false; note.judged = true; showJudgment("GOOD", Color.LTGRAY); rhythmScore += 100
                    } else { rhythmScore += 3 }
                    rhythmScoreText.text = "Score: $rhythmScore"
                }
            }
            if (bgm!!.isPlaying) handler.postDelayed(this, 16) else endRhythmGame()
        }
    }

    fun spawnFallingArrow(note: RhythmNote) {
        val density = resources.displayMetrics.density
        val baseSize = (80 * density).toInt()
        val container = FrameLayout(this)
        val tailHeight = if (note.isHold) (note.duration * (rhythmLayout.height / travelTime.toFloat())) else 0f
        val containerHeight = (baseSize + tailHeight).toInt()
        container.layoutParams = FrameLayout.LayoutParams(baseSize, containerHeight).apply {
            leftMargin = ((rhythmLayout.width / 4) * note.lane) + ((rhythmLayout.width / 8) - (baseSize / 2))
        }
        if (note.isHold) {
            val tail = View(this).apply {
                layoutParams = FrameLayout.LayoutParams((baseSize * 0.45f).toInt(), tailHeight.toInt()).apply { gravity = 1; topMargin = baseSize / 2 }
                setBackgroundColor(when(note.lane) { 0 -> Color.RED; 1 -> Color.BLUE; 2 -> Color.GREEN; else -> Color.YELLOW })
                alpha = 0.6f
            }
            container.addView(tail)
        }
        val arrow = ImageView(this).apply {
            setImageResource(when(note.lane) { 0 -> R.drawable.ic_arrow_left; 1 -> R.drawable.ic_arrow_down; 2 -> R.drawable.ic_arrow_up; else -> R.drawable.ic_arrow_right })
            layoutParams = FrameLayout.LayoutParams(baseSize, baseSize)
        }
        container.addView(arrow); noteLaneLayout.addView(container); container.tag = note; activeNotes.add(container)
        container.translationY = -containerHeight.toFloat()
        container.animate().translationY(rhythmLayout.height.toFloat()).setDuration(travelTime + 500).setInterpolator(null)
            .withEndAction { noteLaneLayout.removeView(container); activeNotes.remove(container) }.start()
    }

    fun checkHit(lane: Int) {
        val curTime = bgm?.currentPosition?.toLong() ?: 0
        val hitView = activeNotes.filter { (it.tag as RhythmNote).lane == lane && !(it.tag as RhythmNote).judged }
            .minByOrNull { abs((it.tag as RhythmNote).time - curTime) } ?: run {
                rhythmScore = (rhythmScore - 150).coerceAtLeast(0)
                rhythmScoreText.text = "Score: $rhythmScore"
                return
            }
        val note = hitView.tag as RhythmNote
        val diff = abs(note.time - curTime)
        if (diff > 250) {
            rhythmScore = (rhythmScore - 150).coerceAtLeast(0) 
            rhythmScoreText.text = "Score: $rhythmScore"
            return
        }
        if (note.isHold) { note.isBeingHeld = true; hitView.alpha = 0.4f; showJudgment("HOLD!", Color.YELLOW) }
        else {
            note.judged = true
            when {
                diff < 80 -> { rhythmScore += 250; showJudgment("PERFECT", Color.CYAN) }
                diff < 140 -> { rhythmScore += 150; showJudgment("GREAT", Color.GREEN) }
                diff < 200 -> { rhythmScore += 100; showJudgment("GOOD", Color.YELLOW) }
                else -> { rhythmScore += 75; showJudgment("BAD", Color.MAGENTA) }
            }
            noteLaneLayout.removeView(hitView); activeNotes.remove(hitView)
        }
        rhythmScoreText.text = "Score: $rhythmScore"
    }

    fun showJudgment(t: String, c: Int) {
        val v = TextView(this).apply { text = t; setTextColor(c); textSize = 35f; typeface = Typeface.DEFAULT_BOLD }
        rhythmLayout.addView(v, RelativeLayout.LayoutParams(-2, -2).apply { addRule(13) })
        v.animate().alpha(0f).translationY(-150f).setDuration(500).withEndAction { rhythmLayout.removeView(v) }.start()
    }

    fun updateScore() { rhythmScoreText.text = "Score: $rhythmScore" }
    fun endRhythmGame() { isRhythmActive = false; rhythmTimer?.cancel(); rhythmLayout.visibility = View.GONE; resultScreen.visibility = View.VISIBLE; scoreResultTextView.text = "Finished! Score: $rhythmScore" }
    fun stopBgm() { bgm?.stop(); bgm?.release(); bgm = null }
    override fun onPause() { super.onPause(); bgm?.pause(); menuMusic?.pause() }
    override fun onResume() { super.onResume(); if (isRhythmActive) bgm?.start() else playMenuMusic() }
    override fun onDestroy() { super.onDestroy(); stopBgm(); stopMenuMusic(); tts?.shutdown() }
}

class Question(val text: String, val options: List<String>, val correctIndices: List<Int>)
