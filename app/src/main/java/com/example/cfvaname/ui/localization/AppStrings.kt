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
    
    // ===================================================
    // DATA GEJALA (SYMPTOMS)
    // ===================================================
    object DataGejala : AppStrings("Data Gejala", "Symptoms Data")
    object AddSymptom : AppStrings("Tambah Gejala", "Add Symptom")
    object EditSymptom : AppStrings("Edit Gejala", "Edit Symptom")
    object DeleteSymptom : AppStrings("Hapus Gejala", "Delete Symptom")
    object SymptomCode : AppStrings("Kode Gejala", "Symptom Code")
    object SymptomName : AppStrings("Nama Gejala", "Symptom Name")
    object SymptomDescription : AppStrings("Deskripsi Gejala", "Symptom Description")
    object NoSymptoms : AppStrings("Belum ada data gejala", "No symptoms data")
    object SearchSymptom : AppStrings("Cari gejala...", "Search symptom...")
    
    // ===================================================
    // DATA HIPOTESIS (HYPOTHESIS)
    // ===================================================
    object DataHipotesis : AppStrings("Data Hipotesis", "Hypothesis Data")
    object AddHypothesis : AppStrings("Tambah Hipotesis", "Add Hypothesis")
    object EditHypothesis : AppStrings("Edit Hipotesis", "Edit Hypothesis")
    object DeleteHypothesis : AppStrings("Hapus Hipotesis", "Delete Hypothesis")
    object HypothesisCode : AppStrings("Kode Hipotesis", "Hypothesis Code")
    object HypothesisName : AppStrings("Nama Hipotesis", "Hypothesis Name")
    object HypothesisDescription : AppStrings("Deskripsi Hipotesis", "Hypothesis Description")
    object NoHypothesis : AppStrings("Belum ada data hipotesis", "No hypothesis data")
    object SearchHypothesis : AppStrings("Cari hipotesis...", "Search hypothesis...")
    
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
    object AboutApp : AppStrings("Tentang Aplikasi", "About Application")
    object AppVersion : AppStrings("Versi Aplikasi", "Application Version")
    object Developer : AppStrings("Pengembang", "Developer")
    object Description : AppStrings("Deskripsi", "Description")
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