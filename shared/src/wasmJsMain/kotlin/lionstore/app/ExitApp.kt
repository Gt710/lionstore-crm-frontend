package lionstore.app

actual fun exitApp() {
    // On web (WasmJs), closing the app is not applicable. No-op.
}
