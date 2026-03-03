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

    // --- RHYTHM GAME DATA ---
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
        val msPerBeat = 414L // 145 BPM
        
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

    // --- QUIZ DATA ---
    val generalQuestions = listOf(
        Question("Who is the strongest PDF of all time??", listOf("Diddy", "Epstein", "Manucom", "Trump"), listOf(2)),
        Question("Who wins in a eating Competition?", listOf("Jeff Regjidor", "Mortera", "Nikako Avocado", "Oguri Cap"), listOf(0)),
        Question("What is 2 + 2?", listOf("3", "4", "5", "Idk 22?"), listOf(1, 3)),
        Question("Strongest Sorcerers OAT!?", listOf("Goatjo", "Fraudkuna", "Yuji", "Bumta"), listOf(1)),
        Question("What is Rhed Marcus Yang Rustia favourite pasttime??", listOf("Reading", "Writing", "Playing", "Sleeping"), listOf(0)),
        Question("Is cereal a soup?", listOf("Yes", "No", "Maybe?", "Don't ask me"), listOf(0))
    )

    val umamusumeQuestions = listOf(
        Question("Who is the 'Silent Suzuka' based on?", listOf("Silence Suzuka", "Special Week", "Gold Ship", "Rice Shower"), listOf(0)),
        Question("Which horse girl is known for her chaotic behavior?", listOf("Grass Wonder", "El Condor Pasa", "Gold Ship", "Mejiro McQueen"), listOf(2))
    )

    val projectMoonQuestions = listOf(
        Question("What is the name of the AI in Lobotomy Corporation?", listOf("Angela", "Carmen", "Binah", "Gebura"), listOf(0)),
        Question("In Library of Ruina, what do you turn guests into?", listOf("Books", "Cards", "Light", "Dust"), listOf(0)),
        Question("Who is the Red Mist?", listOf("Gebura", "Kali", "Binah", "Roland"), listOf(0, 1))
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

        setupJuicyButton(findViewById(R.id.startButton))
        
        val c1 = findViewById<View>(R.id.category1Btn)
        val c2 = findViewById<View>(R.id.category2Btn)
        val c3 = findViewById<View>(R.id.category3Btn)
        val rBtn = findViewById<View>(R.id.rhythmBtn)
        
        setupJuicyView(c1)
        setupJuicyView(c2)
        setupJuicyView(c3)
        setupJuicyView(rBtn)
        
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

        c1.setOnClickListener { startQuiz(generalQuestions, R.raw.bluearchive, R.raw.koyukiuwah, false) }
        c2.setOnClickListener { startQuiz(umamusumeQuestions, R.raw.heliosrap, R.raw.wei, true) }
        c3.setOnClickListener { startQuiz(projectMoonQuestions, R.raw.lor, R.raw.dice, false) }
        rBtn.setOnClickListener { startRhythmGame() }

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

    fun setupJuicyButton(button: Button) {
        button.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).start()
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start()
            }
            false
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

    fun startQuiz(list: List<Question>, music: Int, sound: Int, haru: Boolean) {
        stopMenuMusic()
        currentList = list.shuffled()
        qNum = 0
        score = 0
        sfxId = sound
        isHaruMode = haru
        categoryScreen.visibility = View.GONE
        quizScreen.visibility = View.VISIBLE
        stopBgm()
        bgm = MediaPlayer.create(this, music)
        bgm?.isLooping = true
        bgm?.setVolume(0.15f, 0.15f)
        bgm?.start()
        
        faustImage.setImageDrawable(null)
        xiImage.setImageDrawable(null)
        
        if (isHaruMode) {
            xiImage.scaleX = -1f
            loadGif(R.drawable.haru, faustImage)
            loadGif(R.drawable.teio, xiImage)
        } else {
            xiImage.scaleX = 1f
            faustImage.setBackgroundResource(R.drawable.faust_anim)
            (faustImage.background as AnimationDrawable).start()
            xiImage.setBackgroundResource(R.drawable.xi_anim)
            (xiImage.background as AnimationDrawable).start()
            wobbleMascots()
        }
        next()
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
        playSfx(sfxId)
        if (q.correctIndices.contains(idx)) score++
        qNum++
        if (qNum < currentList.size) next() else endQuiz()
    }

    private fun endQuiz() {
        stopBgm()
        quizScreen.visibility = View.GONE
        resultScreen.visibility = View.VISIBLE
        scoreResultTextView.text = "You got $score out of ${currentList.size} correct!"
    }

    fun wobbleMascots() {
        if (quizScreen.visibility != View.VISIBLE || isHaruMode) return
        faustImage.animate().rotation(3f).setDuration(800).withEndAction {
            faustImage.animate().rotation(-3f).setDuration(800).withEndAction { if (quizScreen.visibility == View.VISIBLE) wobbleMascots() }.start()
        }.start()
        xiImage.animate().rotation(3f).setDuration(800).withEndAction {
            xiImage.animate().rotation(-3f).setDuration(800).start()
        }.start()
    }

    fun startRhythmGame() {
        stopMenuMusic()
        isRhythmActive = true
        categoryScreen.visibility = View.GONE
        rhythmLayout.visibility = View.VISIBLE
        rhythmScore = 0
        updateScore()
        generateSongMap()
        stopBgm()
        bgm = MediaPlayer.create(this, R.raw.childrenofthecity)
        bgm?.start()
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
                    note.spawned = true
                    spawnFallingArrow(note)
                }
                
                if (!note.judged && curTime > note.time + 200) {
                    note.judged = true
                    showJudgment("MISS", Color.RED)
                    val penalty = if (note.isHold) 420 else 150
                    rhythmScore = (rhythmScore - penalty).coerceAtLeast(0)
                    updateScore()
                }

                if (note.isHold && note.isBeingHeld) {
                    if (curTime > note.time + note.duration) {
                        note.isBeingHeld = false; note.judged = true
                        rhythmScore += 500; showJudgment("FINISHED!", Color.CYAN)
                        updateScore()
                    } else if (!isHolding[note.lane]) {
                        note.isBeingHeld = false; note.judged = true
                        showJudgment("RELEASE", Color.LTGRAY); rhythmScore += 250; updateScore()
                    } else {
                        rhythmScore += 3; updateScore()
                    }
                }
            }
            if (bgm!!.isPlaying) handler.postDelayed(this, 16)
            else endRhythmGame()
        }
    }

    fun spawnFallingArrow(note: RhythmNote) {
        val density = resources.displayMetrics.density
        val baseSize = (80 * density).toInt()
        val container = FrameLayout(this)
        val tailHeight = if (note.isHold) (note.duration * (rhythmLayout.height / travelTime.toFloat())) else 0f
        val containerHeight = (baseSize + tailHeight).toInt()
        
        val params = FrameLayout.LayoutParams(baseSize, containerHeight)
        params.leftMargin = ((rhythmLayout.width / 4) * note.lane) + ((rhythmLayout.width / 8) - (baseSize / 2))
        container.layoutParams = params

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
        container.addView(arrow)
        noteLaneLayout.addView(container)
        container.tag = note
        activeNotes.add(container)
        container.translationY = -containerHeight.toFloat()
        container.animate().translationY(rhythmLayout.height.toFloat()).setDuration(travelTime + 500).setInterpolator(null)
            .withEndAction { noteLaneLayout.removeView(container); activeNotes.remove(container) }.start()
    }

    fun checkHit(lane: Int) {
        val curTime = bgm?.currentPosition?.toLong() ?: 0
        val hitView = activeNotes.filter { (it.tag as RhythmNote).lane == lane && !(it.tag as RhythmNote).judged }
            .minByOrNull { abs((it.tag as RhythmNote).time - curTime) } ?: return

        val note = hitView.tag as RhythmNote
        val diff = abs(note.time - curTime)
        if (diff > 250) return

        if (note.isHold) {
            note.isBeingHeld = true
            hitView.alpha = 0.4f
            showJudgment("HOLD!", Color.YELLOW)
        } else {
            note.judged = true
            when {
                diff < 80 -> { rhythmScore += 100; showJudgment("PERFECT", Color.CYAN) }
                diff < 160 -> { rhythmScore += 50; showJudgment("GOOD", Color.YELLOW) }
                else -> { rhythmScore += 25; showJudgment("BAD", Color.MAGENTA) }
            }
            noteLaneLayout.removeView(hitView); activeNotes.remove(hitView)
        }
        updateScore()
    }

    fun showJudgment(t: String, c: Int) {
        val v = TextView(this).apply { text = t; setTextColor(c); textSize = 35f; typeface = Typeface.DEFAULT_BOLD }
        rhythmLayout.addView(v, RelativeLayout.LayoutParams(-2, -2).apply { addRule(13) })
        v.animate().alpha(0f).translationY(-150f).setDuration(500).withEndAction { rhythmLayout.removeView(v) }.start()
    }

    fun updateScore() { rhythmScoreText.text = "Score: $rhythmScore" }
    fun endRhythmGame() { isRhythmActive = false; rhythmTimer?.cancel(); rhythmLayout.visibility = View.GONE; resultScreen.visibility = View.VISIBLE; scoreResultTextView.text = "Finished! Final Score: $rhythmScore" }
    fun stopBgm() { bgm?.stop(); bgm?.release(); bgm = null }
    fun playSfx(id: Int) { val s = MediaPlayer.create(this, id); s.setOnCompletionListener { it.release() }; s.start() }
    fun loadGif(id: Int, v: ImageView) { if (Build.VERSION.SDK_INT >= 28) { val s = ImageDecoder.createSource(resources, id); val d = ImageDecoder.decodeDrawable(s); v.setImageDrawable(d); if (d is AnimatedImageDrawable) d.start() } }
    override fun onPause() { super.onPause(); bgm?.pause(); menuMusic?.pause() }
    override fun onResume() { super.onResume(); if (isRhythmActive || quizScreen.visibility == View.VISIBLE) bgm?.start() else playMenuMusic() }
    override fun onDestroy() { super.onDestroy(); stopBgm(); stopMenuMusic(); tts?.shutdown() }
}

data class Question(val text: String, val options: List<String>, val correctIndices: List<Int>)
