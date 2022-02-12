package com.npes87184.s2tdroid.ui.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.anggrayudi.storage.SimpleStorageHelper
import com.anggrayudi.storage.extension.closeStreamQuietly
import com.anggrayudi.storage.file.getBasePath
import com.anggrayudi.storage.file.openInputStream
import com.anggrayudi.storage.file.openOutputStream
import com.npes87184.s2tdroid.R
import com.npes87184.s2tdroid.libs.Transformer.toTraditional
import org.mozilla.universalchardet.UniversalDetector
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter


class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private val storageHelper = SimpleStorageHelper(this)
    private var sourceFile: DocumentFile? = null
    private var targetFolder: DocumentFile? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        val filesTextView: TextView = root.findViewById(R.id.select_file_text)
        val outputFolderTestView: TextView = root.findViewById(R.id.output_folder_text)
        val startBtn: Button = root.findViewById<Button>(R.id.start_button)
        homeViewModel.text.observe(viewLifecycleOwner, Observer {
            filesTextView.text = it
        })
        val filesSelectionCard = root.findViewById<CardView>(R.id.select_file_card)
        val targetFolderSelectionCard = root.findViewById<CardView>(R.id.select_target_folder_card)

        filesSelectionCard.setOnClickListener {
            storageHelper.openFilePicker()
        }

        targetFolderSelectionCard.setOnClickListener {
            storageHelper.openFolderPicker()
        }

        storageHelper.onFolderSelected = { requestCode, folder ->
            outputFolderTestView.text = folder.getBasePath(container!!.context)
            targetFolder = folder
        }
        storageHelper.onFileSelected = { requestCode, files ->
            if (files.isNotEmpty()) {
                filesTextView.text = files[0].getBasePath(container!!.context)
                sourceFile = files[0]
            }
        }

        startBtn.setOnClickListener {
            startTransform(container!!.context)
        }

        return root
    }

    private fun startTransform(context: Context) {
        if (sourceFile == null || targetFolder == null) {
            return
        }
        val encode = getEncode(context, sourceFile!!)
        val inputStream = sourceFile!!.openInputStream(context)
        val targetFolderUri = DocumentFile.fromTreeUri(context, targetFolder!!.uri)
        val destFile = targetFolderUri!!.createFile("text/plain", "123.txt")
        val destStream = destFile!!.openOutputStream(context, false)
        val destStreamWriter = OutputStreamWriter(destStream)
        val destBufferedWriter = BufferedWriter(destStreamWriter)

        BufferedReader(InputStreamReader(inputStream, encode)).forEachLine {
            destBufferedWriter.write(toTraditional(it))
            destBufferedWriter.newLine()
        }

        destBufferedWriter.close()
        destStreamWriter.close()
        destStream.closeStreamQuietly()
        inputStream.closeStreamQuietly()
    }

    private fun getEncode(context: Context, file: DocumentFile): String {
        val inputStream = file.openInputStream(context)
        val buf = ByteArray(4096)
        val detector = UniversalDetector(null)
        var nread: Int
        var encodeString: String

        while (inputStream!!.read(buf).also { nread = it } > 0 && !detector.isDone) {
            detector.handleData(buf, 0, nread)
        }
        detector.dataEnd()

        encodeString = detector.detectedCharset
        if (encodeString == null) {
            encodeString = "Unicode"
        }
        detector.reset()
        inputStream.closeStreamQuietly()
        return encodeString
    }
}