package com.example.aadhaarqr

import android.Manifest
import android.R
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.aadhaarqr.databinding.ActivityMainBinding
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.io.StringReader
import java.util.Objects


class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null

    private val binding: ActivityMainBinding
        get() = _binding!!

    var requestCode = 1001

    var pageHeight = 1120
    var pageWidth = 792

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.ll.visibility = View.INVISIBLE

        checkPermissions()

        scanCode()

        binding.generatePdf.setOnClickListener {
            generatePDF(getPrescriptions())
        }
    }


    private fun scanCode() {
        val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == 2) {
                val scannedData = result.data?.getStringExtra("data")
                if (scannedData != null) {
                    val userData = parseUserData(scannedData)
                    val imageStr = userData.base64Image
//                    var imageBytes = Base64.decode(userData.base64Image, Base64.DEFAULT)
//                    Glide.with(this).load(imageBytes).into(binding.photo)

////                    val imageBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
//                    val byteArrayOutputStream = ByteArrayOutputStream()
//
//                    val bitmap = BitmapFactory.decodeResource(resources, R.drawable.picture_frame)
//                    bitmap.compress(Bitmap.CompressFormat.PNG, 100,byteArrayOutputStream)
//                    var imageBytes: ByteArray = byteArrayOutputStream.toByteArray()
//                    val imageString: String = Base64.encodeToString(imageBytes, Base64.DEFAULT)
////                    imageBytes = Base64.decode(imageString, Base64.DEFAULT)
//                    val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
//                    if (imageBitmap != null) {
//                        binding.photo.setImageBitmap(imageBitmap)
//                    }
                    binding.uid.text = userData.username
                    binding.name.text = userData.name
                    binding.gender.text = userData.gender
                    binding.mobileNumber.text = userData.mobileNumber
                    binding.dateOfBirth.text = userData.dateOfBirth
                    binding.address.text = userData.address
                    binding.ll.visibility = View.VISIBLE
                }
            }
        }

        binding.llScanQRCode.setOnClickListener {
            resultLauncher.launch(Intent(this, ScanActivity::class.java))
        }
    }

    private fun parseUserData(xmlData: String): UserData {
        val xml = XmlPullParserFactory.newInstance().newPullParser()
        xml.setInput(StringReader(xmlData))

        var name = ""
        var username = ""
        var gender = ""
        var mobileNumber = ""
        var dateOfBirth = ""
        var address = ""
        var base64Image = ""

        while (xml.eventType != XmlPullParser.END_DOCUMENT) {
            when (xml.eventType) {
                XmlPullParser.START_TAG -> {
                    when (xml.name) {
                        "QPDB" -> {
                            name = xml.getAttributeValue(null, "n")
                            username = xml.getAttributeValue(null, "u")
                            gender = xml.getAttributeValue(null, "g")
                            mobileNumber = xml.getAttributeValue(null, "m")
                            dateOfBirth = xml.getAttributeValue(null, "d")
                            address = xml.getAttributeValue(null, "a")
                            base64Image = xml.getAttributeValue(null, "i")
                        }
                        "PrintLetterBarcodeData" -> {
                            name = xml.getAttributeValue(null, "name")
                            username = xml.getAttributeValue(null, "uid")
                            gender = xml.getAttributeValue(null, "gender")
                            dateOfBirth = xml.getAttributeValue(null, "dob")
                            val co = xml.getAttributeValue(null, "co")
                            val house = xml.getAttributeValue(null, "house")
                            val street = xml.getAttributeValue(null, "street")
                            val lm = xml.getAttributeValue(null, "lm")
                            val loc = xml.getAttributeValue(null, "loc")
                            val vtc = xml.getAttributeValue(null, "vtc")
                            val po = xml.getAttributeValue(null, "po")
                            val dist = xml.getAttributeValue(null, "dist")
                            val subdist = xml.getAttributeValue(null, "subdist")
                            val state = xml.getAttributeValue(null, "state")
                            val pc = xml.getAttributeValue(null, "pc")
                            address = "$co, $house $street, $lm, $loc, $vtc, $po, $dist, $subdist, $state - $pc"
                        }
                    }
                }
            }
            xml.next()
        }

        return UserData(name, username, gender, mobileNumber, dateOfBirth, address, base64Image)
    }

    private fun parseOldUserData(xml: XmlPullParser): OldUserData {
        var uid = ""
        var name = ""
        var gender = ""
        var yob = ""
        var co = ""
        var house = ""
        var street = ""
        var lm = ""
        var loc = ""
        var vtc = ""
        var po = ""
        var dist = ""
        var subdist = ""
        var state = ""
        var pc = ""
        var dob = ""

        while (xml.eventType != XmlPullParser.END_DOCUMENT) {
            when (xml.eventType) {
                XmlPullParser.START_TAG -> {
                    when (xml.name) {
                        "PrintLetterBarCodeData" -> {
                            uid = xml.getAttributeValue(null, "uid")
                            name = xml.getAttributeValue(null, "name")
                            gender = xml.getAttributeValue(null, "gender")
                            yob = xml.getAttributeValue(null, "yob")
                            co = xml.getAttributeValue(null, "co")
                            house = xml.getAttributeValue(null, "house")
                            street = xml.getAttributeValue(null, "street")
                            lm = xml.getAttributeValue(null, "lm")
                            loc = xml.getAttributeValue(null, "loc")
                            vtc = xml.getAttributeValue(null, "vtc")
                            po = xml.getAttributeValue(null, "po")
                            dist = xml.getAttributeValue(null, "dist")
                            subdist = xml.getAttributeValue(null, "subdist")
                            state = xml.getAttributeValue(null, "state")
                            pc = xml.getAttributeValue(null, "pc")
                            dob = xml.getAttributeValue(null, "dob")
                        }
                    }
                }
            }
            xml.next()
        }

        return OldUserData(uid, name, gender, yob, co, house, street, lm, loc, vtc, po, dist, subdist, state, pc, dob)
    }
    private fun getPrescriptions(): List<Prescription> {
        val prescriptions = mutableListOf<Prescription>()
        val prescription =  Prescription("Paracetamol", "Twice", "Day", 4, "Before lunch and Before Dinner")
        prescriptions.add(prescription)
        prescriptions.add(prescription)
        prescriptions.add(prescription)
        return prescriptions
    }


    private fun generatePDF(prescriptions: List<Prescription>) {

        val pdfDocument: PdfDocument = PdfDocument()

        val heading: Paint = Paint()
        val content: Paint = Paint()

        val myPageInfo: PdfDocument.PageInfo? =
            PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()

        val myPage: PdfDocument.Page = pdfDocument.startPage(myPageInfo)
        val canvas: Canvas = myPage.canvas

        // Set up initial positions for the table
        val xPosition = 70F
        var y = 250F // Declare y as a var
        val rowHeight = 50F

        content.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL))
        content.textSize = 15F
        content.color = ContextCompat.getColor(this, R.color.black)
        content.textAlign = Paint.Align.CENTER

        heading.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL))
        heading.textSize = 40F
        heading.color = ContextCompat.getColor(this, R.color.black)
        heading.textAlign = Paint.Align.CENTER

        canvas.drawText("Prescriptions", 396F, 150F, heading)

        // Define fixed column widths
        val columnWidth = 150F

        // Draw table header
        canvas.drawText("Medication", xPosition, y, content)
        canvas.drawText("Frequency", xPosition + columnWidth, y, content)
        canvas.drawText("Duration", xPosition + 2 * columnWidth, y, content)
        canvas.drawText("Quantity", xPosition + 3 * columnWidth, y, content)
        canvas.drawText("Instructions", xPosition + 4 * columnWidth, y, content)

        // Move down to the first row
        y += rowHeight // Reassign y

        // Iterate through the list of prescriptions and draw each as a row
        for (prescription in prescriptions) {
            // Draw each field with a fixed width
            drawTextWithWrapping(canvas, prescription.medication, xPosition, y, columnWidth, content)
            drawTextWithWrapping(canvas, prescription.frequency, xPosition + columnWidth, y, columnWidth, content)
            drawTextWithWrapping(canvas, prescription.duration, xPosition + 2 * columnWidth, y, columnWidth, content)
            drawTextWithWrapping(canvas, prescription.quantity.toString(), xPosition + 3 * columnWidth, y, columnWidth, content)
            drawTextWithWrapping(canvas, prescription.instructions, xPosition + 4 * columnWidth, y, columnWidth, content)

            // Move down to the next row
            y += rowHeight // Reassign y
        }

        pdfDocument.finishPage(myPage)

        val outputStream: OutputStream
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            outputStream = createPdfForApi33()
        } else {
            outputStream = FileOutputStream(File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "AadhaarCard.pdf"))
        }

        try {
            pdfDocument.writeTo(outputStream)

            Toast.makeText(applicationContext, "PDF file generated..", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()

            Toast.makeText(applicationContext, "Fail to generate PDF file..", Toast.LENGTH_SHORT)
                .show()
        }
        pdfDocument.close()
    }

    private fun drawTextWithWrapping(canvas: Canvas, text: String, x: Float, y: Float, maxWidth: Float, paint: Paint) {
        var yPos = y
        val textLines = wrapText(text, paint, maxWidth)
        for (line in textLines) {
            canvas.drawText(line, x, yPos, paint)
            yPos += paint.textSize
        }
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val result = mutableListOf<String>()
        val words = text.split(" ")
        var currentLine = ""
        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            val lineWidth = paint.measureText(testLine)
            if (lineWidth <= maxWidth) {
                currentLine = testLine
            } else {
                result.add(currentLine)
                currentLine = word
            }
        }
        if (currentLine.isNotEmpty()) {
            result.add(currentLine)
        }
        return result
    }

    private fun createPdfForApi33(): OutputStream {
            val outst: OutputStream
            val contentValues = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, "AadhaarCard.pdf")
                put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            val pdfUri: Uri? = contentResolver.insert(
                MediaStore.Files.getContentUri("external"),
                contentValues
            )
            outst = pdfUri?.let { contentResolver.openOutputStream(it) }!!
            Objects.requireNonNull(outst)
        return outst
    }

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }


    private fun checkPermissions() {

        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            )
        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
        if (!hasPermission(permissions[0])) {
            permissionLauncher.launch(permissions)
        }
    }

    private var permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        var isGranted = true
        for (item in it){
            if (!item.value) {
                isGranted = false
            }
        }
        if (isGranted) {
            Toast.makeText(this, "Permissions Granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Permissions Denied", Toast.LENGTH_SHORT).show()

        }
    }


}