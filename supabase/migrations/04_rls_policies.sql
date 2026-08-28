-- ===================================================
-- AURA DATING & SOCIAL DISCOVERY - ROW LEVEL SECURITY
-- Migration: 04_rls_policies.sql
-- ===================================================

-- Enable RLS on all tables
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.profile_photos ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.interests ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.user_interests ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.user_preferences ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.swipes ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.matches ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.conversations ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.conversation_participants ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.messages ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.blocks ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.reports ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.device_tokens ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.notifications ENABLE ROW LEVEL SECURITY;

-- 1. Profiles Policies
DROP POLICY IF EXISTS "Public profiles are viewable by authenticated users" ON public.profiles;
CREATE POLICY "Public profiles are viewable by authenticated users"
ON public.profiles FOR SELECT
TO authenticated
USING (
    is_banned = false
    AND NOT EXISTS (
        SELECT 1 FROM public.blocks b
        WHERE (b.blocker_id = auth.uid() AND b.blocked_id = profiles.id)
           OR (b.blocker_id = profiles.id AND b.blocked_id = auth.uid())
    )
);

DROP POLICY IF EXISTS "Users can insert their own profile" ON public.profiles;
CREATE POLICY "Users can insert their own profile"
ON public.profiles FOR INSERT
TO authenticated
WITH CHECK (auth.uid() = id);

DROP POLICY IF EXISTS "Users can update their own profile" ON public.profiles;
CREATE POLICY "Users can update their own profile"
ON public.profiles FOR UPDATE
TO authenticated
USING (auth.uid() = id)
WITH CHECK (auth.uid() = id);

-- 2. Profile Photos Policies
DROP POLICY IF EXISTS "Profile photos are viewable by authenticated users" ON public.profile_photos;
CREATE POLICY "Profile photos are viewable by authenticated users"
ON public.profile_photos FOR SELECT
TO authenticated
USING (true);

DROP POLICY IF EXISTS "Users can manage their own photos" ON public.profile_photos;
CREATE POLICY "Users can manage their own photos"
ON public.profile_photos FOR ALL
TO authenticated
USING (auth.uid() = user_id)
WITH CHECK (auth.uid() = user_id);

-- 3. Interests & User Interests Policies
DROP POLICY IF EXISTS "Interests catalog is readable by authenticated users" ON public.interests;
CREATE POLICY "Interests catalog is readable by authenticated users"
ON public.interests FOR SELECT
TO authenticated
USING (true);

DROP POLICY IF EXISTS "User interests are readable by authenticated users" ON public.user_interests;
CREATE POLICY "User interests are readable by authenticated users"
ON public.user_interests FOR SELECT
TO authenticated
USING (true);

DROP POLICY IF EXISTS "Users can manage their own interests" ON public.user_interests;
CREATE POLICY "Users can manage their own interests"
ON public.user_interests FOR ALL
TO authenticated
USING (auth.uid() = user_id)
WITH CHECK (auth.uid() = user_id);

-- 4. User Preferences Policies
DROP POLICY IF EXISTS "Users can view and manage their own preferences" ON public.user_preferences;
CREATE POLICY "Users can view and manage their own preferences"
ON public.user_preferences FOR ALL
TO authenticated
USING (auth.uid() = user_id)
WITH CHECK (auth.uid() = user_id);

-- 5. Swipes Policies
DROP POLICY IF EXISTS "Users can insert their own swipes" ON public.swipes;
CREATE POLICY "Users can insert their own swipes"
ON public.swipes FOR INSERT
TO authenticated
WITH CHECK (auth.uid() = swiper_id);

DROP POLICY IF EXISTS "Users can view their own swipes" ON public.swipes;
CREATE POLICY "Users can view their own swipes"
ON public.swipes FOR SELECT
TO authenticated
USING (auth.uid() = swiper_id);

-- 6. Matches Policies
DROP POLICY IF EXISTS "Users can view their own matches" ON public.matches;
CREATE POLICY "Users can view their own matches"
ON public.matches FOR SELECT
TO authenticated
USING (auth.uid() = user1_id OR auth.uid() = user2_id);

DROP POLICY IF EXISTS "Users can insert matches" ON public.matches;
CREATE POLICY "Users can insert matches"
ON public.matches FOR INSERT
TO authenticated
WITH CHECK (auth.uid() = user1_id OR auth.uid() = user2_id);

DROP POLICY IF EXISTS "Users can update matches" ON public.matches;
CREATE POLICY "Users can update matches"
ON public.matches FOR UPDATE
TO authenticated
USING (auth.uid() = user1_id OR auth.uid() = user2_id);

-- 7. Conversations & Participants Policies (Non-recursive)
DROP POLICY IF EXISTS "Participants can view conversation" ON public.conversations;
CREATE POLICY "Participants can view conversation"
ON public.conversations FOR SELECT
TO authenticated
USING (
    EXISTS (
        SELECT 1 FROM public.matches m
        WHERE m.id = conversations.match_id
          AND (m.user1_id = auth.uid() OR m.user2_id = auth.uid())
    )
);

DROP POLICY IF EXISTS "Participants can update conversation" ON public.conversations;
CREATE POLICY "Participants can update conversation"
ON public.conversations FOR UPDATE
TO authenticated
USING (
    EXISTS (
        SELECT 1 FROM public.matches m
        WHERE m.id = conversations.match_id
          AND (m.user1_id = auth.uid() OR m.user2_id = auth.uid())
    )
)
WITH CHECK (
    EXISTS (
        SELECT 1 FROM public.matches m
        WHERE m.id = conversations.match_id
          AND (m.user1_id = auth.uid() OR m.user2_id = auth.uid())
    )
);

DROP POLICY IF EXISTS "Participants can view participant list" ON public.conversation_participants;
CREATE POLICY "Participants can view participant list"
ON public.conversation_participants FOR SELECT
TO authenticated
USING (
    user_id = auth.uid()
    OR EXISTS (
        SELECT 1 FROM public.matches m
        JOIN public.conversations c ON c.match_id = m.id
        WHERE c.id = conversation_participants.conversation_id
          AND (m.user1_id = auth.uid() OR m.user2_id = auth.uid())
    )
);

DROP POLICY IF EXISTS "Participants can update their own read receipt" ON public.conversation_participants;
CREATE POLICY "Participants can update their own read receipt"
ON public.conversation_participants FOR UPDATE
TO authenticated
USING (auth.uid() = user_id)
WITH CHECK (auth.uid() = user_id);

DROP POLICY IF EXISTS "Users can insert participants" ON public.conversation_participants;
CREATE POLICY "Users can insert participants"
ON public.conversation_participants FOR INSERT
TO authenticated
WITH CHECK (
    user_id = auth.uid()
    OR EXISTS (
        SELECT 1 FROM public.matches m
        JOIN public.conversations c ON c.match_id = m.id
        WHERE c.id = conversation_participants.conversation_id
          AND (m.user1_id = auth.uid() OR m.user2_id = auth.uid())
    )
);

-- 8. Messages Policies (Non-recursive)
DROP POLICY IF EXISTS "Participants can view messages" ON public.messages;
CREATE POLICY "Participants can view messages"
ON public.messages FOR SELECT
TO authenticated
USING (
    auth.uid() = sender_id
    OR EXISTS (
        SELECT 1 FROM public.matches m
        JOIN public.conversations c ON c.match_id = m.id
        WHERE c.id = messages.conversation_id
          AND (m.user1_id = auth.uid() OR m.user2_id = auth.uid())
    )
    OR EXISTS (
        SELECT 1 FROM public.conversation_participants cp
        WHERE cp.conversation_id = messages.conversation_id
          AND cp.user_id = auth.uid()
    )
);

DROP POLICY IF EXISTS "Participants can insert messages" ON public.messages;
CREATE POLICY "Participants can insert messages"
ON public.messages FOR INSERT
TO authenticated
WITH CHECK (
    auth.uid() = sender_id
);

DROP POLICY IF EXISTS "Sender can delete or edit their message" ON public.messages;
CREATE POLICY "Sender can delete or edit their message"
ON public.messages FOR UPDATE
TO authenticated
USING (auth.uid() = sender_id)
WITH CHECK (auth.uid() = sender_id);

-- 9. Blocks Policies
DROP POLICY IF EXISTS "Users can view their own blocks" ON public.blocks;
CREATE POLICY "Users can view their own blocks"
ON public.blocks FOR SELECT
TO authenticated
USING (auth.uid() = blocker_id);

DROP POLICY IF EXISTS "Users can insert blocks" ON public.blocks;
CREATE POLICY "Users can insert blocks"
ON public.blocks FOR INSERT
TO authenticated
WITH CHECK (auth.uid() = blocker_id);

DROP POLICY IF EXISTS "Users can delete their own blocks" ON public.blocks;
CREATE POLICY "Users can delete their own blocks"
ON public.blocks FOR DELETE
TO authenticated
USING (auth.uid() = blocker_id);

-- 10. Reports Policies
DROP POLICY IF EXISTS "Users can submit reports" ON public.reports;
CREATE POLICY "Users can submit reports"
ON public.reports FOR INSERT
TO authenticated
WITH CHECK (auth.uid() = reporter_id);

-- 11. Device Tokens Policies
DROP POLICY IF EXISTS "Users can manage their device tokens" ON public.device_tokens;
CREATE POLICY "Users can manage their device tokens"
ON public.device_tokens FOR ALL
TO authenticated
USING (auth.uid() = user_id)
WITH CHECK (auth.uid() = user_id);

-- 12. Notifications Policies
DROP POLICY IF EXISTS "Users can view and update their notifications" ON public.notifications;
CREATE POLICY "Users can view and update their notifications"
ON public.notifications FOR ALL
TO authenticated
USING (auth.uid() = user_id)
WITH CHECK (auth.uid() = user_id);

-- 13. Storage Bucket Setup for Profile Photos
INSERT INTO storage.buckets (id, name, public)
VALUES ('profile-photos', 'profile-photos', true)
ON CONFLICT (id) DO NOTHING;

DROP POLICY IF EXISTS "Public profile photos are viewable by everyone" ON storage.objects;
CREATE POLICY "Public profile photos are viewable by everyone"
ON storage.objects FOR SELECT
USING (bucket_id = 'profile-photos');

DROP POLICY IF EXISTS "Users can upload their own profile photos" ON storage.objects;
CREATE POLICY "Users can upload their own profile photos"
ON storage.objects FOR INSERT
TO authenticated
WITH CHECK (
    bucket_id = 'profile-photos'
    AND (storage.foldername(name))[1] = auth.uid()::text
);

DROP POLICY IF EXISTS "Users can update and delete their own profile photos" ON storage.objects;
CREATE POLICY "Users can update and delete their own profile photos"
ON storage.objects FOR DELETE
TO authenticated
USING (
    bucket_id = 'profile-photos'
    AND (storage.foldername(name))[1] = auth.uid()::text
);

-- 14. Realtime Publications
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_publication_tables WHERE pubname = 'supabase_realtime' AND schemaname = 'public' AND tablename = 'messages') THEN
        ALTER PUBLICATION supabase_realtime ADD TABLE public.messages;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_publication_tables WHERE pubname = 'supabase_realtime' AND schemaname = 'public' AND tablename = 'conversations') THEN
        ALTER PUBLICATION supabase_realtime ADD TABLE public.conversations;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_publication_tables WHERE pubname = 'supabase_realtime' AND schemaname = 'public' AND tablename = 'notifications') THEN
        ALTER PUBLICATION supabase_realtime ADD TABLE public.notifications;
    END IF;
END $$;
