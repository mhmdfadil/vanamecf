-- =====================================================
-- SISTEM CERDAS VENAME - Supabase Users Table
-- Jalankan SQL ini di Supabase SQL Editor
-- =====================================================

-- 1. Aktifkan ekstensi pgcrypto untuk hashing password
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- 2. Buat tabel users
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

-- 3. Buat index untuk email (mempercepat pencarian login)
CREATE INDEX IF NOT EXISTS idx_users_email ON public.users(email);

-- 4. Function untuk register user baru (hash password otomatis)
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
    -- Cek apakah email sudah terdaftar
    SELECT COUNT(*) INTO v_existing FROM public.users WHERE email = LOWER(TRIM(p_email));
    IF v_existing > 0 THEN
        RETURN json_build_object('success', FALSE, 'message', 'Email sudah terdaftar');
    END IF;

    -- Insert user baru dengan password di-hash menggunakan bcrypt
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

-- 5. Function untuk login (verifikasi email + password)
CREATE OR REPLACE FUNCTION public.login_user(
    p_email TEXT,
    p_password TEXT
)
RETURNS JSON AS $$
DECLARE
    v_user RECORD;
BEGIN
    -- Cari user berdasarkan email dan verifikasi password
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

    -- Update last login
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

-- 6. Enable RLS (Row Level Security)
ALTER TABLE public.users ENABLE ROW LEVEL SECURITY;

-- 7. Policy: Allow anon to call RPC functions only
CREATE POLICY "Allow read for authenticated" ON public.users
    FOR SELECT USING (TRUE);

-- 8. Insert sample admin user (password: admin123)
SELECT public.register_user('admin@vename.com', 'admin123', 'Administrator');

-- 9. Insert sample regular user (password: user123)
SELECT public.register_user('user@vename.com', 'user123', 'User Demo');


-- =====================================================

-- =====================================================
-- SISTEM CERDAS VENAME - Tabel Gejala
-- Jalankan SQL ini di Supabase SQL Editor
-- =====================================================

-- 1. Buat tabel gejalas
CREATE TABLE IF NOT EXISTS public.gejalas (
    id BIGSERIAL PRIMARY KEY,
    kode TEXT NOT NULL UNIQUE,
    nama TEXT NOT NULL,
    hipotesis_id BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 2. Index untuk pencarian cepat
CREATE INDEX IF NOT EXISTS idx_gejalas_kode ON public.gejalas(kode);
CREATE INDEX IF NOT EXISTS idx_gejalas_hipotesis ON public.gejalas(hipotesis_id);

-- 3. Enable RLS
ALTER TABLE public.gejalas ENABLE ROW LEVEL SECURITY;

-- 4. Policy: Allow all operations untuk authenticated & anon (sesuaikan nanti)
CREATE POLICY "Allow full access to gejalas" ON public.gejalas
    FOR ALL USING (TRUE) WITH CHECK (TRUE);

-- 5. Function: Auto-update updated_at
CREATE OR REPLACE FUNCTION public.update_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_gejalas_updated_at
    BEFORE UPDATE ON public.gejalas
    FOR EACH ROW
    EXECUTE FUNCTION public.update_updated_at();

-- 6. Insert sample data gejala
INSERT INTO public.gejalas (kode, nama, hipotesis_id) VALUES
    ('G001', 'Demam tinggi', 1)
ON CONFLICT (kode) DO NOTHING;

--- =====================================================

-- =====================================================
-- SISTEM CERDAS VENAME - Tabel Hipotesis
-- Jalankan SQL ini di Supabase SQL Editor
-- =====================================================

-- 1. Buat tabel hipotesiss
CREATE TABLE IF NOT EXISTS public.hipotesiss (
    id BIGSERIAL PRIMARY KEY,
    kode TEXT NOT NULL UNIQUE,
    nama TEXT NOT NULL,
    deskripsi TEXT,
    rekomendasi TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 2. Index
CREATE INDEX IF NOT EXISTS idx_hipotesiss_kode ON public.hipotesiss(kode);

-- 3. Enable RLS
ALTER TABLE public.hipotesiss ENABLE ROW LEVEL SECURITY;

-- 4. Policy
CREATE POLICY "Allow full access to hipotesiss" ON public.hipotesiss
    FOR ALL USING (TRUE) WITH CHECK (TRUE);

-- 5. Trigger auto-update updated_at (reuse function dari gejalas)
CREATE TRIGGER trigger_hipotesiss_updated_at
    BEFORE UPDATE ON public.hipotesiss
    FOR EACH ROW
    EXECUTE FUNCTION public.update_updated_at();

-- 6. Insert sample data hipotesis
INSERT INTO public.hipotesiss (kode, nama, deskripsi, rekomendasi) VALUES
    ('H001', 'COVID-19', 'Penyakit yang disebabkan oleh virus SARS-CoV-2', 'Isolasi mandiri, konsultasi dokter, tes PCR/Antigen'),
    ('H002', 'Influenza', 'Infeksi saluran pernapasan akut yang disebabkan virus influenza', 'Istirahat cukup, minum banyak cairan, obat pereda gejala')
ON CONFLICT (kode) DO NOTHING;

-- 7. Tambahkan foreign key di tabel gejalas ke hipotesiss
-- (Jalankan hanya jika belum ada FK, bisa skip jika error)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_gejalas_hipotesis'
    ) THEN
        ALTER TABLE public.gejalas
        ADD CONSTRAINT fk_gejalas_hipotesis
        FOREIGN KEY (hipotesis_id) REFERENCES public.hipotesiss(id)
        ON DELETE SET DEFAULT;
    END IF;
EXCEPTION WHEN OTHERS THEN
    RAISE NOTICE 'FK already exists or cannot be added: %', SQLERRM;
END $$;

UPDATE public.gejalas SET hipotesis_id = (SELECT id FROM public.hipotesiss WHERE kode = 'H001') WHERE kode IN ('G001');

-- =====================================================