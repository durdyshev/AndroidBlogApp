-- ===================================================
-- AURA DATING & SOCIAL DISCOVERY - INDEXES
-- Migration: 02_indexes_and_constraints.sql
-- ===================================================

-- Profiles Indexes
CREATE INDEX IF NOT EXISTS idx_profiles_location ON public.profiles (latitude, longitude) WHERE is_banned = false;
CREATE INDEX IF NOT EXISTS idx_profiles_gender_birthdate ON public.profiles (gender, birth_date);
CREATE INDEX IF NOT EXISTS idx_profiles_is_online ON public.profiles (is_online, last_seen_at);

-- Photos Indexes
CREATE INDEX IF NOT EXISTS idx_profile_photos_user_order ON public.profile_photos (user_id, display_order);
CREATE INDEX IF NOT EXISTS idx_profile_photos_primary ON public.profile_photos (user_id) WHERE is_primary = true;

-- Swipes Indexes
CREATE INDEX IF NOT EXISTS idx_swipes_swiper ON public.swipes (swiper_id, created_at);
CREATE INDEX IF NOT EXISTS idx_swipes_target ON public.swipes (target_id, action);

-- Matches Indexes
CREATE INDEX IF NOT EXISTS idx_matches_user1 ON public.matches (user1_id) WHERE is_active = true;
CREATE INDEX IF NOT EXISTS idx_matches_user2 ON public.matches (user2_id) WHERE is_active = true;

-- Conversations & Messages Indexes
CREATE INDEX IF NOT EXISTS idx_conversations_match ON public.conversations (match_id);
CREATE INDEX IF NOT EXISTS idx_conversations_updated ON public.conversations (updated_at DESC);
CREATE INDEX IF NOT EXISTS idx_messages_conversation_time ON public.messages (conversation_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_messages_sender ON public.messages (sender_id);

-- Blocks & Reports Indexes
CREATE INDEX IF NOT EXISTS idx_blocks_blocker ON public.blocks (blocker_id);
CREATE INDEX IF NOT EXISTS idx_blocks_blocked ON public.blocks (blocked_id);
CREATE INDEX IF NOT EXISTS idx_reports_reported ON public.reports (reported_id, status);

-- Notifications Indexes
CREATE INDEX IF NOT EXISTS idx_notifications_user_read ON public.notifications (user_id, is_read, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_device_tokens_user ON public.device_tokens (user_id);
