# BlueprintAPK - Turn any website into a standalone Android application
BlueprintAPK is an Android Studio project template that lets you quickly create a standalone Android application out of any URL. The final result is an edge-to-edge kiosk-view experience without a search bar, settings button, or other browser elements.

If you wear an *itty bitty* tin foil hat like me sometimes, you don't want to install closed-source applications for your home lab's services without knowing what the app is precisely doing. With this FOSS alternative, you can create your own APKs to access your home lab services that already have HTTP endpoints. In my case, I use this project for Pi-Hole, Gitea, Grafana, Prometheus, and Mealie to name a few.

While these applications are only usable with an internet connection, it's a quick and easy alternative if you're looking for an app-like feel while accessing your self-hosted service on-the-go.
## Key Features
* Leverages Android WebView to display web pages,
* 100% full screen view (excluding status bar),
* Custom app title, icon and splash screen,
* Supports local network addresses,
* Session data saved upon exit,
* Minimum Android 7.0 support.
## Installation Instructions
The following installation instructions are for advanced users who are already familiar with IDEs or Android Studio. A beginner-friendly step-by-step guide will be coming soon!

1. Install [Android Studio](https://developer.android.com/studio) for your current operating system.
2. Download/clone this repository and extract it to a projects folder.
3. If you plan on creating multiple applications, duplicate the directory and rename it before importing the app.
4. Modify the following files and lines with your preferred application name/url:
	- `app > manifests > AndroidManifest.xml` | Line 17 -> Change `label` to your app's name.
	- `app > kotlin+java > local.blueprint > MainActivity.kt` | Line 1 -> Change `package` to your app's reverse URL.
	- `app > kotlin+java > local.blueprint > MainActivity.kt` | Line 145 -> Set the URL to where you want the application to go to when opened.
	- `app > res > values > strings.xml` | Line 2 -> Change `string` to your app's name.
	- `Gradle Scripts > build.gradle.kts` | Line 6 -> Change `namespace` to your app's reverse URL.
	- `Gradle Scripts > build.gradle.kts` | Line 12 -> Change `applicationId` to your app's reverse URL.
	- `Gradle Scripts > settings.gradle.kts` | Line 25 -> Change `rootProject.name` to your app's name.
5. Right click the `app > res > drawable` directory and select `New > Image Asset` to modify your app's icon and splash.
6. From the top left hamburger menu, go to `Build > Generate Signed App Bundle or APK`
	* Select "APK" from the initial menu.
	* Create a new keystore if you haven't already.
## Artificial Intelligence Disclosure
AI was used as part of this project's code production process with a self-hosted instance of codellama:7b including full code blocks, troubleshooting, bug fixes, and comments. AI was not used in any graphical elements involved with this project.
