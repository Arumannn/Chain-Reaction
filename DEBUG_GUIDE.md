# Debug Guide untuk Animation System

## 🐛 Masalah yang Dilacak

Sistem debug ini dirancang untuk mendeteksi masalah ketika **2 cells meledak ke cell yang sama** dan posisi ball tidak sesuai.

## 📊 Fitur Debug

### 1. **Coordinate Tracking**
- Mencatat setiap perhitungan koordinat untuk setiap cell
- Melacak waktu perhitungan dan urutan perhitungan
- Mendeteksi multiple calculations dalam waktu singkat
- Menampilkan perbedaan koordinat jika ada

### 2. **Explosion Tracking**
- Mencatat setiap explosion yang terjadi
- Melacak target cells dari setiap explosion
- Mendeteksi multiple explosions ke cell yang sama dalam waktu singkat
- Menampilkan timing dan source dari setiap explosion

### 3. **Timing Analysis**
- Mencatat timing events penting
- Menghitung delay antara events
- Mendeteksi race conditions

### 4. **Problem Detection**
- Otomatis mendeteksi cells yang menerima multiple explosions
- Mendeteksi inconsistent coordinates
- Menampilkan summary untuk problematic cells

## 🔍 Cara Menggunakan

### Enable/Disable Debug

Di `AnimationDebugger.java`, ubah:
```java
private static final boolean DEBUG_ENABLED = true; // Set false untuk disable
```

### Output Debug

Debug output akan muncul di console dengan format:
```
[DEBUG] <message>
```

### Contoh Output

#### Multiple Explosions Detected:
```
[DEBUG] ⚠️ MULTIPLE EXPLOSIONS to Cell(3,4) within 50ms:
  Explosion 1: From Cell(2,4) at 1234567890
  Explosion 2: From Cell(4,4) at 1234567940
```

#### Inconsistent Coordinates:
```
[DEBUG] Multiple coordinate calculations for Cell(3,4) within 30ms:
  Previous: (150.50, 200.30) at count 5
  Current:  (152.10, 201.80) at count 6
  Difference: (1.60, 1.50)
```

#### Problematic Cells Report:
```
[DEBUG] === Problematic Cells Report ===
  Cell(3,4): Received 2 explosions
    ⚠️ INCONSISTENT COORDINATES: 2 unique positions found!
      - (150.50, 200.30)
      - (152.10, 201.80)
```

## 🔎 Apa yang Dicari

### 1. **Multiple Explosions ke Cell yang Sama**
- Cek apakah ada cell yang menerima 2+ explosions dalam waktu < 200ms
- Perhatikan timing dan source cells

### 2. **Inconsistent Coordinates**
- Cek apakah koordinat yang dihitung untuk cell yang sama berbeda
- Perbedaan > 1 pixel biasanya menandakan masalah

### 3. **Timing Issues**
- Cek delay antara `processAllPendingAnimations` dan `Platform.runLater-execution`
- Delay besar bisa menyebabkan koordinat tidak konsisten

### 4. **Coordinate Calculation Count**
- Cek berapa kali koordinat dihitung untuk cell yang sama
- Multiple calculations dalam waktu singkat = potential bug

## 📝 Interpretasi Hasil

### Normal Case:
- Setiap cell hanya menerima 1 explosion per batch
- Koordinat konsisten untuk cell yang sama
- Timing events berurutan dengan delay wajar

### Bug Indicators:
- ⚠️ Multiple explosions ke cell yang sama < 200ms
- ⚠️ Inconsistent coordinates (> 1 pixel difference)
- ⚠️ Coordinate calculations dalam waktu sangat singkat (< 10ms)
- ⚠️ Large delay antara Platform.runLater calls

## 🛠️ Troubleshooting

### Jika menemukan inconsistent coordinates:

1. **Cek timing**: Apakah koordinat dihitung pada waktu yang berbeda?
2. **Cek layout**: Apakah layout sudah selesai saat koordinat dihitung?
3. **Cek multiple calls**: Apakah `getCenterInContainer` dipanggil multiple times untuk cell yang sama?

### Jika menemukan multiple explosions:

1. **Cek batch processing**: Apakah `executeBatchCompletedTasks` bekerja dengan benar?
2. **Cek queue**: Apakah tasks di-queue dengan benar?
3. **Cek animation timing**: Apakah animasi selesai pada waktu yang berbeda?

## 📋 Checklist Debug

- [ ] Enable debug mode
- [ ] Reproduce bug (2 cells meledak ke cell yang sama)
- [ ] Cek console output untuk warnings
- [ ] Identifikasi problematic cells
- [ ] Analisis coordinate differences
- [ ] Cek timing issues
- [ ] Gunakan `printCellSummary()` untuk cell yang bermasalah

## 🎯 Next Steps Setelah Debug

Setelah mengidentifikasi masalah:
1. Catat cell coordinates yang inconsistent
2. Catat timing dari multiple explosions
3. Analisis root cause
4. Implement fix berdasarkan findings
5. Test lagi dengan debug enabled untuk verify fix

