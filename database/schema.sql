-- =====================================================
-- SISTEM CERDAS VANAME - Complete Database Schema
-- Jalankan SQL ini di Supabase SQL Editor (urut dari atas ke bawah)
-- =====================================================
-- PERUBAHAN UTAMA:
-- - Tabel gejalas TIDAK lagi punya kolom hipotesis_id
-- - Relasi gejala <-> hipotesis sekarang many-to-many via tabel gejala_hipotesis
-- - Tabel rules sekarang merujuk ke gejala_hipotesis (bukan gejalas langsung)
-- - Satu gejala bisa dipakai di banyak hipotesis dengan bobot CF berbeda
-- =====================================================


-- ===========================================
-- 0. EXTENSION & SHARED FUNCTION
-- ===========================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Function: Auto-update updated_at (shared semua tabel)
CREATE OR REPLACE FUNCTION public.update_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


-- ===========================================
-- 1. TABEL USERS
-- ===========================================

CREATE TABLE IF NOT EXISTS public.users (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    email TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    full_name TEXT NOT NULL DEFAULT '',
    role TEXT NOT NULL DEFAULT 'user',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_users_email ON public.users(email);

ALTER TABLE public.users ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Allow read for authenticated" ON public.users
    FOR SELECT USING (TRUE);

-- Function: Register user baru
CREATE OR REPLACE FUNCTION public.register_user(
    p_email TEXT,
    p_password TEXT,
    p_full_name TEXT DEFAULT ''
)
RETURNS JSON AS $$
DECLARE
    v_user_id UUID;
    v_existing INT;
BEGIN
    SELECT COUNT(*) INTO v_existing FROM public.users WHERE email = LOWER(TRIM(p_email));
    IF v_existing > 0 THEN
        RETURN json_build_object('success', FALSE, 'message', 'Email sudah terdaftar');
    END IF;

    INSERT INTO public.users (email, password_hash, full_name)
    VALUES (LOWER(TRIM(p_email)), crypt(p_password, gen_salt('bf', 10)), TRIM(p_full_name))
    RETURNING id INTO v_user_id;

    RETURN json_build_object(
        'success', TRUE,
        'message', 'Registrasi berhasil',
        'user_id', v_user_id
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Function: Login user
CREATE OR REPLACE FUNCTION public.login_user(
    p_email TEXT,
    p_password TEXT
)
RETURNS JSON AS $$
DECLARE
    v_user RECORD;
BEGIN
    SELECT id, email, full_name, role, is_active
    INTO v_user
    FROM public.users
    WHERE email = LOWER(TRIM(p_email))
      AND password_hash = crypt(p_password, password_hash);

    IF v_user IS NULL THEN
        RETURN json_build_object('success', FALSE, 'message', 'Email atau password salah');
    END IF;

    IF NOT v_user.is_active THEN
        RETURN json_build_object('success', FALSE, 'message', 'Akun tidak aktif');
    END IF;

    UPDATE public.users SET updated_at = NOW() WHERE id = v_user.id;

    RETURN json_build_object(
        'success', TRUE,
        'message', 'Login berhasil',
        'user_id', v_user.id,
        'email', v_user.email,
        'full_name', v_user.full_name,
        'role', v_user.role
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE TRIGGER trigger_users_updated_at
    BEFORE UPDATE ON public.users
    FOR EACH ROW
    EXECUTE FUNCTION public.update_updated_at();

-- Sample users
SELECT public.register_user('admin@vaname.com', 'admin123', 'Administrator');
SELECT public.register_user('user@vaname.com', 'user123', 'User Demo');

-- Set admin role
UPDATE public.users SET role = 'admin' WHERE email = 'admin@vaname.com';


-- ===========================================
-- 2. TABEL HIPOTESIS (buat duluan karena dirujuk)
-- ===========================================

CREATE TABLE IF NOT EXISTS public.hipotesiss (
    id BIGSERIAL PRIMARY KEY,
    kode TEXT NOT NULL UNIQUE,
    nama TEXT NOT NULL,
    deskripsi TEXT,
    rekomendasi TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_hipotesiss_kode ON public.hipotesiss(kode);

ALTER TABLE public.hipotesiss ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Allow full access to hipotesiss" ON public.hipotesiss
    FOR ALL USING (TRUE) WITH CHECK (TRUE);

CREATE TRIGGER trigger_hipotesiss_updated_at
    BEFORE UPDATE ON public.hipotesiss
    FOR EACH ROW
    EXECUTE FUNCTION public.update_updated_at();

-- Data hipotesis (13 penyakit udang vaname - Tabel 4.10)
INSERT INTO public.hipotesiss (kode, nama, deskripsi, rekomendasi) VALUES
    ('P01', 'Taura Syndrome Virus (TVS)',
     E'Penyakit ini disebabkan oleh infeksi kondisi Taura. Faktor munculnya penyakit ini yaitu karena lingkungan yang tidak dikelola dengan baik dan juga kualitas air yang buruk. Biasanya menginfeksi udang pada fase remaja ketika beratnya sekitar 0,15-5 gram atau dalam DoC 1-45 hari. Udang yang terinfeksi TSV akan menjadi lemah dan seluruh permukaan tubuh kemerahan, terutama kipas ekor. Ada juga bintik-bintik hitam pada bagian tubuh yang telah berubah warna. Selanjutnya, cangkang melunak, saluran pencernaan kosong, dan udang mungkin mati jika kondisinya parah.',
     E'1. Memilih benih udang bebas TSV (SPF – Specific Pathogen Free) untuk menekan virus\n2. Disinfeksi kolam sebelum tebar benur\n3. Monitoring kesehatan udang secara berkala\n4. Membuang udang yang sakit atau mati dari tambak untuk mencegah penularan penyakit melalui kanibalisme\n5. Menjaga kualitas air yang optimal dan nutrisi yang tepat\n6. Mencegah masuknya hewan liar (kepiting, burung, dll.) kedalam kolam\n7. Lakukan panen segera sebelum udang mengalami kematian atau mortalitas'),

    ('P02', 'Covert Mortality Nodavirus (CMNV)',
     E'Penyakit ini dapat terjadi sebulan setelah penebaran bibit dan tampaknya sulit bagi penambak untuk mengetahui kematian udang lebih awal karena udang akan mati di dasar tambak. Beberapa gejala awal penyakit ini adalah warna hepatopankreas memudar, saluran pencernaan udang kosong dan berwarna emas kecoklatan, kulit udang lunak, dan pertumbuhan udang lambat. Seringkali warna otot menjadi keputihan di bagian tengah tubuh.',
     E'1. Memilih bibit sehat dan bersertifikat serta bebas patogen\n2. Lakukan karantina benur sebelum ditebar\n3. Tingkatkan ketahanan udang dengan probiotik & pakan berkualitas\n4. Cegah masuknya hewan liar (kepiting, burung, dll.) kedalam kolam\n5. Singkirkan udang mati\n6. Pastikan sanitasi & kualitas air optimal'),

    ('P03', 'Yellow Head Virus (YHV)',
     E'Udang di tambak yang sudah tercemar penyakit kepala kuning ini, jika tidak segera diobati dapat menyebabkan tingkat kematian udang mencapai 100%. Ini akan terjadi secara bertahap selama lebih dari 3 hingga 5 hari, terutama untuk udang yang berumur antara 50 dan 60 hari. Efek samping yang muncul saat udang terkena penyakit ini, khususnya nafsu makan udang akan terlihat berkurang drastis dari yang diharapkan. Sehingga membuat perut udang terlihat tidak terisi dan warna tubuhnya menjadi pucat. Jika diperhatikan baik-baik, bagian atas udang dan hepatopankreas akan tampak kekuningan.',
     E'1. Memilih benih yang bersertifikasi bebas penyakit YHV\n2. Memantau kualitas air agar tetap di kondisi optimal\n3. Jangan mencampur udang dari kolam berbeda\n4. Keluarkan udang sakit dengan cepat\n5. Lakukan karantina dan uji PCR\n6. Terapkan biosekuriti dan pengawasan lingkungan secara ketat'),

    ('P04', 'White Feces Disease (WFD)',
     E'Disebabkan oleh bakteri Vibrio alginolyticus & Vibrio fluvalis serta protozoa parasit gregarin. Terlihat kotoran udang yang berwarna putih di permukaan tambak. Apalagi saat dilakukan pemeriksaan saluran pencernaan udang, akan ditemukan tubuh berbentuk ulat. Gejala lainnya yaitu udang berwarna gelap pada insang, nafsu makan udang menurun. Air kolam yang terlalu kotor menjadi salah satu penyebab munculnya penyakit berak putih ini pada udang. Biasanya ini terjadi 2 bulan setelah penebaran.',
     E'1. Menjaga kualitas air agar tetap stabil\n2. Gunakan benih sehat dan berkualitas\n3. Pemeriksaan rutin dan cepat tanggap terhadap gejala awal sangatlah penting untuk mencegah penyebaran penyakit lebih lanjut\n4. Berikan pakan berkualitas, tidak berlebihan dan bergizi seimbang\n5. Gunakan klorin atau hidrogen peroksida pada saat persiapan air\n6. Tingkatkan aerasi menggunakan kincir\n7. Kurangi penumpukan bahan organik dengan melakukan penggantian air'),

    ('P05', 'White Spot Syndrome Virus (WSSV)',
     E'Disebabkan oleh adanya infeksi hepatopancreatic parvovirus (HPV) dan monodon baculovirus (MBV), sehingga udang akan kehilangan nafsu makan dan pada akhirnya terjadi kematian massal. Ekor menjadi kemerahan. Jika udang terkena penyakit parah, bintik-bintik putih dengan lebar 0,5 hingga 2 mm akan muncul di kulit. Bintik putih akan menyebar ke bagian tubuh udang, antena patah dan mata rusak, hepatopankreas membesar, serta udang terlihat berenang di pinggiran tambak.',
     E'1. Memilih benur bebas WSSV (SPF)\n2. Lakukan desinfeksi kolam dan air masuk sebelum tebar\n3. Jangan masukkan air laut tanpa penyaringan & desinfeksi\n4. Menjaga kualitas lingkungan budidaya agar tidak menimbulkan stres bagi udang (suhu cukup > 29 °C)\n5. Perkuat biosekuriti dan pengawasan harian\n6. Panen dini jika kematian meningkat drastis\n7. Berikan unsur imunostimulan (misalnya suplementasi vitamin C pada pakan)'),

    ('P06', 'Infectious Myonecrosis Virus (IMNV)',
     E'Penyakit ini disebabkan oleh virus myonekrosis. Udang yang terserang penyakit ini biasanya akan menunjukkan efek samping klinis, yaitu terjadinya kerusakan jaringan otot pada tubuh udang dengan ciri otot yang terkena akan tampak putih. Selain itu, udang juga akan tampak pucat, dan kemudian menjadi kemerahan pada bagian ruas bawah hingga ekor udang.',
     E'1. Memilih benur/bibit bebas IMNV\n2. Hindari stres (jangan panen di siang terik dan hindari guncangan air)\n3. Pisahkan kolam udang sakit & sehat\n4. Menjaga kebersihan dan sanitasi ketat di fasilitas pemijahan dan kolam\n5. Jaga kualitas air tetap stabil\n6. Lakukan pergantian air\n7. Panen parsial (mengambil sebagian udang dari kolam) untuk menjaga kapasitas tambak\n8. Berikan pakan tambahan yang mengandung vitamin C, molase dan probiotik\n9. Berikan ramuan herbal yang terbuat dari bawang putih dan jambu biji.'),

    ('P07', 'Infectious Hypodermal And Hematopoetic Necrosis Virus (IHHNV)',
     E'Disebabkan oleh Parvovirus. Gejala Klinis: Nafsu makan menurun, pertumbuhan lambat, berenang di permukaan secara perlahan, hilang keseimbangan dan bergerak berputar dan selanjutnya tenggelam perlahan dalam posisi terbalik. Udang yang sekarat umumnya berwarna merah kecoklatan atau pink. Populasi udang dengan gejala-gejala tersebut umumnya akan mengalami laju kematian dalam tempo 3-10 hari. Penyakit IHHNV membuat udang menjadi kerdil dan berbagai kerusakan kulit kuku udang, terutama di daerah rostrum, antena, dada dan pinggang.',
     E'1. Memilih benur bersertifikat bebas IHHNV\n2. Hindari kepadatan tebar tinggi\n3. Sanitasi pada semua peralatan dan pekerja dalam semua tahap proses produksi\n4. Menjaga kebersihan & sanitasi lingkungan budidaya agar tidak menimbulkan stress bagi udang\n5. Jaga kestabilan lingkungan (kualitas air dan suhu)\n6. Cegah masuknya hewan liar dan vektor penyakit\n7. Hindari kualitas benur yang kurang baik'),

    ('P08', 'Early Mortality Syndrome (EMS)',
     E'Infeksi ini umumnya terjadi setelah udang berusia 20 hingga 30 hari. Gejala penyakit ini antara lain udang yang terlihat tidak berdaya dan tidak memiliki keinginan untuk bergerak dengan baik, rasa lapar yang berkurang, ukuran tubuh udang yang tidak seimbang/kepala udang yang lebih besar dari bentuk tubuh.',
     E'1. Memilih bibit bebas AHPND\n2. Pantau kesehatan organ hepatopankreas udang secara rutin\n3. Gunakan probiotik khusus untuk menekan Vibrio dan manajemen pakan ketat\n4. Hindari akumulasi bahan organik\n5. Mengatur kepadatan populasi dan kualitas air kolam\n6. Lakukan pemupukan air awal dengan tepat\n7. Lakukan penggantian air secara berkala'),

    ('P09', 'Chronic Softshell Syndrome atau Softshelling (CSS)',
     E'Penyakit ini menyebabkan cangkang udang menjadi lunak secara kronis sehingga udang tidak dapat melakukan pergantian kulit (molting) dengan baik. Pertumbuhan udang menjadi lambat dan cangkang tampak kehitaman kemerahan.',
     E'1. Memberi pakan kaya nutrisi untuk membantu pergantian kulit\n2. Hindari perubahan suhu/keasaman (pH) yang mendadak\n3. Gunakan pakan dan probiotik untuk memperkuat cangkang\n4. Kontrol kepadatan, berikan kalsium & mineral tambahan di air dan pakan (misalnya zeolite)\n5. Menjaga kondisi air dan tanah\n6. Manajemen pergantian air yang baik'),

    ('P10', 'Body Cramp (BC)',
     E'Diduga berkaitan dengan ketidakseimbangan mineral, atau peningkatan suhu air dan udara. Buruknya kualitas pakan juga dikaitkan dengan kram. Kram ditandai dengan kontraksi otot ekor dan terjadi abnormalitas fisiologis pada otot. Defisiensi vitamin B, ketidakseimbangan kalsium dan magnesium, oksigen rendah, perubahan salinitas, paparan atypical kation atau campuran anion diduga menjadi penyebab kram lainnya. Penyakit ini ditandai dengan pembengkokan rigid pada ekor (saat hidup) secara parsial atau keseluruhan.',
     E'1. Hindari guncangan air atau perubahan kualitas air mendadak\n2. Umumnya kekurangan Mg/Ca → kejang otot, solusinya dengan menambahkan mineral (Ca, Mg) melalui kapur, suplemen mineral dan pakan berkalsium\n3. Hindari kepadatan tebar dan kualitas air buruk\n4. Hindari fluktuasi suhu dan salinitas ekstrim\n5. Perhatikan kualitas air, terutama oksigen dan amonia'),

    ('P11', 'Black Gill Disease',
     E'Penyakit ini disebabkan oleh Mikroorganisme Fusarium dan Aspergillus flavus. Penyakit insang hitam ini terjadi karena kualitas air dan kondisi lingkungan yang buruk, konsentrasi DO yang rendah, pencemaran, dan padat tebar yang tidak sesuai dengan kapasitas kolam. Penyakit ini dapat menyebabkan masalah pernafasan pada udang yang mengarah pada kematian. Pada tahap awal, insang akan berwarna putih buram dan menjadi kuning atau kecoklatan. Pada infeksi taraf akut, insang berwarna coklat atau hitam disertai pengecilan dan kerusakan insang.',
     E'1. Menghindari tebar berlebihan\n2. Gunakan probiotik untuk menekan mikroorganisme patogen\n3. Jangan biarkan sisa pakan membusuk di dasar kolam\n4. Kontrol kualitas air dan bahan organik berlebih\n5. Lakukan pergantian air rutin\n6. Gunakan pakan yang mengandung asam askorbat diatas 2000mg/kg'),

    ('P12', 'Enterocytozoon Hepatopenaei (EHP)',
     E'Disebabkan oleh Mikrosporidia Enterocytozoon hepatopenaei. Gejala utama adalah perbedaan ukuran udang yang ekstrem dari satu tambak, nafsu makan menurun, pertumbuhan lambat, dan ujung ekor membengkak.',
     E'1. Gunakan benur bebas EHP\n2. Bersihkan dan sterilkan kolam sebelum tebar\n3. Cegah penggunaan pakan hidup (polychaete) yang bisa jadi pembawa penyakit\n4. Hindari kepadatan tebar dan pakan berlebih\n5. Menjaga air tetap bersih\n6. Gunakan disinfektan dan probiotik selama masa budidaya'),

    ('P13', 'Vibriosis',
     E'Penyakit ini disebabkan oleh bakteri Vibrio anguillarum. Udang akan kehilangan nafsu makan, kulit mengalami pemucatan, terjadi peradangan, dilanjutkan dengan kulit melepur dan borok. Gejala lain meliputi anggota tubuh tidak lengkap, cangkang hitam kemerahan, dan pembengkakan tubuh.',
     E'1. Gunakan probiotik anti-Vibrio\n2. Hindari kepadatan tebar dan pakan berlebih\n3. Jaga kualitas air, terutama suhu & pH\n4. Gunakan air bersih dan lakukan filtrasi serta desinfeksi\n5. Kontrol kadar organik dalam tambak\n6. Perbaiki aerasi\n7. Berikan unsur imunostimulan (misalnya suplementasi vitamin C pada pakan)')

ON CONFLICT (kode) DO NOTHING;

-- ===========================================
-- 3. TABEL GEJALA (tanpa hipotesis_id)
-- ===========================================

CREATE TABLE IF NOT EXISTS public.gejalas (
    id BIGSERIAL PRIMARY KEY,
    kode TEXT NOT NULL UNIQUE,
    nama TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_gejalas_kode ON public.gejalas(kode);

ALTER TABLE public.gejalas ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Allow full access to gejalas" ON public.gejalas
    FOR ALL USING (TRUE) WITH CHECK (TRUE);

CREATE TRIGGER trigger_gejalas_updated_at
    BEFORE UPDATE ON public.gejalas
    FOR EACH ROW
    EXECUTE FUNCTION public.update_updated_at();

-- Data gejala (30 gejala - Tabel 4.11)
INSERT INTO public.gejalas (kode, nama) VALUES
    ('G01', 'Ekor berwarna kemerahan'),
    ('G02', 'Kaki renang berwarna merah'),
    ('G03', 'Terdapat bintik hitam di cangkang'),
    ('G04', 'Cangkang lunak'),
    ('G05', 'Kepala dan dada berwarna kuning'),
    ('G06', 'Kematian massal'),
    ('G07', 'Perut tampak kosong'),
    ('G08', 'Insang kuning'),
    ('G09', 'Kotoran putih mengambang di permukaan air'),
    ('G10', 'Saluran pencernaan rusak dan membusuk'),
    ('G11', 'Udang berenang di permukaan'),
    ('G12', 'Terdapat bintik putih di cangkang'),
    ('G13', 'Tubuh kemerahan'),
    ('G14', 'Ekor berwarna putih'),
    ('G15', 'Tubuh pucat'),
    ('G16', 'Rostrum tampak putih'),
    ('G17', 'Udang tenggelam di dasar kolam'),
    ('G18', 'Tubuh kerdil'),
    ('G19', 'Kepala lebih besar dari tubuh'),
    ('G20', 'Udang lemah'),
    ('G21', 'Pertumbuhan lambat'),
    ('G22', 'Cangkang hitam kemerahan'),
    ('G23', 'Berenang tidak tentu arah'),
    ('G24', 'Tubuh kaku atau melengkung'),
    ('G25', 'Berenang lambat'),
    ('G26', 'Insang berwarna hitam'),
    ('G27', 'Nafsu makan menurun'),
    ('G28', 'Ujung ekor membengkak'),
    ('G29', 'Anggota tubuh tidak lengkap'),
    ('G30', 'Pembengkakan tubuh')
ON CONFLICT (kode) DO NOTHING;


-- ===========================================
-- 4. TABEL GEJALA_HIPOTESIS (many-to-many pivot)
--    Satu gejala bisa milik banyak hipotesis
--    Satu hipotesis bisa punya banyak gejala
-- ===========================================

CREATE TABLE IF NOT EXISTS public.gejala_hipotesis (
    id BIGSERIAL PRIMARY KEY,
    gejala_id BIGINT NOT NULL REFERENCES public.gejalas(id) ON DELETE CASCADE,
    hipotesis_id BIGINT NOT NULL REFERENCES public.hipotesiss(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(gejala_id, hipotesis_id)
);

CREATE INDEX IF NOT EXISTS idx_gh_gejala ON public.gejala_hipotesis(gejala_id);
CREATE INDEX IF NOT EXISTS idx_gh_hipotesis ON public.gejala_hipotesis(hipotesis_id);

ALTER TABLE public.gejala_hipotesis ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Allow full access to gejala_hipotesis" ON public.gejala_hipotesis
    FOR ALL USING (TRUE) WITH CHECK (TRUE);

CREATE TRIGGER trigger_gejala_hipotesis_updated_at
    BEFORE UPDATE ON public.gejala_hipotesis
    FOR EACH ROW
    EXECUTE FUNCTION public.update_updated_at();

-- -----------------------------------------------
-- R1: IF G01 AND G02 AND G03 THEN P01
-- -----------------------------------------------
INSERT INTO public.gejala_hipotesis (gejala_id, hipotesis_id) VALUES
    ((SELECT id FROM public.gejalas WHERE kode='G01'), (SELECT id FROM public.hipotesiss WHERE kode='P01')),
    ((SELECT id FROM public.gejalas WHERE kode='G02'), (SELECT id FROM public.hipotesiss WHERE kode='P01')),
    ((SELECT id FROM public.gejalas WHERE kode='G03'), (SELECT id FROM public.hipotesiss WHERE kode='P01'))
ON CONFLICT (gejala_id, hipotesis_id) DO NOTHING;

-- -----------------------------------------------
-- R2: IF G04 AND G05 AND G06 THEN P02
-- -----------------------------------------------
INSERT INTO public.gejala_hipotesis (gejala_id, hipotesis_id) VALUES
    ((SELECT id FROM public.gejalas WHERE kode='G04'), (SELECT id FROM public.hipotesiss WHERE kode='P02')),
    ((SELECT id FROM public.gejalas WHERE kode='G05'), (SELECT id FROM public.hipotesiss WHERE kode='P02')),
    ((SELECT id FROM public.gejalas WHERE kode='G06'), (SELECT id FROM public.hipotesiss WHERE kode='P02'))
ON CONFLICT (gejala_id, hipotesis_id) DO NOTHING;

-- -----------------------------------------------
-- R3: IF G05 AND G07 AND G08 THEN P03
-- G05 juga ada di P02 (shared)
-- -----------------------------------------------
INSERT INTO public.gejala_hipotesis (gejala_id, hipotesis_id) VALUES
    ((SELECT id FROM public.gejalas WHERE kode='G05'), (SELECT id FROM public.hipotesiss WHERE kode='P03')),
    ((SELECT id FROM public.gejalas WHERE kode='G07'), (SELECT id FROM public.hipotesiss WHERE kode='P03')),
    ((SELECT id FROM public.gejalas WHERE kode='G08'), (SELECT id FROM public.hipotesiss WHERE kode='P03'))
ON CONFLICT (gejala_id, hipotesis_id) DO NOTHING;

-- -----------------------------------------------
-- R4: IF G04 AND G09 AND G10 THEN P04
-- G04 juga ada di P02 (shared)
-- -----------------------------------------------
INSERT INTO public.gejala_hipotesis (gejala_id, hipotesis_id) VALUES
    ((SELECT id FROM public.gejalas WHERE kode='G04'), (SELECT id FROM public.hipotesiss WHERE kode='P04')),
    ((SELECT id FROM public.gejalas WHERE kode='G09'), (SELECT id FROM public.hipotesiss WHERE kode='P04')),
    ((SELECT id FROM public.gejalas WHERE kode='G10'), (SELECT id FROM public.hipotesiss WHERE kode='P04'))
ON CONFLICT (gejala_id, hipotesis_id) DO NOTHING;

-- -----------------------------------------------
-- R5: IF G11 AND G12 AND G13 THEN P05
-- -----------------------------------------------
INSERT INTO public.gejala_hipotesis (gejala_id, hipotesis_id) VALUES
    ((SELECT id FROM public.gejalas WHERE kode='G11'), (SELECT id FROM public.hipotesiss WHERE kode='P05')),
    ((SELECT id FROM public.gejalas WHERE kode='G12'), (SELECT id FROM public.hipotesiss WHERE kode='P05')),
    ((SELECT id FROM public.gejalas WHERE kode='G13'), (SELECT id FROM public.hipotesiss WHERE kode='P05'))
ON CONFLICT (gejala_id, hipotesis_id) DO NOTHING;

-- -----------------------------------------------
-- R6: IF G01 AND G14 AND G15 THEN P06
-- G01 juga ada di P01 (shared)
-- -----------------------------------------------
INSERT INTO public.gejala_hipotesis (gejala_id, hipotesis_id) VALUES
    ((SELECT id FROM public.gejalas WHERE kode='G01'), (SELECT id FROM public.hipotesiss WHERE kode='P06')),
    ((SELECT id FROM public.gejalas WHERE kode='G14'), (SELECT id FROM public.hipotesiss WHERE kode='P06')),
    ((SELECT id FROM public.gejalas WHERE kode='G15'), (SELECT id FROM public.hipotesiss WHERE kode='P06'))
ON CONFLICT (gejala_id, hipotesis_id) DO NOTHING;

-- -----------------------------------------------
-- R7: IF G16 AND G17 AND G18 THEN P07
-- -----------------------------------------------
INSERT INTO public.gejala_hipotesis (gejala_id, hipotesis_id) VALUES
    ((SELECT id FROM public.gejalas WHERE kode='G16'), (SELECT id FROM public.hipotesiss WHERE kode='P07')),
    ((SELECT id FROM public.gejalas WHERE kode='G17'), (SELECT id FROM public.hipotesiss WHERE kode='P07')),
    ((SELECT id FROM public.gejalas WHERE kode='G18'), (SELECT id FROM public.hipotesiss WHERE kode='P07'))
ON CONFLICT (gejala_id, hipotesis_id) DO NOTHING;

-- -----------------------------------------------
-- R8: IF G04 AND G19 AND G20 THEN P08
-- G04 juga ada di P02, P04 (shared)
-- -----------------------------------------------
INSERT INTO public.gejala_hipotesis (gejala_id, hipotesis_id) VALUES
    ((SELECT id FROM public.gejalas WHERE kode='G04'), (SELECT id FROM public.hipotesiss WHERE kode='P08')),
    ((SELECT id FROM public.gejalas WHERE kode='G19'), (SELECT id FROM public.hipotesiss WHERE kode='P08')),
    ((SELECT id FROM public.gejalas WHERE kode='G20'), (SELECT id FROM public.hipotesiss WHERE kode='P08'))
ON CONFLICT (gejala_id, hipotesis_id) DO NOTHING;

-- -----------------------------------------------
-- R9: IF G04 AND G21 AND G22 THEN P09
-- G04 juga ada di P02, P04, P08 (shared)
-- -----------------------------------------------
INSERT INTO public.gejala_hipotesis (gejala_id, hipotesis_id) VALUES
    ((SELECT id FROM public.gejalas WHERE kode='G04'), (SELECT id FROM public.hipotesiss WHERE kode='P09')),
    ((SELECT id FROM public.gejalas WHERE kode='G21'), (SELECT id FROM public.hipotesiss WHERE kode='P09')),
    ((SELECT id FROM public.gejalas WHERE kode='G22'), (SELECT id FROM public.hipotesiss WHERE kode='P09'))
ON CONFLICT (gejala_id, hipotesis_id) DO NOTHING;

-- -----------------------------------------------
-- R10: IF G14 AND G23 AND G24 THEN P10
-- G14 juga ada di P06 (shared)
-- -----------------------------------------------
INSERT INTO public.gejala_hipotesis (gejala_id, hipotesis_id) VALUES
    ((SELECT id FROM public.gejalas WHERE kode='G14'), (SELECT id FROM public.hipotesiss WHERE kode='P10')),
    ((SELECT id FROM public.gejalas WHERE kode='G23'), (SELECT id FROM public.hipotesiss WHERE kode='P10')),
    ((SELECT id FROM public.gejalas WHERE kode='G24'), (SELECT id FROM public.hipotesiss WHERE kode='P10'))
ON CONFLICT (gejala_id, hipotesis_id) DO NOTHING;

-- -----------------------------------------------
-- R11: IF G20 AND G25 AND G26 THEN P11
-- G20 juga ada di P08 (shared)
-- -----------------------------------------------
INSERT INTO public.gejala_hipotesis (gejala_id, hipotesis_id) VALUES
    ((SELECT id FROM public.gejalas WHERE kode='G20'), (SELECT id FROM public.hipotesiss WHERE kode='P11')),
    ((SELECT id FROM public.gejalas WHERE kode='G25'), (SELECT id FROM public.hipotesiss WHERE kode='P11')),
    ((SELECT id FROM public.gejalas WHERE kode='G26'), (SELECT id FROM public.hipotesiss WHERE kode='P11'))
ON CONFLICT (gejala_id, hipotesis_id) DO NOTHING;

-- -----------------------------------------------
-- R12: IF G21 AND G27 AND G28 THEN P12
-- G21 juga ada di P09 (shared)
-- -----------------------------------------------
INSERT INTO public.gejala_hipotesis (gejala_id, hipotesis_id) VALUES
    ((SELECT id FROM public.gejalas WHERE kode='G21'), (SELECT id FROM public.hipotesiss WHERE kode='P12')),
    ((SELECT id FROM public.gejalas WHERE kode='G27'), (SELECT id FROM public.hipotesiss WHERE kode='P12')),
    ((SELECT id FROM public.gejalas WHERE kode='G28'), (SELECT id FROM public.hipotesiss WHERE kode='P12'))
ON CONFLICT (gejala_id, hipotesis_id) DO NOTHING;

-- -----------------------------------------------
-- R13: IF G22 AND G29 AND G30 THEN P13
-- G22 juga ada di P09 (shared)
-- -----------------------------------------------
INSERT INTO public.gejala_hipotesis (gejala_id, hipotesis_id) VALUES
    ((SELECT id FROM public.gejalas WHERE kode='G22'), (SELECT id FROM public.hipotesiss WHERE kode='P13')),
    ((SELECT id FROM public.gejalas WHERE kode='G29'), (SELECT id FROM public.hipotesiss WHERE kode='P13')),
    ((SELECT id FROM public.gejalas WHERE kode='G30'), (SELECT id FROM public.hipotesiss WHERE kode='P13'))
ON CONFLICT (gejala_id, hipotesis_id) DO NOTHING;

-- ===========================================
-- 5. TABEL NILAI CF (Certainty Factor user)
-- ===========================================

CREATE TABLE IF NOT EXISTS public.nilai_cfs (
    id BIGSERIAL PRIMARY KEY,
    keterangan TEXT NOT NULL,
    nilai NUMERIC(3, 2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE public.nilai_cfs ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Allow full access to nilai_cfs" ON public.nilai_cfs
    FOR ALL USING (TRUE) WITH CHECK (TRUE);

CREATE TRIGGER trigger_nilai_cfs_updated_at
    BEFORE UPDATE ON public.nilai_cfs
    FOR EACH ROW
    EXECUTE FUNCTION public.update_updated_at();

-- Function: Recalculate semua nilai CF merata (0.00 - 1.00)
CREATE OR REPLACE FUNCTION public.recalculate_nilai_cf()
RETURNS VOID AS $$
DECLARE
    total_rows INT;
    row_index INT := 0;
    rec RECORD;
    step_value NUMERIC(3,2);
BEGIN
    SELECT COUNT(*) INTO total_rows FROM public.nilai_cfs;

    IF total_rows = 0 THEN RETURN; END IF;

    IF total_rows = 1 THEN
        UPDATE public.nilai_cfs SET nilai = 1.00;
        RETURN;
    END IF;

    step_value := ROUND(1.00 / (total_rows - 1), 2);

    FOR rec IN SELECT id FROM public.nilai_cfs ORDER BY id ASC
    LOOP
        UPDATE public.nilai_cfs
        SET nilai = LEAST(ROUND(row_index * step_value, 2), 1.00)
        WHERE id = rec.id;
        row_index := row_index + 1;
    END LOOP;
END;
$$ LANGUAGE plpgsql;

-- Data nilai CF (6 tingkat keyakinan)
INSERT INTO public.nilai_cfs (keterangan, nilai) VALUES
    ('Tidak', 0.00),
    ('Tidak Tahu', 0.20),
    ('Kurang Yakin', 0.40),
    ('Cukup Yakin', 0.60),
    ('Yakin', 0.80),
    ('Sangat Yakin', 1.00);


-- ===========================================
-- 6. TABEL RULES
--    Sekarang merujuk ke gejala_hipotesis (bukan gejalas)
--    Artinya: rule = "gejala X untuk hipotesis Y" punya CF pakar tertentu
-- ===========================================

CREATE TABLE IF NOT EXISTS public.rules (
    id BIGSERIAL PRIMARY KEY,
    gejala_hipotesis_id BIGINT NOT NULL REFERENCES public.gejala_hipotesis(id) ON DELETE CASCADE,
    cf_id BIGINT NOT NULL REFERENCES public.nilai_cfs(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_rules_gh ON public.rules(gejala_hipotesis_id);
CREATE INDEX IF NOT EXISTS idx_rules_cf ON public.rules(cf_id);

-- Satu pasangan gejala-hipotesis hanya punya 1 rule
CREATE UNIQUE INDEX IF NOT EXISTS idx_rules_gh_unique ON public.rules(gejala_hipotesis_id);

ALTER TABLE public.rules ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Allow full access to rules" ON public.rules
    FOR ALL USING (TRUE) WITH CHECK (TRUE);

CREATE TRIGGER trigger_rules_updated_at
    BEFORE UPDATE ON public.rules
    FOR EACH ROW
    EXECUTE FUNCTION public.update_updated_at();


-- ===========================================
-- 7. TABEL KUESIONER
-- ===========================================

CREATE TABLE IF NOT EXISTS public.kuesioners (
    id BIGSERIAL PRIMARY KEY,
    nama_petambak TEXT NOT NULL,
    no_hp TEXT NOT NULL DEFAULT '',
    lokasi_tambak TEXT NOT NULL DEFAULT '',
    usia_udang INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_kuesioners_nama ON public.kuesioners(nama_petambak);

ALTER TABLE public.kuesioners ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Allow full access to kuesioners" ON public.kuesioners
    FOR ALL USING (TRUE) WITH CHECK (TRUE);

CREATE TRIGGER trigger_kuesioners_updated_at
    BEFORE UPDATE ON public.kuesioners
    FOR EACH ROW
    EXECUTE FUNCTION public.update_updated_at();


-- ===========================================
-- 8. TABEL KUESIONER DATA (jawaban user)
--    Sekarang merujuk ke gejala_hipotesis
-- ===========================================

CREATE TABLE IF NOT EXISTS public.kuesioner_data (
    id BIGSERIAL PRIMARY KEY,
    kuesioner_id BIGINT NOT NULL REFERENCES public.kuesioners(id) ON DELETE CASCADE,
    gejala_hipotesis_id BIGINT NOT NULL REFERENCES public.gejala_hipotesis(id) ON DELETE CASCADE,
    cf_value BIGINT NOT NULL REFERENCES public.nilai_cfs(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_kuesioner_data_kuesioner ON public.kuesioner_data(kuesioner_id);
CREATE INDEX IF NOT EXISTS idx_kuesioner_data_gh ON public.kuesioner_data(gejala_hipotesis_id);

ALTER TABLE public.kuesioner_data ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Allow full access to kuesioner_data" ON public.kuesioner_data
    FOR ALL USING (TRUE) WITH CHECK (TRUE);

CREATE TRIGGER trigger_kuesioner_data_updated_at
    BEFORE UPDATE ON public.kuesioner_data
    FOR EACH ROW
    EXECUTE FUNCTION public.update_updated_at();


-- ===========================================
-- CONTOH QUERY BERGUNA
-- ===========================================

-- Lihat semua gejala beserta hipotesis terkait:
-- SELECT g.kode AS gejala_kode, g.nama AS gejala,
--        h.kode AS hipotesis_kode, h.nama AS hipotesis
-- FROM public.gejala_hipotesis gh
-- JOIN public.gejalas g ON g.id = gh.gejala_id
-- JOIN public.hipotesiss h ON h.id = gh.hipotesis_id
-- ORDER BY g.kode, h.kode;

-- Lihat gejala yang shared antar beberapa hipotesis:
-- SELECT g.kode, g.nama, COUNT(gh.hipotesis_id) AS jumlah_hipotesis
-- FROM public.gejalas g
-- JOIN public.gejala_hipotesis gh ON g.id = gh.gejala_id
-- GROUP BY g.id, g.kode, g.nama
-- HAVING COUNT(gh.hipotesis_id) > 1
-- ORDER BY jumlah_hipotesis DESC;

-- Lihat rules lengkap (gejala + hipotesis + nilai CF pakar):
-- SELECT g.kode AS gejala_kode, g.nama AS gejala,
--        h.kode AS hipotesis_kode, h.nama AS hipotesis,
--        nc.keterangan AS cf_label, nc.nilai AS cf_pakar
-- FROM public.rules r
-- JOIN public.gejala_hipotesis gh ON gh.id = r.gejala_hipotesis_id
-- JOIN public.gejalas g ON g.id = gh.gejala_id
-- JOIN public.hipotesiss h ON h.id = gh.hipotesis_id
-- JOIN public.nilai_cfs nc ON nc.id = r.cf_id
-- ORDER BY h.kode, g.kode;