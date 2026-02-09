package com.example.cfvaname.ui.localization

sealed class AppStrings(val id: String, val en: String) {
    // ===================================================
    // NAVIGATION & SCREENS
    // ===================================================
    object Dashboard : AppStrings("Dasbor", "Dashboard")
    object Home : AppStrings("Beranda", "Home")
    object Menu : AppStrings("Menu", "Menu")
    object Gejala : AppStrings("Gejala", "Symptoms")
    object Hipotesis : AppStrings("Hipotesis", "Hypothesis")
    object NilaiCf : AppStrings("Nilai CF", "CF Values")
    object Profile : AppStrings("Profil", "Profile")
    object Reports : AppStrings("Laporan", "Reports")
    object Settings : AppStrings("Pengaturan", "Settings")
    object About : AppStrings("Tentang", "About")
    
    // ===================================================
    // AUTHENTICATION
    // ===================================================
    object Welcome : AppStrings("Selamat Datang", "Welcome")
    object Login : AppStrings("Masuk", "Login")
    object Email : AppStrings("Email", "Email")
    object Password : AppStrings("Kata Sandi", "Password")
    object ForgotPassword : AppStrings("Lupa Kata Sandi?", "Forgot Password?")
    object DontHaveAccount : AppStrings("Belum punya akun?", "Don't have an account?")
    object Register : AppStrings("Daftar", "Register")
    object EnterEmail : AppStrings("Masukkan email", "Enter email")
    object EnterPassword : AppStrings("Masukkan kata sandi", "Enter password")
    object LoginButton : AppStrings("Masuk", "Sign In")
    object LoggingIn : AppStrings("Sedang masuk...", "Logging in...")
    
    // ===================================================
    // SETTINGS SCREEN
    // ===================================================
    object SettingsTitle : AppStrings("Pengaturan", "Settings")
    object Notifications : AppStrings("Notifikasi", "Notifications")
    object ManageNotifications : AppStrings("Kelola notifikasi", "Manage notifications")
    object Language : AppStrings("Bahasa", "Language")
    object LanguageIndonesian : AppStrings("Indonesia", "Indonesian")
    object LanguageEnglish : AppStrings("Inggris", "English")
    object Theme : AppStrings("Tema", "Theme")
    object ThemeLight : AppStrings("Terang", "Light")
    object ThemeDark : AppStrings("Gelap", "Dark")
    object ThemeSystem : AppStrings("Mengikuti Sistem", "Follow System")
    object Storage : AppStrings("Penyimpanan", "Storage")
    object ManageLocalData : AppStrings("Kelola data lokal", "Manage local data")
    object Privacy : AppStrings("Privasi", "Privacy")
    object PrivacySettings : AppStrings("Pengaturan privasi", "Privacy settings")
    object Logout : AppStrings("Keluar dari Akun", "Logout")
    object SelectLanguage : AppStrings("Pilih Bahasa", "Select Language")
    object SelectTheme : AppStrings("Pilih Tema", "Select Theme")
    
    // ===================================================
    // DASHBOARD
    // ===================================================
    object WelcomeBack : AppStrings("Selamat Datang Kembali", "Welcome Back")
    object QuickActions : AppStrings("Aksi Cepat", "Quick Actions")
    object Statistics : AppStrings("Statistik", "Statistics")
    object RecentActivities : AppStrings("Aktivitas Terbaru", "Recent Activities")
    object TotalSymptoms : AppStrings("Total Gejala", "Total Symptoms")
    object TotalHypothesis : AppStrings("Total Hipotesis", "Total Hypothesis")
    object TotalDiagnosis : AppStrings("Total Diagnosis", "Total Diagnosis")
    
    // Dashboard activities
    object NewUserRegistered : AppStrings("User baru terdaftar", "New user registered")
    object DataUploadSuccess : AppStrings("Data berhasil di-upload", "Data uploaded successfully")
    object SystemUpdated : AppStrings("Sistem telah diperbarui", "System has been updated")
    object MonthlyReportCreated : AppStrings("Laporan bulanan dibuat", "Monthly report created")
    object BackupCompleted : AppStrings("Backup otomatis selesai", "Automatic backup completed")
    object MinutesAgo : AppStrings("menit lalu", "minutes ago")
    object HourAgo : AppStrings("jam lalu", "hours ago")
    
    // ===================================================
    // DATA GEJALA (SYMPTOMS)
    // ===================================================
    object DataGejala : AppStrings("Data Gejala", "Symptoms Data")
    object ManageSymptomData : AppStrings("Kelola data gejala penyakit", "Manage disease symptom data")
    object AddSymptom : AppStrings("Tambah Gejala", "Add Symptom")
    object EditSymptom : AppStrings("Edit Gejala", "Edit Symptom")
    object DeleteSymptom : AppStrings("Hapus Gejala", "Delete Symptom")
    object SymptomCode : AppStrings("Kode Gejala", "Symptom Code")
    object SymptomName : AppStrings("Nama Gejala", "Symptom Name")
    object SymptomDescription : AppStrings("Deskripsi Gejala", "Symptom Description")
    object NoSymptoms : AppStrings("Belum ada data gejala", "No symptoms data")
    object SearchSymptom : AppStrings("Cari gejala...", "Search symptom...")
    object NoSymptomFoundFor : AppStrings("Tidak ada gejala ditemukan untuk", "No symptoms found for")
    object SymptomCodeExample : AppStrings("Contoh: G001", "Example: G001")
    object SymptomNameExample : AppStrings("Contoh: Demam tinggi", "Example: High fever")
    object DeletedDataCannotBeRestored : AppStrings("Data yang dihapus tidak dapat dikembalikan.", "Deleted data cannot be restored.")
    object Deleting : AppStrings("Menghapus...", "Deleting...")
    
    // ===================================================
    // DATA HIPOTESIS (HYPOTHESIS)
    // ===================================================
    object DataHipotesis : AppStrings("Data Hipotesis", "Hypothesis Data")
    object ManageHypothesisData : AppStrings("Kelola data hipotesis penyakit", "Manage disease hypothesis data")
    object AddHypothesis : AppStrings("Tambah Hipotesis", "Add Hypothesis")
    object EditHypothesis : AppStrings("Edit Hipotesis", "Edit Hypothesis")
    object DeleteHypothesis : AppStrings("Hapus Hipotesis", "Delete Hypothesis")
    object HypothesisCode : AppStrings("Kode Hipotesis", "Hypothesis Code")
    object HypothesisName : AppStrings("Nama Hipotesis", "Hypothesis Name")
    object HypothesisDescription : AppStrings("Deskripsi Hipotesis", "Hypothesis Description")
    object NoHypothesis : AppStrings("Belum ada data hipotesis", "No hypothesis data")
    object SearchHypothesis : AppStrings("Cari hipotesis...", "Search hypothesis...")
    object NoHypothesisFoundFor : AppStrings("Tidak ditemukan untuk", "Not found for")
    object HypothesisCodeExample : AppStrings("Contoh: H001", "Example: H001")
    object HypothesisNameExample : AppStrings("Contoh: COVID-19", "Example: COVID-19")
    object HypothesisDescriptionPlaceholder : AppStrings("Penjelasan tentang hipotesis...", "Explanation about hypothesis...")
    object Recommendation : AppStrings("Rekomendasi", "Recommendation")
    object RecommendationOptional : AppStrings("Rekomendasi (opsional)", "Recommendation (optional)")
    object RecommendationPlaceholder : AppStrings("Tindakan yang disarankan...", "Recommended action...")
    object Optional : AppStrings("(opsional)", "(optional)")
    object DetailHypothesis : AppStrings("Detail Hipotesis", "Hypothesis Detail")
    object SymptomsReferencingWillBeAffected : AppStrings("Gejala yang merujuk hipotesis ini akan terdampak.", "Symptoms referencing this hypothesis will be affected.")
    
    // ===================================================
    // NILAI CF (CERTAINTY FACTOR)
    // ===================================================
    object DataNilaiCf : AppStrings("Data Nilai CF", "CF Values Data")
    object AddCfValue : AppStrings("Tambah Nilai CF", "Add CF Value")
    object EditCfValue : AppStrings("Edit Nilai CF", "Edit CF Value")
    object DeleteCfValue : AppStrings("Hapus Nilai CF", "Delete CF Value")
    object CfValue : AppStrings("Nilai CF", "CF Value")
    object SelectSymptom : AppStrings("Pilih Gejala", "Select Symptom")
    object SelectHypothesis : AppStrings("Pilih Hipotesis", "Select Hypothesis")
    object NoCfValues : AppStrings("Belum ada data nilai CF", "No CF values data")
    object CertaintyFactor : AppStrings("Certainty Factor (0.00 - 1.00)", "Certainty Factor (0.00 - 1.00)")
    object Level : AppStrings("Level", "Level")
    object AutoDistributedValues : AppStrings("Nilai terdistribusi otomatis secara merata dari 0,00 sampai 1,00 sesuai jumlah data.", "Values are automatically distributed evenly from 0.00 to 1.00 based on the number of data.")
    object ValueDistribution : AppStrings("Distribusi Nilai", "Value Distribution")
    object LevelOfTotal : AppStrings("Level %d dari %d", "Level %d of %d")
    object Description : AppStrings("Keterangan", "Description")
    object DescriptionExample : AppStrings("Contoh: Cukup Yakin", "Example: Quite Certain")
    object PreviewAfterAdd : AppStrings("Preview distribusi setelah ditambah:", "Preview distribution after adding:")
    object AutoCannotChangeManually : AppStrings("(otomatis, tidak bisa diubah manual)", "(automatic, cannot be changed manually)")
    object AllOtherCfValuesWillRecalculate : AppStrings("Semua nilai CF lainnya akan dihitung ulang otomatis.", "All other CF values will be recalculated automatically.")
    
    // ===================================================
    // PROFILE
    // ===================================================
    object MyProfile : AppStrings("Profil Saya", "My Profile")
    object FullName : AppStrings("Nama Lengkap", "Full Name")
    object PhoneNumber : AppStrings("Nomor Telepon", "Phone Number")
    object Address : AppStrings("Alamat", "Address")
    object EditProfile : AppStrings("Edit Profil", "Edit Profile")
    object ChangePassword : AppStrings("Ubah Kata Sandi", "Change Password")
    object CurrentPassword : AppStrings("Kata Sandi Saat Ini", "Current Password")
    object NewPassword : AppStrings("Kata Sandi Baru", "New Password")
    object ConfirmPassword : AppStrings("Konfirmasi Kata Sandi", "Confirm Password")
    
    // ===================================================
    // RULES
    // ===================================================
    object Rules : AppStrings("Aturan / Rules", "Rules")
    object ConnectSymptomWithCfValue : AppStrings("Hubungkan gejala dengan nilai CF", "Connect symptoms with CF values")
    object Total : AppStrings("Total", "Total")
    object LoadingRulesData : AppStrings("Memuat data rules...", "Loading rules data...")
    object NoRulesData : AppStrings("Belum ada data rules", "No rules data")
    object AddRule : AppStrings("Tambah Rule", "Add Rule")
    object AddNewRule : AppStrings("Tambah Rule Baru", "Add New Rule")
    object EditRule : AppStrings("Edit Rule", "Edit Rule")
    object DeleteRule : AppStrings("Hapus Rule?", "Delete Rule?")
    object Rule : AppStrings("Rule", "Rule")
    object Symptom : AppStrings("Gejala", "Symptom")
    object SelectSymptomPlaceholder : AppStrings("Pilih gejala...", "Select symptom...")
    object NoSymptomAvailable : AppStrings("Tidak ada gejala tersedia", "No symptoms available")
    object SelectCfValuePlaceholder : AppStrings("Pilih nilai CF...", "Select CF value...")
    object NoCfValueAvailable : AppStrings("Tidak ada nilai CF", "No CF values available")
    object ConfirmDeleteRule : AppStrings("Anda yakin ingin menghapus rule ini?", "Are you sure you want to delete this rule?")
    object Saving : AppStrings("Menyimpan...", "Saving...")
    object IdNotFound : AppStrings("tidak ditemukan", "not found")
    object SelectCertaintyLevel : AppStrings("Pilih tingkat keyakinan untuk setiap gejala", "Select certainty level for each symptom")
    object SaveAllRules : AppStrings("Simpan Semua Rules", "Save All Rules")
    object NoHypothesisWithSymptoms : AppStrings("Belum ada hipotesis dengan gejala", "No hypothesis with symptoms yet")
    object SymptomsCount : AppStrings("%d Gejala", "%d Symptoms")
    object NotSet : AppStrings("Belum diatur", "Not set")
    object RulesSavedSuccessfully : AppStrings("Rules berhasil disimpan", "Rules saved successfully")
    
    // ===================================================
    // KUESIONER / QUESTIONNAIRE
    // ===================================================
    object Questionnaire : AppStrings("Kuesioner", "Questionnaire")
    object Questionnaire_Title : AppStrings("Kuesioner", "Questionnaire")
    object DiseaseQuestionnaireVename : AppStrings("Diagnosa penyakit udang vaname", "Vename shrimp disease diagnosis")
    object Data : AppStrings("Data", "Data")
    object NoQuestionnaireData : AppStrings("Belum ada data kuesioner", "No questionnaire data")
    object LoadingData : AppStrings("Memuat data...", "Loading data...")
    object NewQuestionnaire : AppStrings("Kuesioner Baru", "New Questionnaire")
    object FarmerInformation : AppStrings("Informasi Petambak", "Farmer Information")
    object FarmerName : AppStrings("Nama Petambak", "Farmer Name")
    object PhoneNumber_Header : AppStrings("No. HP", "Phone Number")
    object FarmLocation : AppStrings("Lokasi Tambak", "Farm Location")
    object ShrimpAge : AppStrings("Usia Udang (hari)", "Shrimp Age (days)")
    object SelectSymptomsAndConfidence : AppStrings("Pilih Gejala & Tingkat Keyakinan", "Select Symptoms & Confidence Level")
    object SelectSymptomInstructions : AppStrings("Pilih gejala yang dialami dan tentukan tingkat keyakinan Anda", "Select symptoms experienced and determine your confidence level")
    object SymptomCountSelected : AppStrings("%d gejala dipilih", "%d symptoms selected")
    object SaveAndViewResults : AppStrings("Simpan & Lihat Hasil", "Save & View Results")
    object DiagnosisResults : AppStrings("Hasil Diagnosa", "Diagnosis Results")
    object SelectedSymptoms : AppStrings("Gejala yang Dipilih", "Selected Symptoms")
    object CfCalculationDetails : AppStrings("Detail Perhitungan CF", "CF Calculation Details")
    object RankingResults : AppStrings("Hasil Ranking", "Ranking Results")
    object DiagnosisSummary : AppStrings("Kesimpulan Diagnosa", "Diagnosis Summary")
    object MainDiagnosis : AppStrings("Diagnosa Utama", "Main Diagnosis")
    object ConfidenceLevel : AppStrings("Keyakinan", "Confidence")
    object ChangeConfidence : AppStrings("Ubah", "Change")
    object DiagnosisReportTitle : AppStrings("LAPORAN DIAGNOSA SISTEM PAKAR", "EXPERT SYSTEM DIAGNOSIS REPORT")
    object CertaintyFactorAnalysis : AppStrings("Hasil Analisis Certainty Factor - VENAME", "Certainty Factor Analysis Results - VENAME")
    object DeleteQuestionnaire : AppStrings("Hapus Kuesioner?", "Delete Questionnaire?")
    object DeleteQuestionnaireConfirm : AppStrings("Hapus kuesioner \"%s\" beserta seluruh datanya?", "Delete questionnaire \"%s\" and all its data?")
    object ViewResults : AppStrings("Lihat Hasil", "View Results")
    object Delete_Button : AppStrings("Hapus", "Delete")
    object Cancel_Button : AppStrings("Batal", "Cancel")
    object SymptomsTable : AppStrings("Gejala", "Symptoms")
    object CfExpertTableHeader : AppStrings("CF\nPakar", "CF\nExpert")
    object CfUserTableHeader : AppStrings("CF\nUser", "CF\nUser")
    object CfSymptomTableHeader : AppStrings("CF\nGejala", "CF\nSymptom")
    object CfCombineTableHeader : AppStrings("CF\nCombine", "CF\nCombine")
    object FinalCfCombine : AppStrings("CF Combine Akhir:", "Final CF Combine:")
    object Formula : AppStrings("Rumus:", "Formula:")
    object CfFormulaSymptom : AppStrings("CF(gejala) = CF(pakar) × CF(user)", "CF(symptom) = CF(expert) × CF(user)")
    object CfFormulaCombine : AppStrings("CF(combine) = CF(lama) + CF(gejala) × (1 - CF(lama))", "CF(combine) = CF(old) + CF(symptom) × (1 - CF(old))")
    object Rank : AppStrings("#%d", "#%d")
    object FailedExportPdf : AppStrings("Gagal export PDF:", "Failed to export PDF:")
    object HypothesisWithSymptoms : AppStrings("Belum ada hipotesis dengan gejala", "No hypothesis with symptoms yet")
    
    // ===================================================
    // REPORTS
    // ===================================================
    object DiagnosisReports : AppStrings("Laporan Diagnosis", "Diagnosis Reports")
    object GenerateReport : AppStrings("Buat Laporan", "Generate Report")
    object ExportPdf : AppStrings("Ekspor PDF", "Export PDF")
    object ExportExcel : AppStrings("Ekspor Excel", "Export Excel")
    object FilterByDate : AppStrings("Filter Berdasarkan Tanggal", "Filter by Date")
    object NoReports : AppStrings("Belum ada laporan", "No reports")
    
    // ===================================================
    // ABOUT
    // ===================================================
    object AppName : AppStrings("Sistem Cerdas Vename", "Vename Smart System")
    object AboutApp : AppStrings("Tentang Aplikasi", "About Application")
    object AppVersion : AppStrings("Versi Aplikasi", "Application Version")
    object Developer : AppStrings("Pengembang", "Developer")
    object DeveloperTeam : AppStrings("Development Team 2026", "Development Team 2026")
    object AppDescription : AppStrings(
        "Sistem Cerdas Vename adalah platform pengelolaan data dan monitoring yang dirancang untuk membantu organisasi dalam mengoptimalkan proses bisnis secara real-time dengan teknologi terkini.",
        "Vename Smart System is a data management and monitoring platform designed to help organizations optimize business processes in real-time with the latest technology."
    )
    object Technology : AppStrings("Teknologi", "Technology")
    object Copyright : AppStrings("© 2026 Sistem Cerdas Vename", "© 2026 Vename Smart System")
    object TermsAndConditions : AppStrings("Syarat dan Ketentuan", "Terms and Conditions")
    object PrivacyPolicy : AppStrings("Kebijakan Privasi", "Privacy Policy")
    
    // ===================================================
    // COMMON ACTIONS
    // ===================================================
    object Save : AppStrings("Simpan", "Save")
    object Cancel : AppStrings("Batal", "Cancel")
    object Delete : AppStrings("Hapus", "Delete")
    object Edit : AppStrings("Edit", "Edit")
    object Add : AppStrings("Tambah", "Add")
    object Search : AppStrings("Cari", "Search")
    object Back : AppStrings("Kembali", "Back")
    object Yes : AppStrings("Ya", "Yes")
    object No : AppStrings("Tidak", "No")
    object Close : AppStrings("Tutup", "Close")
    object Submit : AppStrings("Kirim", "Submit")
    object Update : AppStrings("Perbarui", "Update")
    object Refresh : AppStrings("Segarkan", "Refresh")
    object Filter : AppStrings("Filter", "Filter")
    object Sort : AppStrings("Urutkan", "Sort")
    object View : AppStrings("Lihat", "View")
    object Download : AppStrings("Unduh", "Download")
    
    // ===================================================
    // MESSAGES & NOTIFICATIONS
    // ===================================================
    object Success : AppStrings("Berhasil", "Success")
    object Error : AppStrings("Kesalahan", "Error")
    object Warning : AppStrings("Peringatan", "Warning")
    object Info : AppStrings("Informasi", "Information")
    object Loading : AppStrings("Memuat...", "Loading...")
    object NoData : AppStrings("Tidak ada data", "No data")
    object DataSaved : AppStrings("Data berhasil disimpan", "Data saved successfully")
    object DataUpdated : AppStrings("Data berhasil diperbarui", "Data updated successfully")
    object DataDeleted : AppStrings("Data berhasil dihapus", "Data deleted successfully")
    object DeleteConfirmation : AppStrings("Apakah Anda yakin ingin menghapus?", "Are you sure you want to delete?")
    object FieldRequired : AppStrings("Kolom ini wajib diisi", "This field is required")
    object InvalidEmail : AppStrings("Email tidak valid", "Invalid email")
    object InvalidPassword : AppStrings("Kata sandi tidak valid", "Invalid password")
    object NetworkError : AppStrings("Kesalahan jaringan", "Network error")
    object UnknownError : AppStrings("Terjadi kesalahan yang tidak diketahui", "An unknown error occurred")
    
    // ===================================================
    // DATES & TIMES
    // ===================================================
    object Today : AppStrings("Hari Ini", "Today")
    object Yesterday : AppStrings("Kemarin", "Yesterday")
    object ThisWeek : AppStrings("Minggu Ini", "This Week")
    object ThisMonth : AppStrings("Bulan Ini", "This Month")
    object Date : AppStrings("Tanggal", "Date")
    object Time : AppStrings("Waktu", "Time")
    object StartDate : AppStrings("Tanggal Mulai", "Start Date")
    object EndDate : AppStrings("Tanggal Akhir", "End Date")
    
    // ===================================================
    // STATUS
    // ===================================================
    object Active : AppStrings("Aktif", "Active")
    object Inactive : AppStrings("Tidak Aktif", "Inactive")
    object Pending : AppStrings("Menunggu", "Pending")
    object Completed : AppStrings("Selesai", "Completed")
    object InProgress : AppStrings("Sedang Berlangsung", "In Progress")
    
    companion object {
        /**
         * Get string value based on current language
         * @param strings The AppStrings object
         * @param language Language code ("id" or "en")
         * @return Translated string
         */
        fun get(strings: AppStrings, language: String): String {
            return when (language) {
                "en" -> strings.en
                else -> strings.id // default to Indonesian
            }
        }
    }
}