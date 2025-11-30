# Implementasi Animasi Wave/Ripple untuk Chain Reaction

## 📋 Ringkasan

Sistem animasi wave/ripple telah diimplementasikan untuk game Chain Reaction dengan fitur:
- ✅ Animasi wave/ripple effect saat explosion
- ✅ Sequential explosion dengan queue system
- ✅ Smooth orb movement animation
- ✅ Delay mechanism untuk synchronization
- ✅ Thread-safe queue management

## 🏗️ Arsitektur

### 1. **ExplosionQueue** (`core/ExplosionQueue.java`)
Queue system untuk mengelola sequential explosions:
- Thread-safe dengan `ReentrantLock`
- Menyimpan `ExplosionTask` yang berisi cell, board, dan player
- Memproses explosions satu per satu setelah animasi selesai
- Callback system untuk notifikasi ketika queue kosong

**Key Methods:**
- `enqueueExplosion()` - Menambahkan explosion ke queue
- `peekNext()` - Mengambil task berikutnya tanpa menghapus
- `onAnimationComplete()` - Menandai animasi selesai dan execute task
- `processNext()` - Memproses task berikutnya

### 2. **ExplosionAnimation** (`view/ExplosionAnimation.java`)
Class utility untuk membuat berbagai jenis animasi:

**a. Wave/Ripple Animation:**
- 3 layer ripple dengan delay berbeda
- Efek scaling, opacity fade, dan stroke width animation
- Gaussian blur untuk smoothness
- Durasi: 400ms per ripple

**b. Orb Movement Animation:**
- TranslateTransition untuk pergerakan linear
- Scale animation untuk efek bounce
- Opacity pulse untuk efek glow
- Durasi: 300ms

**c. Complete Explosion Animation:**
- Kombinasi: wave (asal) → movement → wave (tujuan)
- Sequential transition untuk urutan yang benar

**d. Pre-Explosion Pulse:**
- Pulse animation sebelum explosion
- Durasi: 300ms dengan 2 cycles

### 3. **AnimationManager** (`view/AnimationManager.java`)
Manager untuk mengkoordinasikan animasi dengan queue:
- Monitor `ExplosionQueue` untuk task baru
- Trigger animasi untuk setiap explosion
- Execute task setelah animasi selesai
- Track animasi yang sedang berjalan

**Flow:**
1. `processNextAnimation()` - Ambil task dari queue
2. Buat animasi untuk setiap neighbor
3. Pre-explosion pulse → Movement animations → Wave animations
4. Setelah semua selesai → Execute explosion task
5. Proses animasi berikutnya

### 4. **AnimatedExplosionStrategy** (`strategy/AnimatedExplosionStrategy.java`)
Strategy pattern untuk explosion dengan animasi:
- Menggunakan `ExplosionQueue` instead of immediate execution
- Semua cells menggunakan strategy ini (di-setup di `GridPanel`)

### 5. **GridPanel** (Modified)
- Extends `StackPane` dengan `GridPane` internal dan `Pane` animation layer
- Mapping `Cell` → `CellView` untuk animasi
- Setup `AnimatedExplosionStrategy` untuk semua cells
- Method `startAnimationProcessing()` untuk trigger animasi

### 6. **GameController** (Modified)
- Callback `onAnimationStart` untuk trigger animasi setelah cell click
- Dipanggil setelah `cell.addOrb()` untuk memulai animasi processing

## 🔄 Flow Eksekusi

```
User Click Cell
    ↓
Cell.addOrb() → Cek criticalMass
    ↓
AnimatedExplosionStrategy.explode() → Queue explosion
    ↓
GameController.onAnimationStart() → GridPanel.startAnimationProcessing()
    ↓
AnimationManager.processNextAnimation()
    ↓
[Animasi Sequence]
    1. Pre-explosion pulse
    2. Wave animation (cell asal)
    3. Orb movement (ke setiap neighbor)
    4. Wave animation (cell tujuan)
    ↓
ExplosionQueue.onAnimationComplete() → Execute explosion task
    ↓
Cell.setOrbs() → Distribusi ke neighbors
    ↓
Neighbors.addOrb() → (Jika critical) Queue lagi
    ↓
Process next animation (recursive)
```

## 🎨 Visual Effects

### Wave/Ripple Effect
- **3 Layer Ripples**: Setiap layer dengan delay 50ms
- **Scaling**: Radius 0 → 40
- **Opacity**: 0.8 → 0.0 (fade out)
- **Stroke Width**: 2 → 0.5 (menipis)
- **Blur**: GaussianBlur(5) untuk smoothness

### Orb Movement
- **Path**: Linear dari cell asal ke tujuan
- **Scale**: 1.0 → 1.3 → 1.0 (bounce effect)
- **Opacity**: 1.0 → 0.7 → 1.0 (pulse)
- **Interpolator**: EASE_OUT untuk natural movement

## ⚙️ Konfigurasi

### Durasi Animasi (dapat disesuaikan di `ExplosionAnimation.java`):
```java
private static final double WAVE_DURATION = 400.0;        // ms
private static final double ORB_MOVEMENT_DURATION = 300.0; // ms
private static final double RIPPLE_DELAY = 50.0;           // ms
```

### Performance Tips:
1. **Limit Concurrent Animations**: `AnimationManager` hanya memproses satu explosion pada satu waktu
2. **Cleanup**: Semua ripple circles dihapus setelah animasi selesai
3. **Mouse Transparent**: Animation layer tidak menghalangi user interaction

## 🐛 Troubleshooting

### Animasi tidak muncul:
1. Pastikan `AnimatedExplosionStrategy` sudah di-set untuk semua cells
2. Pastikan `GameController.setOnAnimationStart()` sudah di-set di `MainApp`
3. Cek apakah `ExplosionQueue` memiliki task (gunakan debug)

### Animasi terlalu cepat/lambat:
- Adjust durasi di `ExplosionAnimation.java` constants

### Race condition:
- `ExplosionQueue` sudah thread-safe dengan `ReentrantLock`
- `AnimationManager` track active animations untuk prevent duplicates

## 📝 Best Practices

1. **Sequential Processing**: Selalu proses explosions secara sequential untuk prevent race conditions
2. **Cleanup**: Selalu hapus visual elements setelah animasi selesai
3. **Bounds Calculation**: Gunakan `boundsInParent` untuk posisi yang akurat
4. **Callback Chain**: Pastikan callback chain lengkap untuk synchronization

## 🔮 Future Enhancements

1. **Particle Effects**: Tambah particle system untuk explosion
2. **Sound Effects**: Sync dengan animasi
3. **Configurable Speed**: User bisa adjust animasi speed
4. **Animation Presets**: Berbagai style animasi (fast, slow, dramatic)

