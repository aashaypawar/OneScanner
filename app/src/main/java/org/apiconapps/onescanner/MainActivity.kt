package org.apiconapps.onescanner

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode

private const val REQUEST_CODE = 101

class MainActivity : AppCompatActivity() {
    private lateinit var codeScanner: CodeScanner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_main)

        setupPermission()
        codeScanner()
    }

    private fun codeScanner(){
        val scannerView = findViewById<CodeScannerView>(R.id.scanner_view)
        codeScanner = CodeScanner(this, scannerView)
        codeScanner.apply {
            camera = CodeScanner.CAMERA_BACK // or CAMERA_FRONT or specific camera id
            formats = CodeScanner.ALL_FORMATS // list of type BarcodeFormat,
            autoFocusMode = AutoFocusMode.SAFE // or CONTINUOUS
            scanMode = ScanMode.CONTINUOUS // or CONTINUOUS or PREVIEW
            isAutoFocusEnabled = true // Whether to enable auto focus or not
            isFlashEnabled = false // Whether to enable flash or not

            decodeCallback = DecodeCallback {
                runOnUiThread {
                    val result = it.text

                    if(result.isNotEmpty()){
                        if(result.startsWith("upi:") || result.startsWith("http")) {
                            Toast.makeText(applicationContext, result, Toast.LENGTH_SHORT).show()
                            startActivity(Intent(Intent(Intent.ACTION_VIEW, Uri.parse(result))))
                        } else {
                            Toast.makeText(applicationContext, result, Toast.LENGTH_SHORT).show()
                            startActivity(Intent(Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com/search?q=$result"))))
                        }
                    }
                }
            }

            errorCallback = ErrorCallback { // or ErrorCallback.SUPPRESS
                runOnUiThread {
                    Toast.makeText(applicationContext, "Camera initialization error: ${it.message}",
                        Toast.LENGTH_LONG).show()
                }
            }
        }

        scannerView.setOnClickListener {
            codeScanner.startPreview()
        }
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }

    private fun setupPermission(){
        val permission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)

        if(permission != PackageManager.PERMISSION_GRANTED)
        {
            makeRequest()
        }
    }

    private fun makeRequest(){
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            REQUEST_CODE->{
                if(grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, "Needs Permissions", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}