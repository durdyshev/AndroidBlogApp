// Supabase Edge Function: send-push-notification
// Sends Firebase Cloud Messaging (FCM) push notifications securely without exposing Firebase credentials to clients

import { serve } from "https://deno.land/std@0.168.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

interface PushPayload {
  userId: string;
  title: string;
  body: string;
  type: "NEW_LIKE" | "NEW_MATCH" | "NEW_MESSAGE" | "SUPER_LIKE" | "SYSTEM";
  data?: Record<string, string>;
}

serve(async (req) => {
  try {
    if (req.method !== "POST") {
      return new Response("Method not allowed", { status: 405 });
    }

    const { userId, title, body, type, data } = (await req.json()) as PushPayload;

    if (!userId || !title || !body) {
      return new Response(JSON.stringify({ error: "Missing required fields" }), {
        status: 400,
        headers: { "Content-Type": "application/json" },
      });
    }

    const supabaseUrl = Deno.env.get("SUPABASE_URL")!;
    const supabaseServiceKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!;
    const fcmServerKey = Deno.env.get("FCM_SERVER_KEY");

    const supabase = createClient(supabaseUrl, supabaseServiceKey);

    // Fetch device tokens for user
    const { data: tokens, error: tokenError } = await supabase
      .from("device_tokens")
      .select("token")
      .eq("user_id", userId);

    if (tokenError || !tokens || tokens.length === 0) {
      return new Response(
        JSON.stringify({ status: "skipped", reason: "No active device tokens found" }),
        { headers: { "Content-Type": "application/json" } }
      );
    }

    // Send push to FCM
    if (fcmServerKey) {
      for (const t of tokens) {
        await fetch("https://fcm.googleapis.com/fcm/send", {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: `key=${fcmServerKey}`,
          },
          body: JSON.stringify({
            to: t.token,
            notification: {
              title,
              body,
              sound: "default",
            },
            data: {
              type,
              ...data,
            },
          }),
        });
      }
    }

    return new Response(
      JSON.stringify({ status: "success", deliveredToTokens: tokens.length }),
      { headers: { "Content-Type": "application/json" } }
    );
  } catch (err: any) {
    return new Response(JSON.stringify({ error: err.message }), {
      status: 500,
      headers: { "Content-Type": "application/json" },
    });
  }
});
