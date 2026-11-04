# Su Takip Android Uygulaması

Sade, modern, tamamen çevrimdışı (offline) ve kullanıcı odaklı bir Su Takip Android uygulamasıdır.

## 📱 Özellikler
- **Tamamen Çevrimdışı:** İnternet izni dahi gerektirmez, hesap/üyelik yoktur.
- **Kalıcı Yerel Depolama:** `SharedPreferences` kullanılarak veriler cihaz hafızasında güvenle saklanır, uygulama kapatılıp açıldığında kaybolmaz.
- **Günlük Su Takibi:** Bugün içilen toplam miktar büyük punto ile (örneğin `1.75 / 2.50 L`), yüzde tamamlanma oranı ve renkli ilerleme çubuğu ile gösterilir.
- **Hedef Tebrik Mesajı:** Günlük hedefe ulaşıldığında yeşil tebrik kartı belirir.
- **Hızlı Erişim Butonları:** Son kullanılan 3 miktar hafızada tutulur ve tek tıkla eklenir. Tekrarlanan miktarlar listeyi doldurmaz.
- **Özel Miktar Ekleme:** İstediğiniz miktarı (ör. 0.33 L) girebileceğiniz dialog penceresi.
- **Günlük Hedef Belirleme:** Üst kısımdan hedef istenildiği an değiştirilebilir (varsayılan 2.50 L).
- **Manuel Günü Bitir Sistemi:** Gün otomatik veya gece yarısı sıfırlanmaz. Kullanıcı "Günü Bitir" butonuna onay verene kadar mevcut gün devam eder. Bitirildiğinde gün istatistiklere kaydedilir ve yeni gün 0 L ile başlar.
- **Haftalık & Aylık İstatistikler:**
  - Son 7 ve son 30 tamamlanmış günün verileri.
  - Toplam ve günlük ortalama tüketim değerleri.
  - Özel sütun grafik (SimpleBarChartView).
  - Gün bazlı detaylı döküm.

## 📦 APK Dosyası
Doğrudan kurulabilir APK dosyası proje kök dizinine yerleştirilmiştir:
- **`SuTakip.apk`** (veya `app/build/outputs/apk/debug/app-debug.apk`)

