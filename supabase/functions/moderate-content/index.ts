// Supabase Edge Function: moderate-content
// Automated content & text screening for safety, toxicity, and prohibited patterns

import { serve } from "https://deno.land/std@0.168.0/http/server.ts";

const PROHIBITED_KEYWORDS = [
  "underage",
  "minor",
  "scam",
  "credit card",
  "wire money",
  "crypto giveaway",
];

serve(async (req) => {
  try {
    if (req.method !== "POST") {
      return new Response("Method not allowed", { status: 405 });
    }

    const { text } = await req.json();
    if (!text || typeof text !== "string") {
      return new Response(JSON.stringify({ flagged: false }), {
        headers: { "Content-Type": "application/json" },
      });
    }

    const lower = text.toLowerCase();
    const hasProhibited = PROHIBITED_KEYWORDS.some((kw) => lower.includes(kw));

    return new Response(
      JSON.stringify({
        flagged: hasProhibited,
        reason: hasProhibited ? "Contains prohibited terms" : null,
      }),
      { headers: { "Content-Type": "application/json" } }
    );
  } catch (err: any) {
    return new Response(JSON.stringify({ error: err.message }), {
      status: 500,
      headers: { "Content-Type": "application/json" },
    });
  }
});
