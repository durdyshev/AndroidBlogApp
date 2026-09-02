-- ===================================================
-- AURA DATING & SOCIAL DISCOVERY - LOCATIONS & SEARCH
-- Migration: 06_locations_and_search.sql
-- ===================================================

-- 1. Countries Table
CREATE TABLE IF NOT EXISTS public.countries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL,
    code TEXT,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- 2. Regions Table (Provinces / States / Welayats)
CREATE TABLE IF NOT EXISTS public.regions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    country_id UUID NOT NULL REFERENCES public.countries(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- 3. Cities Table (Cities / Etraps)
CREATE TABLE IF NOT EXISTS public.cities (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    region_id UUID NOT NULL REFERENCES public.regions(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- Indexes for fast hierarchical location lookups
CREATE INDEX IF NOT EXISTS idx_regions_country_id ON public.regions(country_id);
CREATE INDEX IF NOT EXISTS idx_cities_region_id ON public.cities(region_id);

-- 4. Add Home Location Columns to Profiles Table
ALTER TABLE public.profiles
ADD COLUMN IF NOT EXISTS country_id UUID REFERENCES public.countries(id) ON DELETE SET NULL,
ADD COLUMN IF NOT EXISTS region_id UUID REFERENCES public.regions(id) ON DELETE SET NULL,
ADD COLUMN IF NOT EXISTS city_id UUID REFERENCES public.cities(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_profiles_location_hierarchy ON public.profiles(country_id, region_id, city_id);

-- 5. Row Level Security for Location Catalogs (Public Read)
ALTER TABLE public.countries ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.regions ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.cities ENABLE ROW LEVEL SECURITY;

GRANT SELECT ON public.countries TO anon, authenticated;
GRANT SELECT ON public.regions TO anon, authenticated;
GRANT SELECT ON public.cities TO anon, authenticated;

DROP POLICY IF EXISTS "Public read countries" ON public.countries;
CREATE POLICY "Public read countries" ON public.countries FOR SELECT TO anon, authenticated USING (true);

DROP POLICY IF EXISTS "Public read regions" ON public.regions;
CREATE POLICY "Public read regions" ON public.regions FOR SELECT TO anon, authenticated USING (true);

DROP POLICY IF EXISTS "Public read cities" ON public.cities;
CREATE POLICY "Public read cities" ON public.cities FOR SELECT TO anon, authenticated USING (true);

-- 6. Server-Side Location-Based Search RPC Function
CREATE OR REPLACE FUNCTION public.search_candidates_by_location(
    p_country_id UUID DEFAULT NULL,
    p_region_id UUID DEFAULT NULL,
    p_city_id UUID DEFAULT NULL,
    p_min_age INT DEFAULT 18,
    p_max_age INT DEFAULT 100,
    p_gender TEXT DEFAULT 'ALL',
    p_limit INT DEFAULT 20,
    p_offset INT DEFAULT 0
)
RETURNS TABLE (
    id UUID,
    display_name TEXT,
    birth_date DATE,
    gender TEXT,
    bio TEXT,
    distance_km DOUBLE PRECISION,
    is_online BOOLEAN,
    last_seen_at TIMESTAMPTZ,
    country_name TEXT,
    region_name TEXT,
    city_name TEXT,
    photos JSONB,
    interests JSONB
) AS $$
DECLARE
    v_user_id UUID := auth.uid();
    v_my_lat DOUBLE PRECISION;
    v_my_lon DOUBLE PRECISION;
BEGIN
    IF v_user_id IS NOT NULL THEN
        SELECT latitude, longitude INTO v_my_lat, v_my_lon FROM public.profiles WHERE profiles.id = v_user_id;
    END IF;

    RETURN QUERY
    WITH candidate_profiles AS (
        SELECT
            p.id,
            p.display_name,
            p.birth_date,
            p.gender,
            p.bio,
            p.is_online,
            p.last_seen_at,
            co.name AS country_name,
            rg.name AS region_name,
            ct.name AS city_name,
            CASE 
                WHEN v_my_lat IS NOT NULL AND p.latitude IS NOT NULL 
                THEN public.calculate_distance_km(v_my_lat, v_my_lon, p.latitude, p.longitude)
                ELSE NULL
            END AS dist
        FROM public.profiles p
        LEFT JOIN public.countries co ON co.id = p.country_id
        LEFT JOIN public.regions rg ON rg.id = p.region_id
        LEFT JOIN public.cities ct ON ct.id = p.city_id
        WHERE (v_user_id IS NULL OR p.id != v_user_id)
          AND p.is_banned = false
          -- Location hierarchy matching
          AND (p_country_id IS NULL OR p.country_id = p_country_id)
          AND (p_region_id IS NULL OR p.region_id = p_region_id)
          AND (p_city_id IS NULL OR p.city_id = p_city_id)
          -- Exclude blocked users (either direction)
          AND (
              v_user_id IS NULL 
              OR NOT EXISTS (
                  SELECT 1 FROM public.blocks b
                  WHERE (b.blocker_id = v_user_id AND b.blocked_id = p.id)
                     OR (b.blocker_id = p.id AND b.blocked_id = v_user_id)
              )
          )
          -- Filter Gender Preference
          AND (
              p_gender IS NULL
              OR p_gender = 'ALL'
              OR p_gender = 'EVERYONE'
              OR (p_gender IN ('WOMEN', 'WOMAN') AND p.gender = 'WOMAN')
              OR (p_gender IN ('MEN', 'MAN') AND p.gender = 'MAN')
              OR (p_gender = 'NON_BINARY' AND p.gender = 'NON_BINARY')
          )
          -- Filter Age Preference (Adults 18+ only)
          AND (
              DATE_PART('year', age(p.birth_date)) >= GREATEST(COALESCE(p_min_age, 18), 18)
              AND DATE_PART('year', age(p.birth_date)) <= COALESCE(p_max_age, 100)
          )
    )
    SELECT
        cp.id,
        cp.display_name,
        cp.birth_date,
        cp.gender,
        cp.bio,
        cp.dist AS distance_km,
        cp.is_online,
        cp.last_seen_at,
        cp.country_name,
        cp.region_name,
        cp.city_name,
        COALESCE(
            (
                SELECT jsonb_agg(
                    jsonb_build_object(
                        'id', ph.id,
                        'photo_url', ph.photo_url,
                        'storage_path', ph.storage_path,
                        'display_order', ph.display_order,
                        'is_primary', ph.is_primary
                    ) ORDER BY ph.display_order
                )
                FROM public.profile_photos ph
                WHERE ph.user_id = cp.id
            ),
            '[]'::jsonb
        ) AS photos,
        COALESCE(
            (
                SELECT jsonb_agg(
                    jsonb_build_object(
                        'id', i.id,
                        'name', i.name,
                        'category', i.category,
                        'icon', i.icon
                    )
                )
                FROM public.user_interests ui
                JOIN public.interests i ON i.id = ui.interest_id
                WHERE ui.user_id = cp.id
            ),
            '[]'::jsonb
        ) AS interests
    FROM candidate_profiles cp
    ORDER BY cp.is_online DESC, cp.last_seen_at DESC NULLS LAST, cp.id
    LIMIT p_limit OFFSET p_offset;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 7. Seed Initial Location Data (Turkmenistan, Turkey, Germany, Kazakhstan, Uzbekistan)

-- Countries
INSERT INTO public.countries (id, name, code) VALUES
    ('11111111-1111-1111-1111-111111111101', 'Turkmenistan', 'TM'),
    ('11111111-1111-1111-1111-111111111102', 'Turkey', 'TR'),
    ('11111111-1111-1111-1111-111111111103', 'Germany', 'DE'),
    ('11111111-1111-1111-1111-111111111104', 'Kazakhstan', 'KZ'),
    ('11111111-1111-1111-1111-111111111105', 'Uzbekistan', 'UZ')
ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, code = EXCLUDED.code;

-- Regions: Turkmenistan
INSERT INTO public.regions (id, country_id, name) VALUES
    ('22222222-2222-2222-2222-222222222201', '11111111-1111-1111-1111-111111111101', 'Mary'),
    ('22222222-2222-2222-2222-222222222202', '11111111-1111-1111-1111-111111111101', 'Ahal'),
    ('22222222-2222-2222-2222-222222222203', '11111111-1111-1111-1111-111111111101', 'Balkan'),
    ('22222222-2222-2222-2222-222222222204', '11111111-1111-1111-1111-111111111101', 'Dashoguz'),
    ('22222222-2222-2222-2222-222222222205', '11111111-1111-1111-1111-111111111101', 'Lebap')
ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name;

-- Cities: Mary, Turkmenistan
INSERT INTO public.cities (region_id, name) VALUES
    ('22222222-2222-2222-2222-222222222201', 'Mary'),
    ('22222222-2222-2222-2222-222222222201', 'Bayramaly'),
    ('22222222-2222-2222-2222-222222222201', 'Yoloten'),
    ('22222222-2222-2222-2222-222222222201', 'Serhetabat'),
    ('22222222-2222-2222-2222-222222222201', 'Murgap'),
    ('22222222-2222-2222-2222-222222222201', 'Sakarçäge'),
    ('22222222-2222-2222-2222-222222222201', 'Tagtabazar')
ON CONFLICT DO NOTHING;

-- Cities: Ahal & Ashgabat, Turkmenistan
INSERT INTO public.cities (region_id, name) VALUES
    ('22222222-2222-2222-2222-222222222202', 'Ashgabat'),
    ('22222222-2222-2222-2222-222222222202', 'Anau'),
    ('22222222-2222-2222-2222-222222222202', 'Baharly'),
    ('22222222-2222-2222-2222-222222222202', 'Gökdepe'),
    ('22222222-2222-2222-2222-222222222202', 'Kaka'),
    ('22222222-2222-2222-2222-222222222202', 'Tejen')
ON CONFLICT DO NOTHING;

-- Cities: Balkan, Turkmenistan
INSERT INTO public.cities (region_id, name) VALUES
    ('22222222-2222-2222-2222-222222222203', 'Balkanabat'),
    ('22222222-2222-2222-2222-222222222203', 'Turkmenbashi'),
    ('22222222-2222-2222-2222-222222222203', 'Hazar'),
    ('22222222-2222-2222-2222-222222222203', 'Gumdag'),
    ('22222222-2222-2222-2222-222222222203', 'Bereket'),
    ('22222222-2222-2222-2222-222222222203', 'Serdar')
ON CONFLICT DO NOTHING;

-- Cities: Dashoguz, Turkmenistan
INSERT INTO public.cities (region_id, name) VALUES
    ('22222222-2222-2222-2222-222222222204', 'Dashoguz'),
    ('22222222-2222-2222-2222-222222222204', 'Köneürgenç'),
    ('22222222-2222-2222-2222-222222222204', 'Akdepe'),
    ('22222222-2222-2222-2222-222222222204', 'Boldumsaz'),
    ('22222222-2222-2222-2222-222222222204', 'Görogly'),
    ('22222222-2222-2222-2222-222222222204', 'Gubadag')
ON CONFLICT DO NOTHING;

-- Cities: Lebap, Turkmenistan
INSERT INTO public.cities (region_id, name) VALUES
    ('22222222-2222-2222-2222-222222222205', 'Turkmenabat'),
    ('22222222-2222-2222-2222-222222222205', 'Kerki'),
    ('22222222-2222-2222-2222-222222222205', 'Magdanly'),
    ('22222222-2222-2222-2222-222222222205', 'Gazojak'),
    ('22222222-2222-2222-2222-222222222205', 'Saýat'),
    ('22222222-2222-2222-2222-222222222205', 'Darganata')
ON CONFLICT DO NOTHING;

-- Regions: Turkey
INSERT INTO public.regions (id, country_id, name) VALUES
    ('22222222-2222-2222-2222-222222222211', '11111111-1111-1111-1111-111111111102', 'Istanbul'),
    ('22222222-2222-2222-2222-222222222212', '11111111-1111-1111-1111-111111111102', 'Ankara'),
    ('22222222-2222-2222-2222-222222222213', '11111111-1111-1111-1111-111111111102', 'Izmir'),
    ('22222222-2222-2222-2222-222222222214', '11111111-1111-1111-1111-111111111102', 'Antalya')
ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name;

-- Cities: Turkey
INSERT INTO public.cities (region_id, name) VALUES
    ('22222222-2222-2222-2222-222222222211', 'Kadikoy'),
    ('22222222-2222-2222-2222-222222222211', 'Besiktas'),
    ('22222222-2222-2222-2222-222222222211', 'Sisli'),
    ('22222222-2222-2222-2222-222222222211', 'Uskudar'),
    ('22222222-2222-2222-2222-222222222212', 'Cankaya'),
    ('22222222-2222-2222-2222-222222222212', 'Kecioren'),
    ('22222222-2222-2222-2222-222222222213', 'Konak'),
    ('22222222-2222-2222-2222-222222222213', 'Karsiyaka'),
    ('22222222-2222-2222-2222-222222222214', 'Muratpasa'),
    ('22222222-2222-2222-2222-222222222214', 'Konyaalti')
ON CONFLICT DO NOTHING;

-- Regions: Germany
INSERT INTO public.regions (id, country_id, name) VALUES
    ('22222222-2222-2222-2222-222222222221', '11111111-1111-1111-1111-111111111103', 'Berlin'),
    ('22222222-2222-2222-2222-222222222222', '11111111-1111-1111-1111-111111111103', 'Bavaria')
ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name;

-- Cities: Germany
INSERT INTO public.cities (region_id, name) VALUES
    ('22222222-2222-2222-2222-222222222221', 'Berlin'),
    ('22222222-2222-2222-2222-222222222222', 'Munich'),
    ('22222222-2222-2222-2222-222222222222', 'Nuremberg'),
    ('22222222-2222-2222-2222-222222222222', 'Augsburg')
ON CONFLICT DO NOTHING;

-- Regions: Kazakhstan
INSERT INTO public.regions (id, country_id, name) VALUES
    ('22222222-2222-2222-2222-222222222231', '11111111-1111-1111-1111-111111111104', 'Almaty'),
    ('22222222-2222-2222-2222-222222222232', '11111111-1111-1111-1111-111111111104', 'Astana')
ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name;

-- Cities: Kazakhstan
INSERT INTO public.cities (region_id, name) VALUES
    ('22222222-2222-2222-2222-222222222231', 'Almaty'),
    ('22222222-2222-2222-2222-222222222232', 'Astana')
ON CONFLICT DO NOTHING;

-- Regions: Uzbekistan
INSERT INTO public.regions (id, country_id, name) VALUES
    ('22222222-2222-2222-2222-222222222241', '11111111-1111-1111-1111-111111111105', 'Tashkent'),
    ('22222222-2222-2222-2222-222222222242', '11111111-1111-1111-1111-111111111105', 'Samarkand')
ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name;

-- Cities: Uzbekistan
INSERT INTO public.cities (region_id, name) VALUES
    ('22222222-2222-2222-2222-222222222241', 'Tashkent'),
    ('22222222-2222-2222-2222-222222222242', 'Samarkand')
ON CONFLICT DO NOTHING;
