# HHPanda CloudStream Extension

Plugin CloudStream cho trang [HHPanda](https://hhpanda.st/) - Xem Hoạt Hình Trung Quốc Vietsub 4K.

## Tính năng

- 🎬 Xem phim hoạt hình Trung Quốc Vietsub/Thuyết Minh
- 🔍 Tìm kiếm phim
- 📺 Trang chủ với danh mục: Mới Cập Nhật, Hoàn Thành, Top Xem Nhiều, Tu Tiên, Kiếm Hiệp, Huyền Huyễn...
- 🎥 Nhiều server chất lượng: 4K V1, 4K V2, 1080P V1, 1080P V2
- 🔊 Hỗ trợ Vietsub và Thuyết Minh

## Cài đặt

### Cách 1: Thêm repository vào CloudStream

1. Mở CloudStream app
2. Vào **Settings** → **Extensions** → **Add repository**
3. Nhập URL repository JSON (xem phần Build bên dưới)

### Cách 2: Build từ source

1. Clone repository này
2. Chạy: `./gradlew HhpandaProvider:make`
3. File `.cs3` sẽ được tạo trong `HhpandaProvider/build/`
4. Copy file `.cs3` vào điện thoại và cài đặt

## Build

```bash
# Linux/Mac
./gradlew HhpandaProvider:make

# Windows
.\gradlew.bat HhpandaProvider:make
```

## Cấu trúc project

```
HhpandaProvider/
├── build.gradle.kts          # Cấu hình plugin
├── src/main/
│   ├── AndroidManifest.xml
│   └── kotlin/com/hhpanda/
│       └── HhpandaProvider.kt  # Code chính của provider
├── .github/workflows/         # GitHub Actions auto build
└── README.md
```

## Cách hoạt động

Plugin sử dụng các endpoint sau của HHPanda:

- **Tìm kiếm**: `GET https://hhpanda.st/?s={query}`
- **Trang chủ**: `GET https://hhpanda.st/moi-cap-nhat`
- **Chi tiết phim**: `GET https://hhpanda.st/{slug}`
- **Video player**: `GET https://hhpanda.st/player/player.php?action=dox_ajax_player&post_id={id}&chapter_st={ep}&type={server}&sv={version}`

Video được extract từ `streamfree.vip` thông qua `loadExtractor`.

## License

Public domain. Sử dụng tùy ý.
