-- ===================================================
-- AURA DATING & SOCIAL DISCOVERY - FUNCTIONS & PROCEDURES
-- Migration: 03_functions_and_triggers.sql
-- ===================================================

-- 1. Haversine Distance Calculation Function (KM)
CREATE OR REPLACE FUNCTION public.calculate_distance_km(
    lat1 DOUBLE PRECISION,
    lon1 DOUBLE PRECISION,
    lat2 DOUBLE PRECISION,
    lon2 DOUBLE PRECISION
)
RETURNS DOUBLE PRECISION AS $$
DECLARE
    r DOUBLE PRECISION := 6371.0; -- Earth's radius in km
    dlat DOUBLE PRECISION;
    dlon DOUBLE PRECISION;
    a DOUBLE PRECISION;
    c DOUBLE PRECISION;
BEGIN
    IF lat1 IS NULL OR lon1 IS NULL OR lat2 IS NULL OR lon2 IS NULL THEN
        RETURN NULL;
    END IF;

    dlat := radians(lat2 - lat1);
    dlon := radians(lon2 - lon1);
    a := sin(dlat / 2.0) * sin(dlat / 2.0) +
         cos(radians(lat1)) * cos(radians(lat2)) *
         sin(dlon / 2.0) * sin(dlon / 2.0);
    c := 2.0 * atan2(sqrt(a), sqrt(1.0 - a));
    RETURN r * c;
END;
$$ LANGUAGE plpgsql IMMUTABLE;

-- 2. Update Location Function
CREATE OR REPLACE FUNCTION public.update_user_location(
    p_latitude DOUBLE PRECISION,
    p_longitude DOUBLE PRECISION
)
RETURNS VOID AS $$
BEGIN
    UPDATE public.profiles
    SET
        latitude = p_latitude,
        longitude = p_longitude,
        location_updated_at = now(),
        updated_at = now()
    WHERE id = auth.uid();
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 3. Atomic Process Swipe Function
CREATE OR REPLACE FUNCTION public.process_swipe(
    p_target_id UUID,
    p_action TEXT
)
RETURNS JSONB AS $$
DECLARE
    v_swiper_id UUID := auth.uid();
    v_is_reciprocal BOOLEAN := false;
    v_match_id UUID := NULL;
    v_user1_id UUID;
    v_user2_id UUID;
    v_target_profile JSONB;
    v_swiper_name TEXT;
BEGIN
    IF v_swiper_id IS NULL THEN
        RAISE EXCEPTION 'Not authenticated';
    END IF;

    IF v_swiper_id = p_target_id THEN
        RAISE EXCEPTION 'Cannot swipe on yourself';
    END IF;

    -- Upsert Swipe Record
    INSERT INTO public.swipes (swiper_id, target_id, action, created_at)
    VALUES (v_swiper_id, p_target_id, p_action, now())
    ON CONFLICT (swiper_id, target_id)
    DO UPDATE SET action = p_action, created_at = now();

    -- If PASS, return no match
    IF p_action = 'PASS' THEN
        RETURN jsonb_build_object('is_match', false, 'match_id', NULL);
    END IF;

    -- Check for reciprocal LIKE or SUPER_LIKE
    SELECT EXISTS (
        SELECT 1 FROM public.swipes
        WHERE swiper_id = p_target_id
          AND target_id = v_swiper_id
          AND action IN ('LIKE', 'SUPER_LIKE')
    ) INTO v_is_reciprocal;

    IF v_is_reciprocal THEN
        -- Order user IDs to enforce unique match constraint
        IF v_swiper_id < p_target_id THEN
            v_user1_id := v_swiper_id;
            v_user2_id := p_target_id;
        ELSE
            v_user1_id := p_target_id;
            v_user2_id := v_swiper_id;
        END IF;

        -- Create or reactivate Match
        INSERT INTO public.matches (user1_id, user2_id, is_active, created_at)
        VALUES (v_user1_id, v_user2_id, true, now())
        ON CONFLICT (user1_id, user2_id)
        DO UPDATE SET is_active = true, unmatched_by = NULL, unmatched_at = NULL
        RETURNING id INTO v_match_id;

        -- Create Conversation if not exists
        INSERT INTO public.conversations (match_id, created_at, updated_at)
        VALUES (v_match_id, now(), now())
        ON CONFLICT (match_id) DO NOTHING;

        -- Add participants
        INSERT INTO public.conversation_participants (conversation_id, user_id, last_read_at)
        SELECT c.id, v_user1_id, now() FROM public.conversations c WHERE c.match_id = v_match_id
        ON CONFLICT DO NOTHING;

        INSERT INTO public.conversation_participants (conversation_id, user_id, last_read_at)
        SELECT c.id, v_user2_id, now() FROM public.conversations c WHERE c.match_id = v_match_id
        ON CONFLICT DO NOTHING;

        -- Create Notifications for both users
        SELECT display_name INTO v_swiper_name FROM public.profiles WHERE id = v_swiper_id;

        INSERT INTO public.notifications (user_id, actor_id, type, title, body, data)
        VALUES (
            p_target_id,
            v_swiper_id,
            'NEW_MATCH',
            'It''s a Match! 🎉',
            'You and ' || COALESCE(v_swiper_name, 'someone') || ' liked each other!',
            jsonb_build_object('match_id', v_match_id, 'actor_id', v_swiper_id)
        );

        -- Fetch Target Public Profile for Immediate UI Celebration
        SELECT jsonb_build_object(
            'id', p.id,
            'display_name', p.display_name,
            'birth_date', p.birth_date,
            'photos', COALESCE(jsonb_agg(ph.photo_url ORDER BY ph.display_order) FILTER (WHERE ph.id IS NOT NULL), '[]'::jsonb)
        )
        INTO v_target_profile
        FROM public.profiles p
        LEFT JOIN public.profile_photos ph ON ph.user_id = p.id
        WHERE p.id = p_target_id
        GROUP BY p.id, p.display_name, p.birth_date;

        RETURN jsonb_build_object(
            'is_match', true,
            'match_id', v_match_id,
            'matched_user', v_target_profile
        );
    ELSE
        -- If LIKE or SUPER_LIKE without reciprocal yet, notify target user
        IF p_action = 'LIKE' THEN
            SELECT display_name INTO v_swiper_name FROM public.profiles WHERE id = v_swiper_id;
            INSERT INTO public.notifications (user_id, actor_id, type, title, body, data)
            VALUES (
                p_target_id,
                v_swiper_id,
                'NEW_LIKE',
                'New Like! 👀',
                'Someone just liked your profile.',
                jsonb_build_object('actor_id', v_swiper_id)
            );
        ELSIF p_action = 'SUPER_LIKE' THEN
            SELECT display_name INTO v_swiper_name FROM public.profiles WHERE id = v_swiper_id;
            INSERT INTO public.notifications (user_id, actor_id, type, title, body, data)
            VALUES (
                p_target_id,
                v_swiper_id,
                'SUPER_LIKE',
                'Someone Super Liked you! ⭐',
                'Someone special just super liked your profile.',
                jsonb_build_object('actor_id', v_swiper_id)
            );
        END IF;

        RETURN jsonb_build_object('is_match', false, 'match_id', NULL);
    END IF;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 4. Cursor Paginated Discovery Candidates Query Function
CREATE OR REPLACE FUNCTION public.get_discovery_candidates(
    p_limit INT DEFAULT 20,
    p_cursor_id UUID DEFAULT NULL
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
    photos JSONB,
    interests JSONB
) AS $$
DECLARE
    v_user_id UUID := auth.uid();
    v_my_lat DOUBLE PRECISION;
    v_my_lon DOUBLE PRECISION;
    v_min_age INT;
    v_max_age INT;
    v_gender_pref TEXT;
    v_max_dist INT;
    v_online_only BOOLEAN;
BEGIN
    IF v_user_id IS NULL THEN
        RAISE EXCEPTION 'Not authenticated';
    END IF;

    -- Get Current User Coordinates & Preferences
    SELECT latitude, longitude INTO v_my_lat, v_my_lon FROM public.profiles WHERE profiles.id = v_user_id;

    SELECT
        COALESCE(min_age, 18),
        COALESCE(max_age, 50),
        COALESCE(interested_in_gender, 'ALL'),
        COALESCE(max_distance_km, 50),
        COALESCE(show_only_online, false)
    INTO v_min_age, v_max_age, v_gender_pref, v_max_dist, v_online_only
    FROM public.user_preferences
    WHERE user_preferences.user_id = v_user_id;

    -- Default fallback if preferences row does not exist yet
    v_min_age := COALESCE(v_min_age, 18);
    v_max_age := COALESCE(v_max_age, 50);
    v_gender_pref := COALESCE(v_gender_pref, 'ALL');
    v_max_dist := COALESCE(v_max_dist, 50);
    v_online_only := COALESCE(v_online_only, false);

    RETURN QUERY
    WITH candidate_profiles AS (
        SELECT
            p.id,
            p.display_name,
            p.birth_date,
            p.gender,
            p.bio,
            public.calculate_distance_km(v_my_lat, v_my_lon, p.latitude, p.longitude) AS dist,
            p.is_online,
            p.last_seen_at
        FROM public.profiles p
        WHERE p.id != v_user_id
          AND p.is_banned = false
          -- Exclude blocked users (either direction)
          AND NOT EXISTS (
              SELECT 1 FROM public.blocks b
              WHERE (b.blocker_id = v_user_id AND b.blocked_id = p.id)
                 OR (b.blocker_id = p.id AND b.blocked_id = v_user_id)
          )
          -- Exclude already swiped profiles
          AND NOT EXISTS (
              SELECT 1 FROM public.swipes s
              WHERE s.swiper_id = v_user_id AND s.target_id = p.id
          )
          -- Filter Gender Preference
          AND (
              v_gender_pref = 'ALL'
              OR (v_gender_pref = 'WOMEN' AND p.gender = 'WOMAN')
              OR (v_gender_pref = 'MEN' AND p.gender = 'MAN')
              OR (v_gender_pref = 'NON_BINARY' AND p.gender = 'NON_BINARY')
          )
          -- Filter Age Preference
          AND (
              DATE_PART('year', age(p.birth_date)) >= v_min_age
              AND DATE_PART('year', age(p.birth_date)) <= v_max_age
          )
          -- Filter Online Status
          AND (NOT v_online_only OR p.is_online = true)
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
    WHERE (v_my_lat IS NULL OR cp.dist IS NULL OR cp.dist <= v_max_dist)
    ORDER BY cp.is_online DESC, cp.dist ASC NULLS LAST, cp.id
    LIMIT p_limit;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 5. Unmatch User Function
CREATE OR REPLACE FUNCTION public.unmatch_user(p_match_id UUID)
RETURNS VOID AS $$
BEGIN
    UPDATE public.matches
    SET
        is_active = false,
        unmatched_by = auth.uid(),
        unmatched_at = now()
    WHERE id = p_match_id
      AND (user1_id = auth.uid() OR user2_id = auth.uid());
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 6. Get or Create Conversation Between Two Users Function
CREATE OR REPLACE FUNCTION public.get_or_create_conversation(p_target_user_id UUID)
RETURNS UUID AS $$
DECLARE
    v_my_id UUID := auth.uid();
    v_user1_id UUID;
    v_user2_id UUID;
    v_match_id UUID;
    v_conv_id UUID;
BEGIN
    IF v_my_id IS NULL THEN
        RAISE EXCEPTION 'Not authenticated';
    END IF;

    IF v_my_id = p_target_user_id THEN
        RAISE EXCEPTION 'Cannot converse with yourself';
    END IF;

    -- Order user IDs to enforce unique match constraint
    IF v_my_id < p_target_user_id THEN
        v_user1_id := v_my_id;
        v_user2_id := p_target_user_id;
    ELSE
        v_user1_id := p_target_user_id;
        v_user2_id := v_my_id;
    END IF;

    -- Create or reactivate Match
    INSERT INTO public.matches (user1_id, user2_id, is_active, created_at)
    VALUES (v_user1_id, v_user2_id, true, now())
    ON CONFLICT (user1_id, user2_id)
    DO UPDATE SET is_active = true, unmatched_by = NULL, unmatched_at = NULL
    RETURNING id INTO v_match_id;

    -- Create Conversation if not exists
    INSERT INTO public.conversations (match_id, created_at, updated_at)
    VALUES (v_match_id, now(), now())
    ON CONFLICT (match_id) DO NOTHING;

    SELECT id INTO v_conv_id FROM public.conversations WHERE match_id = v_match_id;

    -- Ensure both participants exist
    INSERT INTO public.conversation_participants (conversation_id, user_id, last_read_at)
    VALUES (v_conv_id, v_user1_id, now())
    ON CONFLICT (conversation_id, user_id) DO NOTHING;

    INSERT INTO public.conversation_participants (conversation_id, user_id, last_read_at)
    VALUES (v_conv_id, v_user2_id, now())
    ON CONFLICT (conversation_id, user_id) DO NOTHING;

    RETURN v_conv_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 7. Automatic Notification on New Message Trigger
CREATE OR REPLACE FUNCTION public.handle_new_message_notification()
RETURNS TRIGGER AS $$
DECLARE
    v_sender_name TEXT;
    v_recipient_id UUID;
BEGIN
    -- Find sender display name
    SELECT display_name INTO v_sender_name FROM public.profiles WHERE id = NEW.sender_id;

    -- Find the other participant in this conversation
    SELECT user_id INTO v_recipient_id
    FROM public.conversation_participants
    WHERE conversation_id = NEW.conversation_id
      AND user_id != NEW.sender_id
    LIMIT 1;

    -- If not found in conversation_participants, find via matches table
    IF v_recipient_id IS NULL THEN
        SELECT CASE WHEN m.user1_id = NEW.sender_id THEN m.user2_id ELSE m.user1_id END INTO v_recipient_id
        FROM public.conversations c
        JOIN public.matches m ON c.match_id = m.id
        WHERE c.id = NEW.conversation_id;
    END IF;

    -- If recipient found, insert into notifications table
    IF v_recipient_id IS NOT NULL THEN
        INSERT INTO public.notifications (user_id, actor_id, type, title, body, data, is_read, created_at)
        VALUES (
            v_recipient_id,
            NEW.sender_id,
            'NEW_MESSAGE',
            COALESCE(v_sender_name, 'New Message'),
            CASE 
                WHEN NEW.message_type = 'IMAGE' THEN '📷 Sent a photo'
                ELSE NEW.content
            END,
            jsonb_build_object(
                'conversation_id', NEW.conversation_id,
                'message_id', NEW.id,
                'sender_id', NEW.sender_id
            ),
            false,
            now()
        );
    END IF;

    -- Update last message in conversations table
    UPDATE public.conversations
    SET
        last_message_text = CASE WHEN NEW.message_type = 'IMAGE' THEN '📷 Photo' ELSE NEW.content END,
        last_message_at = NEW.created_at,
        last_message_sender_id = NEW.sender_id,
        updated_at = now()
    WHERE id = NEW.conversation_id;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

DROP TRIGGER IF EXISTS trigger_on_new_message ON public.messages;
CREATE TRIGGER trigger_on_new_message
AFTER INSERT ON public.messages
FOR EACH ROW
EXECUTE FUNCTION public.handle_new_message_notification();
