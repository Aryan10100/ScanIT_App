package com.example.scanit

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class MainActivity : AppCompatActivity() {

    lateinit var result: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_main)
        val camera = findViewById<ImageView>(R.id.btnCamera)
        val eraser = findViewById<ImageView>(R.id.btnErase)
        val copy = findViewById<ImageView>(R.id.btnCopy)

        result = findViewById(R.id.resultTv)

        camera.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (intent.resolveActivity(packageManager) != null) {
                startActivityForResult(intent, 123)
            } else {
                Toast.makeText(this, "OOPS! Your camera isn't working.", Toast.LENGTH_SHORT).show()
            }
        }

        eraser.setOnClickListener {
            result.setText("")
        }

        copy.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("label", result.text.toString())
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 123 && resultCode == RESULT_OK) {
            data?.let { intentData ->
                val extras = intentData.extras
                val bitmap = extras?.get("data") as? Bitmap
                if (bitmap != null) {
                    DetectTextUsingML(this, result, bitmap).processImage()
                } else {
                    Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

class DetectTextUsingML(
    private val context: Context,
    private val result: EditText,
    private val bitmap: Bitmap
) {

    fun processImage() {
        val recognizerLatin = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val image = InputImage.fromBitmap(bitmap, 0)

        recognizerLatin.process(image)
            .addOnSuccessListener { visionText ->
                result.setText(visionText.text)
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Oops! Something went wrong", Toast.LENGTH_SHORT).show()
            }
    }

}
