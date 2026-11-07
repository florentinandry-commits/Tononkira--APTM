package mg.aptm.tononkira

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import java.util.concurrent.Executors
import java.util.zip.ZipInputStream
import javax.xml.parsers.DocumentBuilderFactory

class MainActivity : AppCompatActivity() {
    private lateinit var store: SongStore
    private lateinit var list: LinearLayout
    private var player: MediaPlayer? = null
    private val worker = Executors.newSingleThreadExecutor()
    private val pickImage = 101
    private val pickDoc = 102
    private val pickAudio = 103
    private var pendingImageOcr = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        store = SongStore(this)
        buildUi()
        refresh()
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(18, 18, 18, 18); setBackgroundColor(Color.rgb(247,247,242)) }
        val title = TextView(this).apply { text = "TONONKIRA APTM"; textSize = 25f; setTextColor(Color.rgb(35,75,54)); gravity = Gravity.CENTER; setPadding(0,8,0,18) }
        root.addView(title)
        val row1 = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        row1.addView(btn("➕ Hira vaovao") { editSong(null) }, weight())
        row1.addView(btn("🖼 Sary / Capture OCR") { pick(pickImage, "image/*") }, weight())
        root.addView(row1)
        val row2 = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        row2.addView(btn("Importer PDF / Word") { pick(pickDoc, "*/*") }, weight())
        row2.addView(btn("🎵 Audio") { Toast.makeText(this@MainActivity, "Misafidiana hira aloha, dia ampidiro audio ao amin'ny fanovana.", Toast.LENGTH_LONG).show() }, weight())
        root.addView(row2)
        root.addView(TextView(this).apply { text = "BOKY TONONKIRA"; textSize = 18f; setTextColor(Color.DKGRAY); setPadding(0,20,0,8) })
        val scroll = ScrollView(this)
        list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        scroll.addView(list)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
    }

    private fun weight() = LinearLayout.LayoutParams(0, 52, 1f).apply { setMargins(4,4,4,4) }
    private fun btn(text: String, action: () -> Unit) = Button(this).apply { this.text = text; textSize = 12f; setOnClickListener { action() } }
    private fun pick(code: Int, type: String) {
        val i = Intent(Intent.ACTION_OPEN_DOCUMENT).apply { addCategory(Intent.CATEGORY_OPENABLE); this.type = type }
        startActivityForResult(i, code)
    }

    private fun refresh() {
        list.removeAllViews()
        val songs = store.all()
        if (songs.isEmpty()) list.addView(TextView(this).apply { text = "Mbola tsy misy hira. Ampidiro sary, PDF/Word na mamoròna hira vaovao."; setPadding(8,18,8,18) })
        songs.sortedBy { it.title.lowercase() }.forEach { song ->
            val card = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(10,10,10,10); setBackgroundColor(Color.WHITE) }
            card.addView(TextView(this).apply { text = song.title; textSize = 18f; setTextColor(Color.rgb(35,75,54)) })
            val actions = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            actions.addView(btn("Vakio") { editSong(song) }, weight())
            actions.addView(btn("Projection") { startActivity(Intent(this@MainActivity, ProjectionActivity::class.java).putExtra("title", song.title).putExtra("lyrics", song.lyrics)) }, weight())
            actions.addView(btn("PDF") { openPdfOrToast(song) }, weight())
            actions.addView(btn("Fafao") { store.delete(song.id); refresh() }, weight())
            card.addView(actions)
            list.addView(card, LinearLayout.LayoutParams(-1, -2).apply { setMargins(0,6,0,6) })
        }
    }

    private fun openPdfOrToast(song: Song) {
        Toast.makeText(this, "Ny PDF dia azo sokafana rehefa avy nampidirina; ny lahatsoratra DOCX kosa azo avadika ho hira.", Toast.LENGTH_LONG).show()
    }

    private fun editSong(existing: Song?) {
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(20,10,20,0) }
        val title = EditText(this).apply { hint = "Lohatenin'ny hira"; setText(existing?.title ?: "") }
        val lyrics = EditText(this).apply { hint = "Tononkira"; minLines = 8; gravity = Gravity.TOP; setText(existing?.lyrics ?: "") }
        box.addView(title); box.addView(lyrics)
        val audio = Button(this).apply { text = if (existing?.audioUri == null) "Ampidiro audio" else "Hanova audio"; setOnClickListener { pick(pickAudio, "audio/*") } }
        box.addView(audio)
        android.app.AlertDialog.Builder(this).setTitle("Tononkira").setView(box)
            .setPositiveButton("Tehirizo") { _, _ ->
                val t = title.text.toString().trim()
                if (t.isNotEmpty()) { store.save(Song(existing?.id ?: System.currentTimeMillis(), t, lyrics.text.toString(), existing?.audioUri)); refresh() }
            }.setNegativeButton("Ajanona", null).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return
        val uri = data?.data ?: return
        try { contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) {}
        when (requestCode) {
            pickImage -> runOcr(uri)
            pickDoc -> importDocument(uri)
            pickAudio -> Toast.makeText(this, "Audio voafidy. Ampifandraiso amin'ny hira amin'ny fanovana manaraka.", Toast.LENGTH_LONG).show()
        }
    }

    private fun runOcr(uri: Uri) {
        Toast.makeText(this, "Mamaky ny soratra amin'ny sary...", Toast.LENGTH_SHORT).show()
        try {
            val image = InputImage.fromFilePath(this, uri)
            TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS).process(image)
                .addOnSuccessListener { result ->
                    val extracted = result.text.trim()
                    if (extracted.isBlank()) { Toast.makeText(this, "Tsy nahita soratra ilay OCR.", Toast.LENGTH_LONG).show(); return@addOnSuccessListener }
                    val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(18,0,18,0) }
                    val title = EditText(this).apply { hint = "Lohatenin'ny hira" }
                    val lyrics = EditText(this).apply { setText(extracted); minLines = 10; gravity = Gravity.TOP }
                    box.addView(title); box.addView(lyrics)
                    android.app.AlertDialog.Builder(this).setTitle("Vokatry ny OCR — ahitsio raha ilaina").setView(box)
                        .setPositiveButton("Tehirizo ho hira") { _, _ ->
                            val name = title.text.toString().trim().ifBlank { "Hira ${store.all().size + 1}" }
                            store.save(Song(System.currentTimeMillis(), name, lyrics.text.toString()))
                            refresh()
                        }.setNegativeButton("Ajanona", null).show()
                }
                .addOnFailureListener { Toast.makeText(this, "Tsy nahomby ny OCR: ${it.message}", Toast.LENGTH_LONG).show() }
        } catch (e: Exception) { Toast.makeText(this, "Tsy azo vakina ilay sary: ${e.message}", Toast.LENGTH_LONG).show() }
    }

    private fun importDocument(uri: Uri) {
        val name = (uri.lastPathSegment ?: "").lowercase()
        try {
            val mime = contentResolver.getType(uri) ?: ""
            when {
                mime.contains("pdf") || name.endsWith(".pdf") -> {
                    Toast.makeText(this, "PDF voaray. Sokafy amin'ny mpamaky PDF an'ny finday; azonao ampiasaina koa ny capture hanaovana OCR.", Toast.LENGTH_LONG).show()
                    val view = Intent(Intent.ACTION_VIEW).apply { setDataAndType(uri, "application/pdf"); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }
                    try { startActivity(view) } catch (_: Exception) { Toast.makeText(this, "Tsy misy PDF reader napetraka.", Toast.LENGTH_LONG).show() }
                }
                mime.contains("wordprocessingml") || name.endsWith(".docx") -> {
                    val text = contentResolver.openInputStream(uri)?.use { input ->
                        val zip = ZipInputStream(input)
                        var entry = zip.nextEntry
                        val xml = StringBuilder()
                        while (entry != null) {
                            if (entry.name == "word/document.xml") {
                                val bytes = zip.readBytes()
                                val factory = DocumentBuilderFactory.newInstance()
                                factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
                                val doc = factory.newDocumentBuilder().parse(bytes.inputStream())
                                val nodes = doc.getElementsByTagName("w:p")
                                for (i in 0 until nodes.length) {
                                    val p = nodes.item(i)
                                    val ts = p.childNodes
                                    val line = StringBuilder()
                                    fun walk(n: org.w3c.dom.Node) {
                                        if (n.nodeName.endsWith(":t") || n.nodeName == "w:t") line.append(n.textContent)
                                        for (j in 0 until n.childNodes.length) walk(n.childNodes.item(j))
                                    }
                                    walk(p); if (line.isNotEmpty()) xml.append(line).append('\n')
                                }
                                break
                            }
                            entry = zip.nextEntry
                        }
                        xml.toString()
                    } ?: ""
                    if (text.isBlank()) Toast.makeText(this, "Tsy nahita lahatsoratra tao amin'ny DOCX.", Toast.LENGTH_LONG).show()
                    else {
                        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(18,0,18,0) }
                        val title = EditText(this).apply { hint = "Lohatenin'ny hira"; setText(uri.lastPathSegment?.substringAfterLast('/')?.substringBeforeLast('.') ?: "") }
                        val lyrics = EditText(this).apply { setText(text); minLines = 10; gravity = Gravity.TOP }
                        box.addView(title); box.addView(lyrics)
                        android.app.AlertDialog.Builder(this).setTitle("Import Word — ahitsio").setView(box)
                            .setPositiveButton("Tehirizo") { _, _ ->
                                store.save(Song(System.currentTimeMillis(), title.text.toString().ifBlank { "Hira vaovao" }, lyrics.text.toString())); refresh()
                            }.setNegativeButton("Ajanona", null).show()
                    }
                }
                else -> Toast.makeText(this, "Format tsy tohanana. Ampiasao PDF na Word .docx.", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) { Toast.makeText(this, "Tsy nahomby ny import: ${e.message}", Toast.LENGTH_LONG).show() }
    }

    override fun onDestroy() { player?.release(); worker.shutdown(); super.onDestroy() }
}
