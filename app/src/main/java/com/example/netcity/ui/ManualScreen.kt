package com.example.netcity.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*

private enum class ManualCategory(val label: String) {
    ALL("Semua"),
    BASICS("Memulai"),
    ROLES("Peran & Akses"),
    CITY("Kota 2.5D"),
    HARDWARE("Scan & Diorama"),
    STUDIO("Karakter"),
    ADMIN("Kelas & Nilai"),
    FAQ("FAQ")
}

private data class ManualStep(
    val title: String,
    val detail: String
)

private data class ManualChapter(
    val id: String,
    val number: String,
    val title: String,
    val summary: String,
    val category: ManualCategory,
    val icon: ImageVector,
    val accent: Color,
    val steps: List<ManualStep>,
    val proTip: String,
    val targetRoute: String? = null,
    val targetActionLabel: String? = null
)

private val manualChapters = listOf(
    ManualChapter(
        id = "ch_intro",
        number = "BAB 01",
        title = "Pengenalan NET4CITY & 3 Pilar Utama",
        summary = "Memahami konsep integrasi Dunia Virtual, Diorama Fisik, dan Identitas Digital Barcode/QR yang bersifat universal.",
        category = ManualCategory.BASICS,
        icon = Icons.Rounded.Hub,
        accent = NetCyanPrimary,
        steps = listOf(
            ManualStep(
                "1. Virtual World (Simulasi Kota 2.5D)",
                "Area permainan simulasi pembangunan kota berbasis peta isometrik 2.5D. Peserta berperan mulai dari teknisi jaringan lapangan, Network Engineer, hingga arsitek Mini ISP."
            ),
            ManualStep(
                "2. Physical World (Diorama Modular Fisik)",
                "Maket/diorama modular fisik yang merepresentasikan rumah, sekolah, lab komputer, perkantoran, ruang server, data center, POP ISP, serta jalur fiber optik ODC–ODP."
            ),
            ManualStep(
                "3. Digital Identity (Barcode / QR Code Permanen)",
                "Setiap karakter dan objek diorama memiliki kode QR/Barcode permanen. Saat dipindai menggunakan USB Barcode Scanner (keyboard wedge) pada Papan Interaktif Digital (PID) atau tablet, data objek langsung tersinkron di layar tanpa perlu ESP32 atau modul RFID."
            ),
            ManualStep(
                "4. Dukungan Penggunaan Universal Lintas Kelas",
                "Satu PID dan satu set diorama modular dapat digunakan secara bergantian oleh berbagai kelas atau kelompok belajar. Jangan mencetak ulang barcode saat ganti kelas—cukup pilih kelas aktif di Beranda agar setiap riwayat pemindaian dan progres tercatat terpisah per sesi kelas."
            )
        ),
        proTip = "Gunakan tampilan layar penuh pada Papan Interaktif Digital (PID) atau tablet berorientasi lanskap agar panel navigasi samping (Navigation Rail) muncul otomatis dan seluruh modul dapat dijangkau dengan satu sentuhan.",
        targetRoute = ROUTE_DASHBOARD,
        targetActionLabel = "Buka Beranda"
    ),
    ManualChapter(
        id = "ch_roles",
        number = "BAB 02",
        title = "Login Akun Google & Hak Akses 3 Peran",
        summary = "Panduan masuk aman menggunakan Akun Google serta pembagian kewenangan antara Administrator, Guru, dan Siswa.",
        category = ManualCategory.ROLES,
        icon = Icons.Rounded.AdminPanelSettings,
        accent = NetPurpleAccent,
        steps = listOf(
            ManualStep(
                "1. Masuk dengan Akun Google",
                "Pada halaman awal, tekan tombol 'Masuk dengan Google' dan pilih akun Google yang aktif di perangkat. Aplikasi akan otomatis menyinkronkan profil, data kelas, kota 2.5D, karakter, barcode, dan penilaian melalui Cloud Firestore."
            ),
            ManualStep(
                "2. Peran Administrator (Akses Penuh)",
                "Memiliki kontrol menyeluruh: membuat/mengubah/menghapus data kelas, guru, dan siswa; mengimpor daftar siswa dari CSV; mengelola database admin (/admins), barcode, dan modul diorama; mereset data demo; serta meninjau laporan seluruh kelas."
            ),
            ManualStep(
                "3. Peran Guru / Pengajar",
                "Mengelola sesi praktikum kelas: memilih kelas aktif, membuka atau mengunci batas level game (Level 1–10), mengatur misi sesi praktikum, memeriksa dan memverifikasi hasil rakitan diorama fisik, mengisi rubrik nilai 4 aspek, serta mengunduh/membagikan laporan CSV."
            ),
            ManualStep(
                "4. Peran Siswa / Peserta",
                "Mengkustomisasi karakter teknisi dan mencetak/menampilkan QR ID pribadi, memainkan simulasi pembangunan kota 2.5D sesuai level yang dibuka guru, memindai objek diorama di PID, mengumpulkan XP & koin NC$, serta memantau progres dan nilai (nilai guru bersifat baca-saja)."
            ),
            ManualStep(
                "5. Beralih Pratinjau Peran di Beranda",
                "Pada kartu utama Beranda tersedia chip Admin, Guru, dan Siswa untuk memudahkan demonstrasi tampilan antar-peran saat pembelajaran di depan kelas."
            )
        ),
        proTip = "Pastikan koneksi internet aktif saat pertama kali login agar inisialisasi ruang kerja dan sinkronisasi data Cloud Firestore berjalan mulus.",
        targetRoute = ROUTE_MANAGEMENT,
        targetActionLabel = "Kelola Kelas & Pengguna"
    ),
    ManualChapter(
        id = "ch_city_engine",
        number = "BAB 03",
        title = "Simulasi Kota 2.5D, Topologi & 10 Level STEM",
        summary = "Cara menavigasi peta isometrik, membangun 14 jenis bangunan/perangkat, menghubungkan kabel, dan menyelesaikan simulasi gangguan.",
        category = ManualCategory.CITY,
        icon = Icons.Rounded.LocationCity,
        accent = NetCyanPrimary,
        steps = listOf(
            ManualStep(
                "1. Navigasi Peta Isometrik 2.5D",
                "Geser jari pada kanvas peta untuk berpindah area. Gunakan tombol Zoom In (+), Zoom Out (-), dan Reset Kamera untuk menyesuaikan sudut pandang pada layar PID maupun layar ponsel."
            ),
            ManualStep(
                "2. Memilih & Menempatkan Objek (14 Katalog)",
                "Pilih objek dari panel Inventory di bagian bawah/samping (Rumah, Sekolah, Kantor, Lab Komputer, Komputer, Switch, Router, Access Point, Server, Data Center, POP ISP, ODC, ODP, atau jalur Fiber/UTP). Ketuk petak grid peta untuk menempatkannya selama saldo NC$ mencukupi."
            ),
            ManualStep(
                "3. Menghubungkan Infrastruktur Jaringan",
                "Aktifkan mode Sambung / Link atau pilih perangkat sumber dan tujuan untuk menarik jalur transmisi (UTP, Fiber Optik, atau Wi-Fi). Koneksi yang valid akan menyalakan indikator sinyal hijau dan meningkatkan Kesehatan Jaringan (%)."
            ),
            ManualStep(
                "4. Menyelesaikan Misi 10 Level Progresif",
                "Buka panel Misi Level untuk melihat daftar syarat bangunan dan perangkat tiap level—mulai dari Level 1 (LAN Dasar), Level 2 (Switch Lab), Level 3 (Router & Gateway), Level 4 (Wi-Fi Sekolah), Level 5 (VLAN & Subnetting), Level 6 (Server DHCP/DNS), Level 7 (Fiber ODC/ODP), Level 8 (Data Center), Level 9 (Mini ISP & POP), hingga Level 10 (Kota Pintar). Tekan 'Selesaikan Level' saat syarat terpenuhi untuk memperoleh hadiah XP dan koin NC$."
            ),
            ManualStep(
                "5. Simulasi Gangguan & Troubleshooting",
                "Tekan tombol 'Simulasi Gangguan' untuk memunculkan skenario insiden jaringan nyata (seperti putusnya jalur fiber, konflik IP, AP penuh, atau layanan DHCP terhenti). Analisis pesan insiden lalu tekan 'Perbaiki Gangguan' untuk memulihkan Kesehatan Jaringan menjadi 100% dan meraih bonus XP."
            )
        ),
        proTip = "Jika level berikutnya masih berstatus terkunci (ikon gembok), minta Guru atau Admin membuka batas level kelas tersebut melalui menu Kelas/User.",
        targetRoute = ROUTE_CITY,
        targetActionLabel = "Buka Kota 2.5D"
    ),
    ManualChapter(
        id = "ch_scanner",
        number = "BAB 04",
        title = "Scan Mode PID & USB Barcode/QR Scanner",
        summary = "Prosedur lengkap menghubungkan USB Barcode/QR Scanner (keyboard wedge) dan memindai objek fisik maupun kartu karakter.",
        category = ManualCategory.HARDWARE,
        icon = Icons.Rounded.QrCodeScanner,
        accent = NetEmerald,
        steps = listOf(
            ManualStep(
                "1. Persiapan Perangkat USB Scanner",
                "Colokkan USB Barcode/QR Scanner ke port USB pada Papan Interaktif Digital (PID) atau melalui adaptor USB OTG pada tablet Android. Scanner akan otomatis dikenali sistem sebagai perangkat keyboard USB (HID Keyboard Wedge) tanpa perlu instalasi driver."
            ),
            ManualStep(
                "2. Memindai Objek di Layar Scan PID",
                "Masuk ke menu 'Scan PID', pastikan kolom input scanner dalam keadaan fokus, lalu arahkan sinar scanner ke kode Barcode/QR pada perangkat diorama atau kartu karakter siswa. Scanner akan mengetikkan ID secara instan dan otomatis memicu pencarian database."
            ),
            ManualStep(
                "3. Mode Input Manual & Registry Cepat",
                "Apabila scanner fisik sedang tidak terpasang, Anda tetap dapat mengetik kode objek secara manual (contoh: DEV-RTR-001, DIO-MOD-01, CHR-001) lalu menekan tombol 'Proses Scan', atau cukup mengetuk langsung salah satu item pada daftar Registry Barcode."
            ),
            ManualStep(
                "4. Mendaftarkan Kode Barcode Baru",
                "Admin dan Guru dapat menambah identitas objek fisik baru melalui formulir Tambah Barcode (isi Kode Unik, Nama Objek, Kategori Perangkat/Diorama/Karakter, dan Deskripsi). Setiap riwayat scan akan mencatat waktu, kelas aktif, dan nama objek secara otomatis."
            )
        ),
        proTip = "Pastikan pengaturan USB Scanner Anda mengaktifkan akhiran 'Enter / Carriage Return' (pengaturan bawaan pabrik) agar hasil pindaian langsung dieksekusi tanpa perlu menyentuh layar lagi.",
        targetRoute = ROUTE_SCAN,
        targetActionLabel = "Buka Scan Mode PID"
    ),
    ManualChapter(
        id = "ch_diorama",
        number = "BAB 05",
        title = "Diorama Manager & Verifikasi Praktik Fisik",
        summary = "Mengelola 10 modul diorama fisik, memperbarui status perakitan, dan melakukan verifikasi praktikum guru.",
        category = ManualCategory.HARDWARE,
        icon = Icons.Rounded.ViewInAr,
        accent = NetAmber,
        steps = listOf(
            ManualStep(
                "1. Mengenal 10 Modul Diorama Fisik",
                "Menu Diorama menampilkan 10 zona diorama modular (Rumah, Sekolah, Lab Komputer, Perkantoran, Rumah Sakit, Pusat Niaga, Ruang Server, Data Center, POP ISP, dan Jalur Fiber ODC–ODP) lengkap dengan kode barcode permanen masing-masing."
            ),
            ManualStep(
                "2. Pembaruan Status Perakitan oleh Kelompok",
                "Saat kelompok praktikum selesai merakit kabel, konektor, atau tata letak perangkat pada diorama fisik, ubah status modul dari 'Belum Dirakit' menjadi 'Dalam Perakitan' atau 'Siap Verifikasi'."
            ),
            ManualStep(
                "3. Verifikasi & Penilaian Guru",
                "Guru memeriksa kerapian, kesesuaian topologi fisik dengan peta kota 2.5D, lalu menekan tombol 'Verifikasi Modul'. Status modul akan berubah menjadi hijau ('Terverifikasi') dan tercatat untuk kelas yang sedang aktif."
            ),
            ManualStep(
                "4. Pergantian Sesi Antar Kelas",
                "Saat jam pelajaran berganti ke kelas lain, cukup ganti pilihan kelas aktif pada Beranda. Status dan riwayat penggunaan diorama untuk kelas baru akan tercatat tanpa menimpa arsip kelas sebelumnya."
            ),
            ManualStep(
                "5. Cetak Pola Diorama Kertas 3D + Barcode (A4)",
                "Tekan tombol 'Print Pola Kertas 3D + Barcode' pada masing-masing modul atau 'Print Semua Pola Diorama' di bagian atas untuk melihat pratinjau jaring-jaring bangunan 3D lengkap dengan QR Code, Barcode 1D Code-128, garis gunting, garis lipat, 7 tab lem, dan kartu label meja. Cetak pada kertas HVS tebal/karton (160–210 gsm) atau simpan sebagai PDF."
            )
        ),
        proTip = "Gabungkan pemindaian barcode modul diorama dengan pemeriksaan langsung di meja praktik agar siswa memahami hubungan nyata antara perangkat fisik dan simulasi topologi virtual.",
        targetRoute = ROUTE_DIORAMA,
        targetActionLabel = "Buka Diorama Manager"
    ),
    ManualChapter(
        id = "ch_character",
        number = "BAB 06",
        title = "Character Studio & QR Code Identitas Siswa",
        summary = "Membuat avatar teknisi virtual yang personal beserta Character ID dan QR Code untuk absensi & misi PID.",
        category = ManualCategory.STUDIO,
        icon = Icons.Rounded.Face,
        accent = NetPurpleAccent,
        steps = listOf(
            ManualStep(
                "1. Membuat Karakter Baru",
                "Buka menu 'Karakter', masukkan Nama Karakter dan pilih Profesi/Peran (Teknisi Jaringan, Siswa, Guru, Warga Kota, atau Petugas ISP)."
            ),
            ManualStep(
                "2. Kustomisasi Tampilan Modular",
                "Sesuaikan gaya rambut, seragam/rompi teknisi, aksesori pendukung (helm safety, kacamata, headset, alat crimping), dan palet warna aksen karakter. Pratinjau karakter diperbarui secara langsung."
            ),
            ManualStep(
                "3. Penyimpanan & Pembuatan QR Code Otomatis",
                "Tekan 'Simpan Karakter'. Sistem otomatis menerbitkan Character ID unik (mis. CHR-XXX) beserta gambar QR Code beresolusi tajam yang dapat dipindai pada Scan Mode PID."
            ),
            ManualStep(
                "4. Pengelolaan Daftar Karakter",
                "Ketuk kartu karakter yang sudah tersimpan untuk melihat detail QR Code layar penuh, memilih karakter aktif untuk sesi praktikum, atau memperbarui profil karakter."
            )
        ),
        proTip = "Tampilkan QR Code karakter siswa di ponsel/tablet masing-masing lalu pindai ke USB Scanner PID sebagai cara cepat mencatat keaktifan siswa saat maju ke papan interaktif.",
        targetRoute = ROUTE_CHARACTER,
        targetActionLabel = "Buka Character Studio"
    ),
    ManualChapter(
        id = "ch_management",
        number = "BAB 07",
        title = "Manajemen Kelas, Pengguna, Impor CSV & Admin",
        summary = "Mengelola kelas secara universal, data guru & siswa, impor massal CSV, serta sinkronisasi database Admin.",
        category = ManualCategory.ADMIN,
        icon = Icons.Rounded.ManageAccounts,
        accent = NetUtpBlue,
        steps = listOf(
            ManualStep(
                "1. Tab Kelas — Fleksibel untuk Semua Kelas",
                "Tambahkan kelas baru dengan kode/nama bebas sesuai kebutuhan institusi Anda, tentukan nama guru pengampu, atur judul misi praktikum aktif, serta buka/kunci batas Level Maksimal (1–10) per kelas."
            ),
            ManualStep(
                "2. Tab Siswa & Impor CSV Massal",
                "Tambahkan siswa satu per satu atau gunakan tombol 'Impor CSV'. Tempelkan daftar siswa dengan format satu baris per siswa: Nama Lengkap,NIS,KodeKelas (contoh: Budi Santoso,2026001,KELAS-A), lalu klik Impor."
            ),
            ManualStep(
                "3. Tab Guru",
                "Kelola daftar guru/instruktur pengampu, NIP/ID pengajar, dan bidang keahlian agar setiap kelas memiliki penanggung jawab yang jelas."
            ),
            ManualStep(
                "4. Tab Admin (Database /admins)",
                "Menampilkan daftar akun Administrator terverifikasi yang terdaftar di Cloud Firestore (/admins). Akun admin utama yang masuk dengan email Google terverifikasi dapat menyinkronkan data admin langsung dari tombol sinkronisasi."
            )
        ),
        proTip = "Gunakan fitur 'Reset Demo' di bagian bawah Beranda (mode Admin) kapan saja Anda ingin memulihkan dataset contoh untuk keperluan pelatihan atau uji coba.",
        targetRoute = ROUTE_MANAGEMENT,
        targetActionLabel = "Buka Kelas & Pengguna"
    ),
    ManualChapter(
        id = "ch_assessment",
        number = "BAB 08",
        title = "Penilaian Rubrik, Ekspor Laporan & FAQ",
        summary = "Cara mengisi rubrik penilaian 4 kategori, mengunci nilai final, mengekspor laporan CSV, dan solusi kendala umum.",
        category = ManualCategory.FAQ,
        icon = Icons.Rounded.Assessment,
        accent = NetCoralError,
        steps = listOf(
            ManualStep(
                "1. Rubrik Penilaian 4 Kategori (Skala 0–100)",
                "Buka menu 'Penilaian', pilih siswa dan kelas, lalu atur nilai pada 4 aspek: Topologi & Simulasi Game, Praktik Diorama Fisik, Troubleshooting Gangguan, dan Kolaborasi Tim. Skor akhir dan predikat (A/B/C/D) dihitung otomatis."
            ),
            ManualStep(
                "2. Catatan Evaluasi & Finalisasi Nilai",
                "Tuliskan umpan balik konstruktif untuk siswa. Pilih status 'DRAFT' jika masih bisa direvisi, atau 'FINALIZED' untuk mengunci nilai secara permanen agar aman dari perubahan yang tidak disengaja."
            ),
            ManualStep(
                "3. Ekspor & Bagikan Laporan CSV",
                "Tekan tombol 'Ekspor CSV' untuk menghasilkan berkas rekap nilai seluruh siswa yang siap dibuka di Microsoft Excel, Google Sheets, atau dibagikan melalui menu berbagi Android."
            ),
            ManualStep(
                "4. FAQ — Kendala Umum & Solusi Cepat",
                "• Scanner USB tidak merespons: Pastikan kolom input Scan PID aktif dan kabel OTG terpasang kencang.\n• Level game terkunci: Minta Guru/Admin menaikkan 'Batas Level' pada tab Kelas.\n• Data belum muncul: Periksa koneksi internet dan pastikan Anda masuk dengan akun Google yang sama.\n• Nilai tidak bisa diubah: Status penilaian sudah FINALIZED; hanya Administrator terverifikasi yang dapat mengubahnya."
            )
        ),
        proTip = "Lakukan finalisasi nilai di akhir setiap bab praktikum dan ekspor arsip CSV secara berkala sebagai cadangan portofolio nilai kelas.",
        targetRoute = ROUTE_ASSESSMENT,
        targetActionLabel = "Buka Penilaian & Laporan"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualScreen(
    onNavigate: ((String) -> Unit)? = null,
    onBack: (() -> Unit)? = null
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ManualCategory.ALL) }
    var expandedChapterIds by remember { mutableStateOf(setOf("ch_intro", "ch_city_engine")) }

    val filteredChapters by remember(searchQuery, selectedCategory) {
        derivedStateOf {
            val q = searchQuery.trim().lowercase()
            manualChapters.filter { chapter ->
                val matchesCategory = selectedCategory == ManualCategory.ALL || chapter.category == selectedCategory
                val matchesQuery = q.isEmpty() ||
                    chapter.title.lowercase().contains(q) ||
                    chapter.summary.lowercase().contains(q) ||
                    chapter.proTip.lowercase().contains(q) ||
                    chapter.steps.any {
                        it.title.lowercase().contains(q) || it.detail.lowercase().contains(q)
                    }
                matchesCategory && matchesQuery
            }
        }
    }

    val allExpanded = filteredChapters.isNotEmpty() && filteredChapters.all { it.id in expandedChapterIds }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .let { if (onBack != null) it.windowInsetsPadding(WindowInsets.safeDrawing) else it }
    ) {
        if (onBack != null) {
            TopAppBar(
                title = {
                    Text(
                        text = "Panduan NET4CITY",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("manual_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                }
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Hero Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(NetSkyCheerStart, NetSkyCheerMid, NetSkyCheerEnd)
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color.White.copy(alpha = 0.22f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.MenuBook,
                                        contentDescription = "Buku Panduan",
                                        tint = NetAmberBright,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Column {
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = NetAmberBright.copy(alpha = 0.28f)
                                    ) {
                                        Text(
                                            text = "GAME EDUKASI PEMBELAJARAN JARINGAN",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = NetAmberBright,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Cara Penggunaan NET4CITY",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Text(
                                text = "Panduan operasional lengkap langkah demi langkah untuk Admin, Guru, dan Siswa: mulai dari login, simulasi kota 2.5D, USB Barcode Scanner PID, Diorama fisik, hingga penilaian & laporan.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.95f)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ManualMiniStat("8 Bab", "Materi Lengkap", Modifier.weight(1f))
                                ManualMiniStat("10 Level", "Kurikulum STEM", Modifier.weight(1f))
                                ManualMiniStat("3 Peran", "Admin•Guru•Siswa", Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // 5-Step Quick Workflow Overview
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Route,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Ringkasan Cepat: Alur Praktikum 5 Langkah",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        val quickFlow = listOf(
                            Triple("1", "Masuk & Pilih Kelas", "Login Google lalu pilih kelas/sesi aktif di Beranda."),
                            Triple("2", "Buat Karakter & QR", "Rancang avatar teknisi di Character Studio untuk QR ID."),
                            Triple("3", "Bangun Kota 2.5D", "Pasang bangunan, perangkat & sambungkan jaringan tiap level."),
                            Triple("4", "Pindai Diorama PID", "Scan barcode modul diorama fisik dengan USB Scanner."),
                            Triple("5", "Verifikasi & Nilai", "Guru verifikasi modul, isi rubrik 4 aspek & ekspor CSV.")
                        )

                        quickFlow.forEach { (num, title, desc) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(NetCyanPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = num,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = desc,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Search & Category Filter Bar
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("manual_search_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        placeholder = { Text("Cari topik: USB Scanner, VLAN, CSV, Diorama, Rubrik...") },
                        leadingIcon = {
                            Icon(Icons.Rounded.Search, contentDescription = "Cari panduan")
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Rounded.Close, contentDescription = "Hapus pencarian")
                                }
                            }
                        }
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ManualCategory.entries.forEach { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat.label) },
                                modifier = Modifier.testTag("manual_filter_${cat.name.lowercase()}")
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Menampilkan ${filteredChapters.size} dari ${manualChapters.size} bab panduan",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(
                            onClick = {
                                expandedChapterIds = if (allExpanded) {
                                    emptySet()
                                } else {
                                    filteredChapters.map { it.id }.toSet()
                                }
                            },
                            modifier = Modifier.testTag("manual_toggle_all_button")
                        ) {
                            Icon(
                                imageVector = if (allExpanded) Icons.Rounded.UnfoldLess else Icons.Rounded.UnfoldMore,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (allExpanded) "Tutup Semua" else "Buka Semua")
                        }
                    }
                }
            }

            if (filteredChapters.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.HelpOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "Topik tidak ditemukan",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Coba kata kunci lain seperti 'Scanner', 'Kota', 'Karakter', 'CSV', atau pilih kategori 'Semua'.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredChapters, key = { it.id }) { chapter ->
                    val isExpanded = chapter.id in expandedChapterIds
                    ManualChapterCard(
                        chapter = chapter,
                        isExpanded = isExpanded,
                        onToggle = {
                            expandedChapterIds = if (isExpanded) {
                                expandedChapterIds - chapter.id
                            } else {
                                expandedChapterIds + chapter.id
                            }
                        },
                        onJumpToFeature = if (onNavigate != null && chapter.targetRoute != null) {
                            { onNavigate(chapter.targetRoute) }
                        } else null
                    )
                }
            }
        }
    }
}

@Composable
private fun ManualMiniStat(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.2f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                color = NetAmberBright,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ManualChapterCard(
    chapter: ManualChapter,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onJumpToFeature: (() -> Unit)?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("manual_chapter_${chapter.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            width = if (isExpanded) 1.5.dp else 1.dp,
            color = if (isExpanded) chapter.accent.copy(alpha = 0.65f) else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(chapter.accent.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = chapter.icon,
                        contentDescription = chapter.title,
                        tint = chapter.accent,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = chapter.number,
                        style = MaterialTheme.typography.labelSmall,
                        color = chapter.accent,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = chapter.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = chapter.summary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = if (isExpanded) "Tutup bab" else "Buka bab",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    chapter.steps.forEach { step ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = step.title,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = step.detail,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Pro Tip Box
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = chapter.accent.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, chapter.accent.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Lightbulb,
                                contentDescription = "Tips Praktik",
                                tint = chapter.accent,
                                modifier = Modifier.size(20.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Tips Praktik",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = chapter.accent
                                )
                                Text(
                                    text = chapter.proTip,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    if (onJumpToFeature != null && chapter.targetActionLabel != null) {
                        FilledTonalButton(
                            onClick = onJumpToFeature,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("manual_jump_${chapter.id}"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(
                                imageVector = chapter.icon,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = chapter.targetActionLabel,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
