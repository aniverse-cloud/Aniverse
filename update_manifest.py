import re

with open("app/src/main/AndroidManifest.xml", "r") as f:
    content = f.read()

# Remove LAUNCHER intent filter from MainActivity
launcher_filter = """            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.category.LAUNCHER" />
            </intent-filter>"""

if launcher_filter in content:
    content = content.replace(launcher_filter, "")

# Add activity-alias for LAUNCHER
alias = """        <activity-alias
            android:name=".Launcher"
            android:targetActivity=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.category.LAUNCHER" />
            </intent-filter>
        </activity-alias>
"""

# Insert alias before </application>
content = content.replace("</application>", f"{alias}\n    </application>")

with open("app/src/main/AndroidManifest.xml", "w") as f:
    f.write(content)
