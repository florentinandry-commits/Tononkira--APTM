package mg.aptm.tononkira

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class ProjectionActivity : AppCompatActivity() {
    private var size = 30f
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.decorView.systemUiVisibility = (4 or  ViewFlags.FULLSCREEN or ViewFlags.HIDE_NAVIGATION or ViewFlags.IMMERSIVE_STICKY)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(22,18,22,18); setBackgroundColor(Color.BLACK) }
        val title = TextView(this).apply { text = intent.getStringExtra("title") ?: "Tononkira"; textSize = 24f; setTextColor(Color.YELLOW); gravity = Gravity.CENTER; setPadding(0,0,0,16) }
        val lyrics = TextView(this).apply { text = intent.getStringExtra("lyrics") ?: ""; textSize = size; setTextColor(Color.WHITE); gravity = Gravity.CENTER; setLineSpacing(8f,1.1f) }
        val scroll = ScrollView(this); scroll.addView(lyrics)
        val controls = LinearLayout(this).apply { gravity = Gravity.CENTER }
        controls.addView(Button(this).apply { text = "A−"; setOnClickListener { size = (size - 2).coerceAtLeast(16f); lyrics.textSize = size } })
        controls.addView(Button(this).apply { text = "A+"; setOnClickListener { size = (size + 2).coerceAtMost(64f); lyrics.textSize = size } })
        controls.addView(Button(this).apply { text = "Miverina"; setOnClickListener { finish() } })
        root.addView(title); root.addView(scroll, LinearLayout.LayoutParams(-1,0,1f)); root.addView(controls)
        setContentView(root)
    }
    private object ViewFlags {
        const val FULLSCREEN = 1024
        const val HIDE_NAVIGATION = 512
        const val IMMERSIVE_STICKY = 4096
    }
}
