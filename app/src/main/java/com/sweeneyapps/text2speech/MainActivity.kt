package com.sweeneyapps.text2speech

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.view.View
import android.widget.*
import java.util.*

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener, AdapterView.OnItemSelectedListener {

    lateinit var textEdit: TextView
    lateinit var speakButton: Button
    lateinit var textToSpeech: TextToSpeech
    lateinit var listVoices: List<Voice>
    var selectedVoice: Int = 0
    val utteranceID: String = "com.sweeneyapps.utteranceID"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPref = getSharedPreferences(getString(R.string.preference_file_key), MODE_PRIVATE) ?: return
        sharedPref.getInt(getString(R.string.preference_selected), 0).also {
            selectedVoice = it
        }


        textEdit = findViewById<TextView>(R.id.textEdit)
        textEdit.requestFocus()

        speakButton = findViewById<Button>(R.id.speakButton)
        speakButton.setOnClickListener {
            val text = textEdit.text

            if(text.isNotEmpty()) {
                textToSpeech.voice = listVoices[selectedVoice]
                textToSpeech.speak(text,
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                     utteranceID)
            }

        }

        textToSpeech = TextToSpeech(this,
            this,
            "com.google.android.tts") // to init

        textToSpeech.setOnUtteranceProgressListener(object: UtteranceProgressListener() {
            override fun onStart(p0: String?) {
                if(!p0.equals(utteranceID)) return

                runOnUiThread {
                    speakButton.isEnabled = false
                }
            }

            override fun onDone(p0: String?) {
                if(!p0.equals(utteranceID)) return

                runOnUiThread {
                    speakButton.isEnabled = true
                    textEdit.text = ""
                }
            }

            override fun onError(p0: String?) {
                if(!p0.equals(utteranceID)) return

                runOnUiThread {
                    Toast.makeText(applicationContext, "Error: I couldn't speak", Toast.LENGTH_LONG).show()
                }

            }
        })
    }



    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech.language = Locale.US

            listVoices = textToSpeech.voices
                .filter { it.locale.equals(Locale("en", "US")) }

            runOnUiThread {
                val spinner = findViewById<Spinner>(R.id.spinner)

                val listOfVoicesName = listVoices.map{ it.name }
                spinner.adapter = ArrayAdapter<String>(
                    this,
                    android.R.layout.simple_spinner_item,
                    listOfVoicesName
                ).also { adapter ->
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                }

                spinner.setSelection(selectedVoice)
                spinner.onItemSelectedListener = this
            }



        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

        val sharedPref = getSharedPreferences(getString(R.string.preference_file_key), MODE_PRIVATE) ?: return
        with (sharedPref.edit()) {
            putInt(getString(R.string.preference_selected), position)
            apply()
        }

        val test = sharedPref.getInt(getString(R.string.preference_selected), 0)
        selectedVoice = position
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        //
    }

    override fun onDestroy() {
        super.onDestroy()
        textToSpeech.shutdown()
    }
}
