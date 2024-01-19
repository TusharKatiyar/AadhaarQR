package com.example.aadhaarqr

import com.journeyapps.barcodescanner.CaptureActivity
import com.journeyapps.barcodescanner.Util

class CaptureAct : CaptureActivity() {

    // Whenever moving back from the scan screen without scanning
    override fun onBackPressed() {
        super.onBackPressed()
        Util.validateMainThread()
    }
}
