# brewnote_android
android version of brewnote calling convex

# Notes
## memory leak crash
looking at this trace more carefully, I see something different: kswapd0 at 11% — the kernel memory swapper is active, meaning the emulator is under memory pressure too. But more importantly, your app shows 85
major faults consistently on every tab switch.

Let me look at this from a code angle. Each ViewModel launches subscriptions in init using viewModelScope.launch { }, which defaults to Dispatchers.Main. That means convex.subscribe() — which calls into native Rust via
JNA — runs synchronously on the main thread before the coroutine suspends. If JNA/Rust initialization per subscription takes any time, it blocks the UI.

Let me fix all ViewModels to launch subscriptions on Dispatchers.IO: