-- ===================================================
-- AURA DATING & SOCIAL DISCOVERY - ADMIN & MODERATION
-- Migration: 05_admin_and_moderation.sql
-- ===================================================

-- 1. Fix Foreign Key Constraints for Clean Cascade Deletion
ALTER TABLE public.conversations
DROP CONSTRAINT IF EXISTS conversations_last_message_sender_id_fkey;

ALTER TABLE public.conversations
ADD CONSTRAINT conversations_last_message_sender_id_fkey
FOREIGN KEY (last_message_sender_id) REFERENCES public.profiles(id) ON DELETE SET NULL;

ALTER TABLE public.matches
DROP CONSTRAINT IF EXISTS matches_unmatched_by_fkey;

ALTER TABLE public.matches
ADD CONSTRAINT matches_unmatched_by_fkey
FOREIGN KEY (unmatched_by) REFERENCES public.profiles(id) ON DELETE SET NULL;

-- 2. Hard/Full Delete Account Procedure (Removes Auth and Cascades All Data)
CREATE OR REPLACE FUNCTION public.soft_delete_user_account()
RETURNS VOID AS $$
DECLARE
    v_user_id UUID := auth.uid();
BEGIN
    IF v_user_id IS NULL THEN
        RAISE EXCEPTION 'Not authenticated';
    END IF;

    -- Nullify last message sender in conversations
    UPDATE public.conversations
    SET last_message_sender_id = NULL
    WHERE last_message_sender_id = v_user_id;

    -- Nullify unmatched_by in matches
    UPDATE public.matches
    SET unmatched_by = NULL
    WHERE unmatched_by = v_user_id;

    -- Deleting from auth.users cascades to public.profiles, profile_photos, swipes, preferences, tokens, etc.
    DELETE FROM auth.users WHERE id = v_user_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

GRANT EXECUTE ON FUNCTION public.soft_delete_user_account TO authenticated, anon;

-- 2. Admin Moderation View for Pending Reports
CREATE OR REPLACE VIEW public.admin_pending_reports AS
SELECT
    r.id AS report_id,
    r.reason,
    r.details,
    r.created_at AS reported_at,
    reporter.id AS reporter_id,
    reporter.display_name AS reporter_name,
    reported.id AS reported_id,
    reported.display_name AS reported_name,
    reported.is_banned AS reported_is_banned
FROM public.reports r
JOIN public.profiles reporter ON reporter.id = r.reporter_id
JOIN public.profiles reported ON reported.id = r.reported_id
WHERE r.status = 'PENDING';

-- 3. Get Blocked Users (Bypasses Discovery RLS to show names and avatars on Settings Screen)
CREATE OR REPLACE FUNCTION public.get_my_blocked_users()
RETURNS TABLE (
    id UUID,
    blocked_user_id UUID,
    display_name TEXT,
    photo_url TEXT,
    created_at TIMESTAMPTZ
) AS $$
DECLARE
    v_user_id UUID := auth.uid();
BEGIN
    RETURN QUERY
    SELECT 
        b.id,
        b.blocked_id AS blocked_user_id,
        p.display_name,
        (
            SELECT ph.photo_url 
            FROM public.profile_photos ph 
            WHERE ph.user_id = b.blocked_id 
            ORDER BY ph.is_primary DESC, ph.display_order ASC 
            LIMIT 1
        ) AS photo_url,
        b.created_at
    FROM public.blocks b
    JOIN public.profiles p ON p.id = b.blocked_id
    WHERE b.blocker_id = v_user_id
    ORDER BY b.created_at DESC;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;
