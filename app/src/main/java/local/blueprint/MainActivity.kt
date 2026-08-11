package local.blueprint

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.CookieManager
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    // Register a modern OS permission launcher
    private val requestNetworkPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val nearbyGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions[Manifest.permission.NEARBY_WIFI_DEVICES] ?: false
        } else {
            true
        }

        // Handle Android 17+ (API 37) ACCESS_LOCAL_NETWORK if applicable
        val localNetworkGranted = if (Build.VERSION.SDK_INT >= 37) {
            permissions["android.permission.ACCESS_LOCAL_NETWORK"] ?: false
        } else {
            true
        }

        if (nearbyGranted && localNetworkGranted) {
            loadUrl()
        } else {
            Toast.makeText(this, "Local Network Permission is required to access local addresses.", Toast.LENGTH_LONG).show()
        }
    }

    @Suppress("DEPRECATION")
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        // INITIALIZE SPLASH SCREEN (Must be called before super.onCreate)
        installSplashScreen()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Find the root layout by its updated ID and apply Window Insets
        val rootLayout = findViewById<ConstraintLayout>(R.id.main_root)

        // Dynamically push the view content down past status bar and camera cutout
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.setPadding(0, insets.top, 0, 0)
            windowInsets
        }

        webView = findViewById(R.id.webView)
        webView.webViewClient = WebViewClient()

        // Handle Chromium internal web permission prompts
        webView.webChromeClient = object : WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest?) {
                request?.grant(request.resources)
            }
        }

        // --- COOKIE CONFIGURATION ---
        setupCookieManager()

        // Configure WebView settings required for local web apps over HTTP
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true // Deprecated in WebKit, but required for legacy Web SQL compatibility
            useWideViewPort = true
            loadWithOverviewMode = true
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            allowFileAccess = true
            allowContentAccess = true
        }

        // Ensure Android's Autofill Framework actively monitors the WebView fields
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            webView.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_YES
        }

        // FORCE DARK MODE VIA ALGORITHMIC DARKENING
        if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
            WebSettingsCompat.setAlgorithmicDarkeningAllowed(webView.settings, true)
        }

        // Intercept back button presses
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })

        // Check and trigger the OS permission dialog before loading the URL
        checkAndRequestPermissions()
    }

    private fun setupCookieManager() {
        val cookieManager = CookieManager.getInstance()

        // Enable acceptance of cookies for this WebView instance
        cookieManager.setAcceptCookie(true)

        // Enable third-party cookies (necessary if modern web apps use cross-domain auth/session cookies)
        cookieManager.setAcceptThirdPartyCookies(webView, true)

        // Force a sync to disk/RAM so active session cookies persist smoothly
        cookieManager.flush()
    }

    private fun checkAndRequestPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        // Check for Android 13+ Nearby Devices permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.NEARBY_WIFI_DEVICES) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.NEARBY_WIFI_DEVICES)
            }
        }

        // Check for Android 17+ explicit Local Network permission
        if (Build.VERSION.SDK_INT >= 37) {
            if (ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_LOCAL_NETWORK") != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add("android.permission.ACCESS_LOCAL_NETWORK")
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestNetworkPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            loadUrl()
        }
    }

    private fun loadUrl() {
        // Inject headers to bypass Chromium's Private Network Access check
        val extraHeaders = HashMap<String, String>().apply {
            put("Access-Control-Allow-Local-Network", "true")
            put("Access-Control-Request-Private-Network", "true")
        }
        webView.loadUrl("http://127.0.0.1:3000", extraHeaders)
    }
}