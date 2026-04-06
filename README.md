# brewnote_android
android version of brewnote calling convex

# Notes
## memory leak crash
looking at this trace more carefully, I see something different: kswapd0 at 11% — the kernel memory swapper is active, meaning the emulator is under memory pressure too. But more importantly, your app shows 85
major faults consistently on every tab switch.

Let me look at this from a code angle. Each ViewModel launches subscriptions in init using viewModelScope.launch { }, which defaults to Dispatchers.Main. That means convex.subscribe() — which calls into native Rust via
JNA — runs synchronously on the main thread before the coroutine suspends. If JNA/Rust initialization per subscription takes any time, it blocks the UI.

Let me fix all ViewModels to launch subscriptions on Dispatchers.IO:

## MD of convex functions
# Convex Backend Function Reference

## beans

| Function | Type | Args | Description |
|---|---|---|---|
| `getRecentBeans` | query | `paginationOpts` | Paginated list for current user |
| `getBeanById` | query | `id: Id<"beans">` | Single bean with vendors + brews |
| `getBeanVendors` | query | `beanId: Id<"beans">` | Vendors for a bean (with pricing) |
| `getBeanBrews` | query | `beanId: Id<"beans">` | Brews that used a specific bean |
| `getBeans` | query | `paginationOpts, name?, countryOfOrigin?, species?, createdById?` | Paginated + filterable |
| `checkBeanExists` | query | `openFoodFactsId: String` | Lookup by barcode ID |
| `upsertBeans` | mutation | `id?, name?, species?, countryOfOrigin?, openFoodFactsId?, vendorIds?` | Create or partial-patch update |

## beanNotes

| Function | Type | Args | Description |
|---|---|---|---|
| `getRecentBeanNotes` | query | `paginationOpts` | Paginated list for current user (with beans) |
| `getBeanNoteById` | query | `id: Id<"beanNotes">` | Single bean note with linked beans |
| `getBeanNotes` | query | `paginationOpts, title?, createdById?` | Paginated + filterable |
| `getBeanNoteVendors` | query | `beanNoteId: Id<"beanNotes">` | Vendors linked to a bean note's beans |
| `upsertBeanNote` | mutation | `id?, title?, tastingNotes?, personalRating?, beanIds?` | Create or partial-patch update |

## brewNotes

| Function | Type | Args | Description |
|---|---|---|---|
| `getRecentBrewNotes` | query | `paginationOpts` | Paginated list for current user (with beans, equipment, brew method) |
| `getBrewNoteById` | query | `id: Id<"brewNotes">` | Single brew note with all linked data |
| `getBrewNotes` | query | `paginationOpts, brewMethodId?, grindSize?, roast?, rating?, createdById?` | Paginated + filterable |
| `upsertBrewNote` | mutation | `id?, brewMethodId?, notes?, grindSize?, roast?, roastDate?, beansWeight?, beansWeightType?, brewTime?, brewTemperature?, brewTemperatureType?, waterToGrindRatio?, rating?, beanIds?, equipmentIds?` | Create or partial-patch update |

## brewMethods

| Function | Type | Args | Description |
|---|---|---|---|
| `getRecentBrewMethods` | query | `paginationOpts, nameFilter?` | Paginated list for current user, optional name search |
| `getBrewMethodById` | query | `id: Id<"brewMethods">` | Single brew method — **public, no auth required** |
| `getBrewMethods` | query | `paginationOpts, name?` | Paginated + filterable by name |
| `getBrewMethodBrews` | query | `brewMethodId: Id<"brewMethods">` | All brew notes that used a specific brew method |
| `upsertBrewMethod` | mutation | `id?, name?, description?` | Create or partial-patch update |

## equipments

| Function | Type | Args | Description |
|---|---|---|---|
| `getRecentEquipments` | query | `paginationOpts` | Paginated list for current user (with vendors + pricing) |
| `getEquipmentById` | query | `id: Id<"equipment">` | Single equipment item with linked vendors |
| `getEquipments` | query | `paginationOpts, name?, brand?, createdById?` | Paginated + filterable |
| `getBrewEquipments` | query | `brewNoteId: Id<"brewNotes">` | Equipment used in a specific brew note |
| `upsertEquipments` | mutation | `id?, name?, brand?, vendorIds?` | Create or partial-patch update |

## vendors

| Function | Type | Args | Description |
|---|---|---|---|
| `getRecentVendors` | query | `paginationOpts` | Paginated list for current user (with beans + equipment) |
| `getVendors` | query | `paginationOpts, name?, type?, createdById?` | Paginated + filterable |
| `getVendorById` | query | `id: Id<"vendors">` | Single vendor with linked beans + equipment |
| `getBeanVendorLink` | query | `beanId, vendorId` | Junction row between a bean and vendor |
| `getEquipmentVendorLink` | query | `equipmentId, vendorId` | Junction row between equipment and vendor |
| `getBeanVendors` | query | `beanId: Id<"beans">` | All vendors for a bean (with pricing) |
| `getEquipmentVendors` | query | `equipmentId: Id<"equipment">` | All vendors for equipment (with pricing) |
| `upsertVendor` | mutation | `id?, name?, type?, url?` | Create or partial-patch update |
| `upsertBeanVendor` | mutation | `beanId, vendorId, price?, currency?, url?, inStock?` | Create/update bean↔vendor link with pricing |
| `upsertEquipmentVendor` | mutation | `equipmentId, vendorId, price?, currency?, url?, inStock?` | Create/update equipment↔vendor link with pricing |
| `removeBeanVendor` | mutation | `beanId, vendorId` | Delete bean↔vendor link |
| `removeEquipmentVendor` | mutation | `equipmentId, vendorId` | Delete equipment↔vendor link |

## home

| Function | Type | Args | Description |
|---|---|---|---|
| `getHomeStats` | query | _(none)_ | Brews this week + top brew method |

---

## Enum Values

### Roast (`CoffeeBeanRoastEnum`)
`LIGHT` · `MEDIUM_LIGHT` · `MEDIUM` · `MEDIUM_DARK` · `DARK`

### Weight (`WeightEnum`)
`GRAMS` · `OUNCES`

### Temperature (`TemperatureEnum`)
`CELSIUS` · `FAHRENHEIT`

### Bean Species (`BeanSpeciesEnum`)
`ARABICA` · `ROBUSTA` · `LIBERICA` · `EXCELSA`

### Vendor Type
`ONLINE` · `LOCAL` · `USER`

# illegal exception type on save/mutation

the IllegalStateException is caused by lifecycle-related operations (likely from the Clerk or Convex SDKs) being triggered on background threads.
I've implemented the following fixes:
1.
Updated ClerkConvexAuthProvider: Wrapped authenticate and logout operations in withContext(Dispatchers.Main). This ensures that any internal Clerk SDK operations that interact with Android Lifecycles are executed on the main thread, even if called from background tasks.
2.
ViewModel Threading Refactor: Updated all ViewModels (BrewsViewModel, BeansViewModel, BeanNotesViewModel, EquipmentViewModel, VendorsViewModel, BrewMethodsViewModel, and HomeViewModel) to remove Dispatchers.IO from viewModelScope.launch.
◦
Collecting Convex subscriptions and updating StateFlow values now happens on the Main thread (the default for viewModelScope).
◦
Expensive operations like convex.mutation are now explicitly wrapped in withContext(Dispatchers.IO) inside the coroutine, following best practices.
These changes ensure that all UI-impacting state changes and lifecycle-sensitive library calls are safely executed on the main thread, which should resolve the "Method setCurrentState must be called on the main thread" crash.