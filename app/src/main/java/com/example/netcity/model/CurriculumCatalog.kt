package com.example.netcity.model

object CurriculumCatalog {

    val professions = listOf(
        "Computer Technician",
        "Network Technician",
        "Server Administrator",
        "Fiber Optic Technician",
        "Network Engineer",
        "ISP Manager"
    )

    val faceShapes = listOf("Bulat", "Oval", "Persegi", "Tegas")
    val hairStyles = listOf("Pendek Rapi", "Spiky Tech", "Bob Modern", "Berhijab Rapi", "Undercut")
    val hairColors = listOf("Hitam", "Cokelat Tua", "Biru Cyber", "Abu Perak")
    val outfitStyles = listOf(
        "Seragam Teknisi Biru",
        "Seragam Siswa SMK TKJ",
        "Jaket ISP Lapangan",
        "Rompi Safety Fiber",
        "Kemeja Server Admin"
    )
    val pantsColors = listOf("Navy", "Abu Gelap", "Khaki", "Hitam")
    val shoeStyles = listOf("Safety Boots", "Sneakers Lab", "Sepatu Pantofel")
    val accessories = listOf("Tanpa Aksesori", "Kacamata Anti-Radiasi", "Helm Safety Kuning", "Headset NOC", "ID Card Teknisi")
    val workTools = listOf(
        "Tang Crimping RJ45",
        "LAN Cable Tester",
        "Optical Power Meter",
        "Fusion Splicer Mini",
        "Laptop Konfigurasi"
    )

    val buildableCatalog: List<BuildableSpec> = listOf(
        BuildableSpec("RUMAH", "Rumah Warga", "Bangunan", 200, 1, "Pelanggan residensial yang membutuhkan koneksi internet FTTH/Wi-Fi.", "1x ONT/Dropcore, Daya 220V", "Jalur Drop Cable dari ODP", 1, "NC-HOME"),
        BuildableSpec("SEKOLAH", "Gedung Sekolah", "Bangunan", 400, 4, "Pusat pembelajaran siswa SMK yang membutuhkan LAN dan Wi-Fi.", "Ruang kelas, panel listrik, rak dinding", "Switch Distribusi & Access Point", 1, "NC-SCH"),
        BuildableSpec("LAB", "Lab Komputer", "Bangunan", 450, 8, "Ruang praktikum perakitan komputer dan jaringan siswa TKJ.", "Meja praktikum, UPS, AC 2PK", "PC Client, Switch 24-Port, Kabel UTP", 1, "NC-LAB"),
        BuildableSpec("KANTOR", "Gedung Kantor", "Bangunan", 500, 6, "Perkantoran kota dengan kebutuhan VLAN terpisah per divisi.", "Rackmount 12U, Switch Manageable", "Router, VLAN, Firewall", 6, "NC-OFC"),
        BuildableSpec("RS", "Rumah Sakit", "Bangunan", 650, 8, "Fasilitas kritis dengan prioritas bandwidth tinggi dan redundansi.", "Dual-ISP Failover, Server Rekam Medis", "Topologi Mesh/Hybrid & Backup", 8, "NC-HSP"),
        BuildableSpec("PC", "Komputer Client", "Perangkat", 150, 1, "Workstation perakitan untuk pengguna akhir di lab dan kantor.", "CPU Quad-Core, RAM 16GB, SSD 512GB, NIC 1Gbps", "Motherboard, PSU, Kabel UTP", 1, "NC-PC"),
        BuildableSpec("SWITCH", "Switch 24-Port", "Perangkat", 250, 24, "Menghubungkan banyak perangkat dalam satu jaringan LAN (Layer 2).", "24x Gigabit RJ45, 2x SFP, VLAN 802.1Q", "Power AC, Patch Cord UTP Cat6", 2, "NC-SWT"),
        BuildableSpec("AP", "Access Point Wi-Fi", "Perangkat", 220, 2, "Memancarkan sinyal nirkabel WLAN (SSID) untuk area gedung.", "Dual-Band 2.4/5GHz Wi-Fi 6, PoE 802.3af", "Kabel UTP PoE dari Switch", 4, "NC-AP"),
        BuildableSpec("SERVER", "Server Sekolah", "Perangkat", 600, 4, "Menyediakan layanan DHCP, DNS, Web Server, File, dan Database.", "Xeon 8-Core, 64GB ECC RAM, RAID-10 NVMe", "Ruang Server ber-AC, UPS, IP Statis", 5, "NC-SRV"),
        BuildableSpec("ROUTER", "Router Core", "Perangkat", 480, 8, "Menghubungkan jaringan berbeda, NAT, gateway, dan routing antar wilayah.", "8x Gigabit + 2x SFP+, Firewall, OSPF/Static", "Konfigurasi IP Subnet & Gateway", 7, "NC-RTR"),
        BuildableSpec("DATACENTER", "Data Center", "Infrastruktur", 1100, 32, "Pusat pemrosesan data dan firewall pertahanan siber skala kota.", "Redundant Power, Core Switch, IDS/IPS Firewall", "Backbone Fiber Optik & Monitoring", 9, "NC-DC"),
        BuildableSpec("POP", "POP Mini ISP", "Infrastruktur", 1200, 16, "Point of Presence ISP tempat OLT dan distribusi backbone fiber optik.", "OLT GPON 8-Port, Core Router BGP, Rectifier", "Kabel Backbone Fiber & ODC", 10, "NC-POP"),
        BuildableSpec("ODC", "Kabinet ODC", "Infrastruktur", 550, 12, "Optical Distribution Cabinet pembagi jalur fiber feeder ke distribusi.", "Kapasitas 144 Core, Passive Splitter 1:4", "Kabel Feeder dari POP ISP", 10, "NC-ODC"),
        BuildableSpec("ODP", "Tiang ODP", "Infrastruktur", 320, 8, "Optical Distribution Point titik terminasi terakhir sebelum rumah pelanggan.", "Splitter 1:8, Adapter SC/UPC, Box Outdoor IP65", "Kabel Distribusi dari ODC & Dropcore", 10, "NC-ODP")
    )

    val levels: List<LearningLevelSpec> = listOf(
        LearningLevelSpec(
            number = 1,
            title = "COMPUTER TOWN",
            subtitle = "Perakitan Perangkat Keras & Workstation Lab",
            story = "Wali Kota NET4CITY baru saja meresmikan SMK TKJ pertama! Namun Laboratorium Komputer masih kosong dan belum memiliki unit PC yang siap dipakai siswa.",
            learningObjectives = listOf(
                "Mengidentifikasi fungsi Motherboard, CPU, RAM, Storage, PSU, NIC, dan Peripheral.",
                "Merancang spesifikasi komputer laboratorium sesuai anggaran virtual."
            ),
            topics = listOf("Motherboard", "CPU", "RAM", "Storage", "PSU", "NIC", "Peripheral"),
            mainMission = "Merakit dan menempatkan minimal 2 Komputer Client di dekat Laboratorium Sekolah.",
            challenge = "Pastikan daya PSU mencukupi dan setiap PC memiliki NIC Ethernet siap jaringan.",
            virtualPractice = "Tempatkan Gedung Lab Komputer dan 2 unit PC pada peta 2.5D NET4CITY.",
            dioramaPractice = "Susun modul diorama Laboratorium & pindai barcode NC-LAB-001 serta NC-PC-001.",
            rewardCoins = 600,
            rewardXp = 150,
            requiredNodeTypes = listOf("LAB", "PC"),
            requiredLinksCount = 0,
            stemScience = "Memahami aliran daya DC dari PSU dan pemrosesan sinyal digital pada CPU & RAM.",
            stemTechnology = "Mengenal komponen inti PC (Motherboard, NVMe SSD, NIC Gigabit).",
            stemEngineering = "Merakit unit PC laboratorium yang seimbang antara performa dan pendinginan.",
            stemMathPrompt = "Jika 1 unit PC membutuhkan daya 350 Watt dan Lab memiliki 4 PC, berapa kapasitas minimal PSU/UPS total (tambah cadangan 20%)?",
            stemMathOptions = listOf("1.200 Watt", "1.680 Watt", "900 Watt"),
            stemMathCorrectIndex = 1,
            faultScenario = "PC-01 gagal booting (Beep 3x RAM longgar). Pasang ulang modul RAM!"
        ),
        LearningLevelSpec(
            number = 2,
            title = "CONNECTION STREET",
            subtitle = "Pengkabelan UTP, RJ45 & Switch Ethernet",
            story = "Komputer di Jalan Koneksi sudah menyala, tetapi belum bisa saling berbagi berkas. Saatnya menarik kabel UTP dan memasang Switch!",
            learningObjectives = listOf(
                "Memahami standar pengkabelan T568A/T568B pada kabel UTP dan konektor RJ45.",
                "Menghubungkan beberapa komputer melalui Switch Ethernet."
            ),
            topics = listOf("NIC", "Kabel UTP", "RJ45", "Switch", "Ethernet"),
            mainMission = "Pasang 1 Switch dan hubungkan minimal 2 perangkat menggunakan kabel UTP.",
            challenge = "Jarak tarikan kabel UTP maksimal 100 meter agar tidak terjadi redaman sinyal.",
            virtualPractice = "Tambahkan Switch ke peta dan buat 2 jalur Kabel UTP antar node.",
            dioramaPractice = "Pindai NC-SWT-001 dan susun jalur kabel UTP lurus (Straight-Through) di diorama.",
            rewardCoins = 700,
            rewardXp = 200,
            requiredNodeTypes = listOf("SWITCH", "PC"),
            requiredLinksCount = 2,
            stemScience = "Memahami transmisi sinyal listrik diferensial pada pasangan kabel twisted pair untuk meredam interferensi.",
            stemTechnology = "Menggunakan konektor RJ45, NIC 1000BASE-T, dan Switch Layer 2.",
            stemEngineering = "Merancang jalur kabel rapi dari setiap komputer ke port Switch sentral.",
            stemMathPrompt = "Dibutuhkan 4 tarikan kabel UTP masing-masing 18 meter + cadangan 2 meter per tarikan. Berapa total panjang kabel UTP?",
            stemMathOptions = listOf("64 meter", "72 meter", "80 meter"),
            stemMathCorrectIndex = 2,
            faultScenario = "Kabel UTP pin 3 dan 6 terputus pada konektor RJ45. Lakukan re-crimping!"
        ),
        LearningLevelSpec(
            number = 3,
            title = "LAN SCHOOL",
            subtitle = "Jaringan Lokal Sekolah & MAC Address",
            story = "Kepala Sekolah meminta seluruh Gedung Sekolah dan Laboratorium terintegrasi dalam satu Local Area Network (LAN) yang stabil.",
            learningObjectives = listOf(
                "Memahami cara kerja MAC Address table pada Switch LAN.",
                "Membangun topologi Star untuk satu gedung sekolah."
            ),
            topics = listOf("LAN", "MAC Address", "Switch", "Kabel Jaringan", "Topologi Dasar"),
            mainMission = "Hubungkan Gedung Sekolah, Lab Komputer, dan Switch dalam topologi LAN Star.",
            challenge = "Hindari switching loop yang menyebabkan broadcast storm.",
            virtualPractice = "Bangun Gedung Sekolah, Switch, dan 3 koneksi UTP aktif.",
            dioramaPractice = "Gabungkan modul Sekolah (NC-SCH-001) dengan Switch (NC-SWT-001) di papan diorama.",
            rewardCoins = 800,
            rewardXp = 250,
            requiredNodeTypes = listOf("SEKOLAH", "SWITCH"),
            requiredLinksCount = 3,
            stemScience = "Memahami propagasi frame Ethernet dan deteksi tabrakan data (CSMA/CD vs Full-Duplex).",
            stemTechnology = "Switch 24-Port Gigabit dan alamat fisik MAC 48-bit heksadesimal.",
            stemEngineering = "Membangun struktur kabel terstruktur (Structured Cabling) antar ruang kelas.",
            stemMathPrompt = "Switch memiliki 24 port. Jika 2 port dipakai uplink dan 18 port dipakai PC kelas, berapa port tersisa untuk ekspansi?",
            stemMathOptions = listOf("4 port", "6 port", "2 port"),
            stemMathCorrectIndex = 0,
            faultScenario = "Terjadi loop kabel pada port 5 & 6 Switch Sekolah! Cabut kabel loop untuk menstabilkan LAN."
        ),
        LearningLevelSpec(
            number = 4,
            title = "WIFI DISTRICT",
            subtitle = "Jaringan Nirkabel WLAN, SSID & Coverage",
            story = "Siswa dan guru membawa tablet serta laptop ke halaman sekolah. Bangun infrastruktur Wi-Fi dengan channel bebas interferensi!",
            learningObjectives = listOf(
                "Menentukan penempatan Access Point agar tidak ada dead-zone.",
                "Mengatur SSID, keamanan WPA2/WPA3, dan pembagian channel 1-6-11."
            ),
            topics = listOf("WLAN", "Access Point", "SSID", "Channel", "Coverage"),
            mainMission = "Pasang Access Point Wi-Fi dan hubungkan ke Switch Sekolah.",
            challenge = "Pilih channel non-overlapping (1, 6, 11) pada pita 2.4 GHz.",
            virtualPractice = "Tempatkan Access Point (AP) dan sambungkan kabel UTP PoE dari Switch.",
            dioramaPractice = "Pindai NC-AP-001 dan letakkan di titik strategis modul Sekolah.",
            rewardCoins = 900,
            rewardXp = 300,
            requiredNodeTypes = listOf("AP", "SWITCH"),
            requiredLinksCount = 3,
            stemScience = "Memahami gelombang elektromagnetik 2.4 GHz vs 5 GHz serta redaman dinding beton.",
            stemTechnology = "Access Point Wi-Fi 6 dengan Power over Ethernet (PoE).",
            stemEngineering = "Merancang peta sebaran sinyal (heatmap) tanpa co-channel interference.",
            stemMathPrompt = "1 Access Point optimal melayani 30 perangkat. Jika ada 115 siswa terhubung bersamaan, minimal berapa AP dibutuhkan?",
            stemMathOptions = listOf("3 Access Point", "4 Access Point", "2 Access Point"),
            stemMathCorrectIndex = 1,
            faultScenario = "Interferensi Channel 6 menyebabkan ping Wi-Fi melonjak >250ms. Pindahkan ke Channel 11!"
        ),
        LearningLevelSpec(
            number = 5,
            title = "SERVER CENTER",
            subtitle = "Client-Server, DHCP, DNS & Web Server",
            story = "Sekolah kini butuh sistem e-learning, pembagian IP otomatis, dan penyimpanan berkas terpusat. Bangun Ruang Server Sekolah!",
            learningObjectives = listOf(
                "Memahami arsitektur Client-Server dan peran layanan DHCP, DNS, Web, File, dan Database.",
                "Menghubungkan Server ke jaringan inti sekolah."
            ),
            topics = listOf("Client-Server", "DHCP", "DNS", "Web Server", "File Server", "Database Server"),
            mainMission = "Bangun Server Sekolah dan hubungkan ke Switch Core.",
            challenge = "Konfigurasi pool DHCP agar tidak bentrok dengan IP statis server.",
            virtualPractice = "Tempatkan node Server pada peta dan tarik jalur UTP/Fiber ke Switch.",
            dioramaPractice = "Gunakan modul Server Room (NC-SRV-001) dan verifikasi penempatannya.",
            rewardCoins = 1000,
            rewardXp = 350,
            requiredNodeTypes = listOf("SERVER", "SWITCH"),
            requiredLinksCount = 4,
            stemScience = "Memahami protokol pertukaran permintaan-respons (TCP/IP, HTTP, DNS query).",
            stemTechnology = "Server Rackmount dengan layanan DHCP, DNS lokal, dan Database.",
            stemEngineering = "Merancang ruang server berpendingin dan backup daya UPS.",
            stemMathPrompt = "Pool DHCP disetel 192.168.10.20 sampai 192.168.10.120. Berapa jumlah total alamat IP dinamis yang tersedia?",
            stemMathOptions = listOf("100 alamat IP", "101 alamat IP", "99 alamat IP"),
            stemMathCorrectIndex = 1,
            faultScenario = "Layanan DHCP Server berhenti, PC baru mendapat IP APIPA 169.254.x.x. Restart layanan DHCP!"
        ),
        LearningLevelSpec(
            number = 6,
            title = "TOPOLOGY CITY",
            subtitle = "Analisis Topologi Star, Bus, Ring, Mesh, Tree & Hybrid",
            story = "Kawasan perkantoran NET4CITY berkembang pesat! Kamu harus memilih topologi jaringan terbaik berdasarkan keandalan dan biaya.",
            learningObjectives = listOf(
                "Membandingkan kelebihan dan kekurangan Star, Bus, Ring, Mesh, Tree, dan Hybrid.",
                "Menganalisis trade-off antara biaya kabel dan toleransi kegagalan."
            ),
            topics = listOf("Star", "Bus", "Ring", "Mesh", "Tree", "Hybrid"),
            mainMission = "Bangun Gedung Kantor dan buat minimal 4 jalur koneksi dalam topologi Hybrid.",
            challenge = "Sediakan jalur redundan agar 1 kabel putus tidak melumpuhkan kantor.",
            virtualPractice = "Tambahkan Gedung Kantor dan hubungkan secara Hybrid/Star dengan Switch & Server.",
            dioramaPractice = "Susun modul Kantor bersama modul Sekolah dan jelaskan topologi yang dipilih kepada guru.",
            rewardCoins = 1100,
            rewardXp = 400,
            requiredNodeTypes = listOf("KANTOR", "SWITCH", "SERVER"),
            requiredLinksCount = 4,
            stemScience = "Menerapkan teori graf (node & edge) untuk menghitung ketahanan jalur komunikasi.",
            stemTechnology = "Switch redundan dan kombinasi media kabel UTP/Fiber.",
            stemEngineering = "Menyeimbangkan biaya instalasi dengan SLA ketersediaan jaringan 99.9%.",
            stemMathPrompt = "Pada topologi Full Mesh dengan 5 Router, berapa jumlah total tautan kabel yang diperlukan (rumus n(n-1)/2)?",
            stemMathOptions = listOf("10 tautan", "15 tautan", "8 tautan"),
            stemMathCorrectIndex = 0,
            faultScenario = "Jalur utama ke Gedung Kantor terputus. Aktifkan jalur backup redundan!"
        ),
        LearningLevelSpec(
            number = 7,
            title = "INTERCITY NETWORK",
            subtitle = "IP Address, Subnetting, Gateway & Routing",
            story = "Dua distrik kota NET4CITY kini ingin saling terhubung. Pasang Router dan tentukan subnet IP agar paket data sampai ke tujuan!",
            learningObjectives = listOf(
                "Menghitung pembagian subnet IPv4 (/24, /25, /26, /27).",
                "Menghubungkan dua segmen jaringan berbeda menggunakan Router & Gateway."
            ),
            topics = listOf("IP Address", "Subnet", "Gateway", "Router", "Routing Dasar"),
            mainMission = "Pasang Router Core untuk menghubungkan segmen Sekolah dan segmen Kantor.",
            challenge = "Pastikan tidak ada overlap IP antar segmen kota.",
            virtualPractice = "Tempatkan Router Core dan hubungkan minimal 2 segmen melalui Switch.",
            dioramaPractice = "Pindai NC-RTR-001 dan tempatkan Router sebagai penghubung dua zona diorama.",
            rewardCoins = 1250,
            rewardXp = 450,
            requiredNodeTypes = listOf("ROUTER", "SWITCH"),
            requiredLinksCount = 5,
            stemScience = "Memahami logika biner (AND bitwise) untuk menentukan Network ID dan Broadcast Address.",
            stemTechnology = "Router Layer 3 dengan tabel routing statis dan dinamis.",
            stemEngineering = "Merancang skema pengalamatan IP yang terukur untuk pertumbuhan kota.",
            stemMathPrompt = "Sebuah subnet IPv4 memakai prefix /26 (subnet mask 255.255.255.192). Berapa jumlah host yang bisa dipakai (usable hosts)?",
            stemMathOptions = listOf("64 host", "62 host", "30 host"),
            stemMathCorrectIndex = 1,
            faultScenario = "Default Gateway pada subnet Kantor salah ketik sehingga gagal ping ke Sekolah. Perbaiki tabel routing!"
        ),
        LearningLevelSpec(
            number = 8,
            title = "SMART CITY",
            subtitle = "VLAN, Arsitektur Kota Cerdas & Integrasi Fasilitas",
            story = "NET4CITY bertransformasi menjadi Smart City! Rumah Sakit, Sekolah, dan Kantor harus terintegrasi dengan isolasi VLAN yang aman.",
            learningObjectives = listOf(
                "Merancang segmentasi VLAN (VLAN 10 Sekolah, VLAN 20 Kantor, VLAN 30 Rumah Sakit).",
                "Mengintegrasikan DHCP, DNS, dan Inter-VLAN Routing."
            ),
            topics = listOf("VLAN", "DHCP", "DNS", "Routing", "Network Architecture"),
            mainMission = "Bangun Rumah Sakit dan integrasikan bersama Router, Server, dan Switch.",
            challenge = "Prioritaskan trafik layanan darurat Rumah Sakit dengan QoS.",
            virtualPractice = "Bangun Rumah Sakit dan hubungkan ke Router Core dengan minimal 6 tautan.",
            dioramaPractice = "Susun diorama lintas fasilitas kota dan verifikasi pembagian VLAN bersama guru.",
            rewardCoins = 1400,
            rewardXp = 550,
            requiredNodeTypes = listOf("RS", "ROUTER", "SERVER"),
            requiredLinksCount = 6,
            stemScience = "Memahami pemisahan broadcast domain secara logis melalui tagging frame IEEE 802.1Q.",
            stemTechnology = "Managed Switch Trunking, Router-on-a-Stick, dan Server Smart City.",
            stemEngineering = "Membangun arsitektur tiga lapis (Core, Distribution, Access).",
            stemMathPrompt = "Bandwidth total kota 1.000 Mbps dibagi: 40% Rumah Sakit, 35% Sekolah, sisanya Kantor. Berapa Mbps untuk Kantor?",
            stemMathOptions = listOf("250 Mbps", "300 Mbps", "200 Mbps"),
            stemMathCorrectIndex = 0,
            faultScenario = "VLAN Tagging pada port trunk menuju Rumah Sakit hilang! Konfigurasi ulang 802.1Q Trunk."
        ),
        LearningLevelSpec(
            number = 9,
            title = "CYBER DEFENSE",
            subtitle = "Firewall, Keamanan Jaringan & Troubleshooting",
            story = "Peringatan Darurat! Serangan trafik mencurigakan menargetkan Data Center NET4CITY. Aktifkan Firewall, backup, dan monitoring!",
            learningObjectives = listOf(
                "Menerapkan aturan Firewall untuk memblokir port tidak aman dan serangan DDoS.",
                "Melakukan troubleshooting sistematis menggunakan lapisan OSI."
            ),
            topics = listOf("Firewall", "Network Security", "Backup", "Troubleshooting", "Monitoring"),
            mainMission = "Bangun Data Center dengan proteksi Firewall dan selesaikan simulasi gangguan.",
            challenge = "Pertahankan Network Health di atas 90% saat serangan berlangsung.",
            virtualPractice = "Bangun Data Center, hubungkan dengan Kabel Fiber, dan jalankan uji perbaikan gangguan.",
            dioramaPractice = "Verifikasi kelengkapan jalur backup pada diorama Server Room & Data Center.",
            rewardCoins = 1600,
            rewardXp = 650,
            requiredNodeTypes = listOf("DATACENTER", "ROUTER"),
            requiredLinksCount = 6,
            stemScience = "Memahami enkripsi kriptografi, inspeksi paket, dan anomali trafik jaringan.",
            stemTechnology = "Next-Gen Firewall, IDS/IPS, dan sistem monitoring SNMP.",
            stemEngineering = "Membangun sistem pertahanan berlapis (Defense-in-Depth) dan disaster recovery.",
            stemMathPrompt = "Firewall mencatat 12.000 paket/detik, 85% adalah trafik serangan SYN Flood yang diblokir. Berapa paket sah yang lolos/detik?",
            stemMathOptions = listOf("1.800 paket/detik", "2.400 paket/detik", "1.200 paket/detik"),
            stemMathCorrectIndex = 0,
            faultScenario = "Serangan SYN Flood pada port 80 Data Center! Aktifkan rule Firewall Rate-Limit."
        ),
        LearningLevelSpec(
            number = 10,
            title = "MINI ISP",
            subtitle = "Fiber Optik FTTH, POP, ODC, ODP & Link Budget",
            story = "Misi Puncak Network Engineer! Bangun Mini ISP lengkap dengan POP, ODC, ODP, dan kabel Fiber Optik FTTH untuk menerangi seluruh rumah warga NET4CITY!",
            learningObjectives = listOf(
                "Merancang jaringan akses FTTH (POP -> ODC -> ODP -> Rumah Pelanggan).",
                "Menghitung redaman optik (Link Budget) dan analisis kelayakan biaya pelanggan."
            ),
            topics = listOf("Fiber Optic", "POP", "Backbone", "ODC", "ODP", "Splitter", "FTTH", "Link Budget", "Maintenance"),
            mainMission = "Bangun rantai lengkap POP ISP -> ODC -> ODP -> Rumah Warga dengan jalur Fiber Optik.",
            challenge = "Jaga total redaman optik di bawah batas maksimal -28 dBm.",
            virtualPractice = "Tempatkan POP, ODC, ODP, dan Rumah lalu tarik kabel Fiber Optik berurutan.",
            dioramaPractice = "Pindai NC-POP-001, NC-ODC-001, NC-ODP-001, NC-HOME-001 dan susun alur FTTH di diorama.",
            rewardCoins = 2500,
            rewardXp = 1000,
            requiredNodeTypes = listOf("POP", "ODC", "ODP", "RUMAH"),
            requiredLinksCount = 7,
            stemScience = "Memahami pemantulan sempurna cahaya laser (Total Internal Reflection) dalam serat kaca optik.",
            stemTechnology = "OLT GPON, Kabel Fiber Single-Mode, Passive Splitter 1:4 & 1:8, serta ONT.",
            stemEngineering = "Merancang distribusi FTTH yang efisien, mudah dirawat, dan menguntungkan.",
            stemMathPrompt = "Daya kirim OLT +4 dBm. Redaman: Splitter 1:4 (-7 dB), Splitter 1:8 (-10 dB), kabel & konektor (-3 dB). Berapa daya terima di ONT rumah?",
            stemMathOptions = listOf("-16 dBm (Sangat Baik)", "-28 dBm (Kritis)", "-10 dBm"),
            stemMathCorrectIndex = 0,
            faultScenario = "Redaman di ODP-01 melonjak ke -31 dBm akibat tekukan tajam (macrobending) fiber! Luruskan patchcord."
        )
    )

    fun createDemoClasses(userId: String): List<Pair<String, Map<String, Any>>> {
        val classes = listOf(
            Triple("x_tkj_1", "X TKJ 1", "Pak Hendra Wijaya, S.Kom"),
            Triple("x_tkj_2", "X TKJ 2", "Pak Hendra Wijaya, S.Kom"),
            Triple("x_tkj_3", "X TKJ 3", "Bu Ratna Sari, S.T"),
            Triple("x_tkj_4", "X TKJ 4", "Bu Ratna Sari, S.T")
        )
        return classes.mapIndexed { idx, (id, code, teacher) ->
            id to mapOf(
                "userId" to userId,
                "code" to code,
                "name" to "Teknik Jaringan Komputer & Telekomunikasi $code",
                "teacherName" to teacher,
                "maxUnlockedLevel" to (3L - (idx % 2)),
                "activeSessionMission" to "Level ${idx % 2 + 1} - Praktik Virtual & Diorama",
                "labGroups" to listOf("Kelompok Alpha", "Kelompok Beta", "Kelompok Gamma"),
                "demoData" to true
            )
        }
    }

    fun createDemoMembers(userId: String): List<Pair<String, Map<String, Any>>> {
        val result = mutableListOf<Pair<String, Map<String, Any>>>()
        // 2 Teachers
        result += "guru_01" to mapOf(
            "userId" to userId,
            "memberType" to "GURU",
            "fullName" to "Pak Hendra Wijaya, S.Kom",
            "identityNumber" to "NIP-19880412001",
            "classId" to "x_tkj_1",
            "assignedClasses" to listOf("x_tkj_1", "x_tkj_2"),
            "groupName" to "Koordinator Lab TJKT",
            "xp" to 0L,
            "level" to 10L,
            "characterId" to "NC-CHAR-00001",
            "demoData" to true
        )
        result += "guru_02" to mapOf(
            "userId" to userId,
            "memberType" to "GURU",
            "fullName" to "Bu Ratna Sari, S.T",
            "identityNumber" to "NIP-19910723002",
            "classId" to "x_tkj_3",
            "assignedClasses" to listOf("x_tkj_3", "x_tkj_4"),
            "groupName" to "Pembina Fiber Optik",
            "xp" to 0L,
            "level" to 10L,
            "characterId" to "NC-CHAR-00002",
            "demoData" to true
        )

        // 5 Students per class = 20 students total
        val studentNames = mapOf(
            "x_tkj_1" to listOf("Raka Pratama", "Nadia Putri", "Dimas Saputra", "Aulia Rahman", "Fajar Nugroho"),
            "x_tkj_2" to listOf("Bagas Kurniawan", "Siti Nurhaliza", "Reza Mahendra", "Intan Permata", "Kevin Aditya"),
            "x_tkj_3" to listOf("Gilang Ramadhan", "Dewi Lestari", "Yusuf Hakim", "Tiara Anjani", "Rizky Febrian"),
            "x_tkj_4" to listOf("Aldi Firmansyah", "Zahra Kamila", "Bayu Setiawan", "Citra Maharani", "Naufal Azhari")
        )
        var counter = 1
        studentNames.forEach { (classId, names) ->
            names.forEachIndexed { idx, name ->
                val docId = "siswa_%02d".format(counter)
                val charId = if (counter <= 8) "NC-CHAR-%05d".format(counter + 2) else ""
                result += docId to mapOf(
                    "userId" to userId,
                    "memberType" to "SISWA",
                    "fullName" to name,
                    "identityNumber" to "NIS-2026-%03d".format(counter),
                    "classId" to classId,
                    "assignedClasses" to emptyList<String>(),
                    "groupName" to listOf("Kelompok Alpha", "Kelompok Beta", "Kelompok Gamma")[idx % 3],
                    "xp" to (180L + counter * 35L),
                    "level" to ((counter % 4) + 1L),
                    "characterId" to charId,
                    "demoData" to true
                )
                counter++
            }
        }
        return result
    }

    fun createDemoCharacters(userId: String): List<Pair<String, Map<String, Any>>> {
        val entries = listOf(
            Triple("NC-CHAR-00001", "Hendra Core", "Pak Hendra Wijaya, S.Kom") to Triple("x_tkj_1", "ISP Manager", "Jaket ISP Lapangan"),
            Triple("NC-CHAR-00002", "Ratna Fiber", "Bu Ratna Sari, S.T") to Triple("x_tkj_3", "Fiber Optic Technician", "Rompi Safety Fiber"),
            Triple("NC-CHAR-00003", "RakaByte", "Raka Pratama") to Triple("x_tkj_1", "Network Technician", "Seragam Teknisi Biru"),
            Triple("NC-CHAR-00004", "NadiaNet", "Nadia Putri") to Triple("x_tkj_1", "Computer Technician", "Seragam Siswa SMK TKJ"),
            Triple("NC-CHAR-00005", "DimasSwitch", "Dimas Saputra") to Triple("x_tkj_1", "Server Administrator", "Kemeja Server Admin"),
            Triple("NC-CHAR-00006", "AuliaWiFi", "Aulia Rahman") to Triple("x_tkj_1", "Network Engineer", "Seragam Teknisi Biru"),
            Triple("NC-CHAR-00007", "FajarPing", "Fajar Nugroho") to Triple("x_tkj_1", "Computer Technician", "Seragam Siswa SMK TKJ"),
            Triple("NC-CHAR-00008", "BagasRoute", "Bagas Kurniawan") to Triple("x_tkj_2", "Network Engineer", "Jaket ISP Lapangan"),
            Triple("NC-CHAR-00009", "GilangOptic", "Gilang Ramadhan") to Triple("x_tkj_3", "Fiber Optic Technician", "Rompi Safety Fiber"),
            Triple("NC-CHAR-00010", "AldiCyber", "Aldi Firmansyah") to Triple("x_tkj_4", "Server Administrator", "Kemeja Server Admin")
        )
        return entries.mapIndexed { idx, (left, right) ->
            val (code, name, owner) = left
            val (classId, profession, outfit) = right
            code to mapOf(
                "userId" to userId,
                "characterCode" to code,
                "name" to name,
                "ownerName" to owner,
                "classId" to classId,
                "profession" to profession,
                "faceShape" to faceShapes[idx % faceShapes.size],
                "hairStyle" to hairStyles[idx % hairStyles.size],
                "hairColor" to hairColors[idx % hairColors.size],
                "outfitStyle" to outfit,
                "pantsColor" to pantsColors[idx % pantsColors.size],
                "shoeStyle" to shoeStyles[idx % shoeStyles.size],
                "accessory" to accessories[idx % accessories.size],
                "workTool" to workTools[idx % workTools.size],
                "level" to ((idx % 5) + 1L),
                "xp" to (200L + idx * 110L),
                "skills" to listOf("Crimping RJ45", "Konfigurasi LAN", "Troubleshooting"),
                "demoData" to true
            )
        }
    }

    fun createDemoBarcodes(userId: String): List<Pair<String, Map<String, Any>>> {
        val raw = listOf(
            listOf("NC-CHAR-00003", "Kartu Karakter RakaByte", "Karakter", "Identitas digital siswa pada PID & sesi diorama.", "QR ID Permanen NC-CHAR-00003", 0, 0, "Scan di awal sesi praktik", 1, "Loker Kartu X TKJ 1"),
            listOf("NC-PC-001", "Komputer Lab Unit 01", "Komputer", "Workstation praktikum perakitan & client jaringan.", "Intel i5, RAM 16GB, SSD 512GB, NIC 1Gbps", 1, 150, "Kabel power & UTP Cat6", 1, "Meja Lab A1"),
            listOf("NC-PC-002", "Komputer Lab Unit 02", "Komputer", "Workstation client untuk pengujian ping dan sharing.", "Intel i5, RAM 16GB, SSD 512GB, NIC 1Gbps", 1, 150, "Kabel power & UTP Cat6", 1, "Meja Lab A2"),
            listOf("NC-PC-003", "Komputer Lab Unit 03", "Komputer", "Workstation uji DHCP dan akses server lokal.", "Ryzen 5, RAM 16GB, SSD 512GB, NIC 1Gbps", 1, 150, "Kabel power & UTP Cat6", 2, "Meja Lab A3"),
            listOf("NC-SWT-001", "Switch Manageable 24P-A", "Switch", "Penghubung perangkat LAN dan manajemen VLAN 802.1Q.", "24x GbE RJ45, 2x SFP, Web/CLI Managed", 24, 250, "Rack 19 inch, UTP Cat6", 2, "Rak Diorama B1"),
            listOf("NC-SWT-002", "Switch Distribusi 16P-B", "Switch", "Switch distribusi antar ruang kelas dan lab.", "16x GbE RJ45, Auto-MDIX, Fanless", 16, 210, "Power AC & Patchcord", 3, "Rak Diorama B1"),
            listOf("NC-AP-001", "Access Point Wi-Fi 6 Sekolah", "Access Point", "Pemancar jaringan nirkabel WLAN dual-band.", "AX1800 Dual Band, PoE 802.3af, Multi-SSID", 2, 220, "Kabel UTP PoE dari Switch", 4, "Rak Diorama B2"),
            listOf("NC-AP-002", "Access Point Outdoor Taman", "Access Point", "Memperluas coverage Wi-Fi area halaman sekolah.", "IP65 Outdoor, Dual-Band, Antena Omnidirectional", 1, 260, "Bracket tiang & PoE Injector", 4, "Rak Diorama B2"),
            listOf("NC-SRV-001", "Server Sekolah Utama", "Server", "Menjalankan DHCP, DNS, Web e-Learning, dan Database.", "Xeon 8C, 64GB ECC, 2x2TB NVMe RAID-1", 4, 600, "Ruang ber-AC, UPS 1500VA", 5, "Rak Server C1"),
            listOf("NC-SRV-002", "File & Backup Server", "Server", "Penyimpanan berkas praktik siswa dan backup otomatis.", "Xeon 6C, 32GB ECC, 4x4TB HDD RAID-5", 2, 520, "UPS & jalur Gigabit Trunk", 9, "Rak Server C1"),
            listOf("NC-RTR-001", "Router Core Mikrotik RB", "Router", "Routing antar jaringan kota, NAT, dan Gateway VLAN.", "8x Gigabit + 2x SFP+, Dual Core 1.4GHz", 10, 480, "Konfigurasi IP & Routing Table", 7, "Rak Diorama C2"),
            listOf("NC-RTR-002", "Router Edge Distrik Timur", "Router", "Menghubungkan kawasan perkantoran ke backbone kota.", "5x Gigabit + 1x SFP, OSPF/Static Routing", 6, 420, "Adaptor 24V & Patchcord", 7, "Rak Diorama C2"),
            listOf("NC-FW-001", "Firewall Appliance NOC", "Keamanan", "Inspeksi paket, pencegahan DDoS, dan filtering port.", "Throughput 2Gbps, IDS/IPS, VPN IPSec", 6, 680, "Dipasang inline depan Data Center", 9, "Rak Server C3"),
            listOf("NC-DC-001", "Miniatur Data Center Kota", "Infrastruktur", "Pusat layanan digital Smart City dan pertahanan siber.", "Redundant PSU, Core Switch, Firewall Rack", 32, 1100, "Backbone Fiber Optik & Pendingin", 9, "Kotak Diorama D1"),
            listOf("NC-POP-001", "Modul POP Mini ISP (OLT)", "Infrastruktur", "Point of Presence ISP sumber sinyal optik GPON.", "OLT 8-Port GPON, SFP Class C+, BGP Core", 16, 1200, "Sumber listrik backup & Feeder FO", 10, "Kotak Diorama D2"),
            listOf("NC-ODC-001", "Kabinet ODC Distrik 01", "Infrastruktur", "Optical Distribution Cabinet splitter fiber 1:4.", "Kapasitas 144 Core, Splitter 1:4 SC/UPC", 12, 550, "Kabel Feeder dari POP ISP", 10, "Kotak Diorama D2"),
            listOf("NC-ODP-001", "Box ODP Tiang Blok A", "Infrastruktur", "Optical Distribution Point terminasi 8 rumah pelanggan.", "Passive Splitter 1:8, Adapter SC/UPC", 8, 320, "Kabel Distribusi dari ODC", 10, "Kotak Diorama D3"),
            listOf("NC-HOME-001", "Modul Rumah Pelanggan 01", "Bangunan", "Pelanggan FTTH dilengkapi ONT Wi-Fi rumahan.", "ONT Dual-Band, Port SC/UPC + 4 LAN", 4, 200, "Dropcore Fiber dari ODP", 10, "Kotak Diorama A1"),
            listOf("NC-SCH-001", "Modul Gedung Sekolah SMK", "Bangunan", "Area belajar utama jaringan LAN & Wi-Fi.", "Kapasitas 4 ruang kelas & panel distribusi", 4, 400, "Switch LAN & Access Point", 3, "Kotak Diorama A1"),
            listOf("NC-LAB-001", "Modul Lab Komputer TKJ", "Bangunan", "Ruang praktikum perakitan PC dan simulasi topologi.", "Kapasitas 20 workstation, AC, Rack 12U", 8, 450, "PC Client & Switch 24-Port", 1, "Kotak Diorama A2")
        )
        return raw.map { row ->
            val code = row[0] as String
            code to mapOf(
                "userId" to userId,
                "barcodeCode" to code,
                "objectName" to (row[1] as String),
                "category" to (row[2] as String),
                "functionDesc" to (row[3] as String),
                "specs" to (row[4] as String),
                "portCount" to (row[5] as Int).toLong(),
                "virtualPrice" to (row[6] as Int).toLong(),
                "installNeeds" to (row[7] as String),
                "relatedLevel" to (row[8] as Int).toLong(),
                "storageLocation" to (row[9] as String),
                "inventoryStatus" to "TERSEDIA",
                "activeClassId" to "",
                "activeStudentName" to "",
                "demoData" to true
            )
        }
    }

    fun createDemoDioramaModules(userId: String): List<Pair<String, Map<String, Any>>> {
        val raw = listOf(
            listOf("MOD-HOME-01", "Modul Rumah Warga", "Rumah", "NC-HOME-001", "Blok diorama rumah residensial dengan dudukan ONT FTTH.", "Rak Diorama A1", listOf(4L, 10L)),
            listOf("MOD-SCH-01", "Modul Gedung Sekolah", "Sekolah", "NC-SCH-001", "Blok gedung sekolah SMK dengan titik pasang Access Point.", "Rak Diorama A1", listOf(3L, 4L, 8L)),
            listOf("MOD-OFC-01", "Modul Gedung Kantor", "Kantor", "NC-RTR-002", "Blok perkantoran kota untuk praktik VLAN dan topologi Hybrid.", "Rak Diorama A2", listOf(6L, 7L, 8L)),
            listOf("MOD-LAB-01", "Modul Laboratorium TKJ", "Laboratorium", "NC-LAB-001", "Blok ruang lab perakitan PC dan uji kabel UTP.", "Rak Diorama A2", listOf(1L, 2L, 3L)),
            listOf("MOD-SRV-01", "Modul Server Room", "Server Room", "NC-SRV-001", "Blok ruang server dilengkapi miniatur rackmount dan UPS.", "Rak Diorama B1", listOf(5L, 8L, 9L)),
            listOf("MOD-POP-01", "Modul POP Mini ISP", "POP", "NC-POP-001", "Blok pusat operasi ISP dengan slot OLT dan core router.", "Rak Diorama B2", listOf(10L)),
            listOf("MOD-ODC-01", "Modul Kabinet ODC", "ODC", "NC-ODC-001", "Miniatur kabinet ODC pinggir jalan dengan jalur splitter 1:4.", "Rak Diorama B2", listOf(10L)),
            listOf("MOD-ODP-01", "Modul Tiang ODP", "ODP", "NC-ODP-001", "Miniatur tiang distribusi optik dengan 8 port dropcore.", "Rak Diorama B3", listOf(10L)),
            listOf("MOD-RD-01", "Modul Jalan & Ducting Kota", "Jalan", "NC-SWT-002", "Tile jalan raya modular dengan parit jalur duct kabel bawah tanah.", "Rak Diorama C1", listOf(2L, 6L, 7L, 10L)),
            listOf("MOD-CBL-01", "Modul Kit Jaringan Kabel", "Jaringan Kabel", "NC-SWT-001", "Set patchcord warna-warni UTP (Biru) & simulasi Fiber (Kuning/Cyan).", "Rak Diorama C2", listOf(2L, 3L, 6L, 7L, 10L))
        )
        return raw.map { row ->
            val code = row[0] as String
            @Suppress("UNCHECKED_CAST")
            val reqLevels = row[6] as List<Long>
            code to mapOf(
                "userId" to userId,
                "moduleCode" to code,
                "name" to (row[1] as String),
                "category" to (row[2] as String),
                "barcodeCode" to (row[3] as String),
                "description" to (row[4] as String),
                "storageLocation" to (row[5] as String),
                "moduleStatus" to "TERSEDIA",
                "requiredLevels" to reqLevels,
                "activeClassId" to "",
                "activeSessionLabel" to "",
                "verifiedByTeacher" to false,
                "demoData" to true
            )
        }
    }

    fun createDemoAssessments(userId: String): List<Pair<String, Map<String, Any>>> {
        val sample = listOf(
            listOf("assess_01", "Raka Pratama", "x_tkj_1", "Kelompok Alpha", 1L, "Merakit Komputer Lab", 42L, 90L, 88L, 92L, 86L, 89L, "Lab & 2 PC aktif, kalkulasi daya tepat", true, "Sangat teliti saat memasang NIC dan menyusun modul diorama.", "DRAFT"),
            listOf("assess_02", "Nadia Putri", "x_tkj_1", "Kelompok Alpha", 2L, "Menghubungkan PC via Switch", 48L, 86L, 84L, 88L, 82L, 85L, "Switch + 2 jalur UTP terhubung baik", true, "Urutan warna T568B rapi dan pengujian konektivitas berhasil.", "DRAFT"),
            listOf("assess_03", "Bagas Kurniawan", "x_tkj_2", "Kelompok Beta", 3L, "Membangun LAN Sekolah", 55L, 84L, 86L, 80L, 85L, 84L, "Topologi Star Sekolah tanpa loop", true, "Pemahaman MAC Address table sudah baik, perlu rapikan manajemen kabel.", "DRAFT"),
            listOf("assess_04", "Gilang Ramadhan", "x_tkj_3", "Kelompok Gamma", 1L, "Merakit Komputer Lab", 40L, 92L, 90L, 94L, 88L, 91L, "Skor STEM sempurna pada uji daya UPS", true, "Inisiatif tinggi membantu anggota kelompok saat troubleshooting RAM.", "FINALIZED"),
            listOf("assess_05", "Aldi Firmansyah", "x_tkj_4", "Kelompok Alpha", 2L, "Menghubungkan PC via Switch", 50L, 78L, 80L, 82L, 76L, 79L, "Jalur UTP aktif setelah perbaikan pin 3", false, "Perlu latihan tambahan saat crimping konektor RJ45.", "DRAFT")
        )
        return sample.map { row ->
            (row[0] as String) to mapOf(
                "userId" to userId,
                "studentName" to (row[1] as String),
                "classId" to (row[2] as String),
                "groupName" to (row[3] as String),
                "levelNumber" to (row[4] as Long),
                "missionTitle" to (row[5] as String),
                "durationMinutes" to (row[6] as Long),
                "technicalScore" to (row[7] as Long),
                "designScore" to (row[8] as Long),
                "physicalScore" to (row[9] as Long),
                "troubleshootingScore" to (row[10] as Long),
                "finalScore" to (row[11] as Long),
                "gameResultSummary" to (row[12] as String),
                "dioramaVerified" to (row[13] as Boolean),
                "teacherNote" to (row[14] as String),
                "status" to (row[15] as String),
                "demoData" to true
            )
        }
    }
}
