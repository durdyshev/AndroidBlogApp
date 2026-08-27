-- ===================================================
-- AURA DATING & SOCIAL DISCOVERY - ADMIN & MODERATION
-- Migration: 05_admin_and_moderation.sql
-- ===================================================

-- 1. Soft Delete Account Procedure
CREATE OR REPLACE FUNCTION public.soft_delete_user_account()
RETURNS VOID AS $$
DECLARE
    v_user_id UUID := auth.uid();
BEGIN
    IF v_user_id IS NULL THEN
        RAISE EXCEPTION 'Not authenticated';
    END IF;

    -- Deactivate matches
    UPDATE public.matches
    SET is_active = false,
        unmatched_by = v_user_id,
        unmatched_at = now()
    WHERE user1_id = v_user_id OR user2_id = v_user_id;

    -- Remove device tokens
    DELETE FROM public.device_tokens WHERE user_id = v_user_id;

    -- Anonymize and mark profile as banned/deactivated
    UPDATE public.profiles
    SET
        display_name = 'Deleted Account',
        bio = NULL,
        latitude = NULL,
        longitude = NULL,
        is_banned = true,
        is_online = false,
        updated_at = now()
    WHERE id = v_user_id;

    -- Remove photos
    DELETE FROM public.profile_photos WHERE user_id = v_user_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

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
