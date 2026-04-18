import { useState, useEffect, useCallback, useRef } from 'react';
import { supabase } from '../lib/supabase';
import type { TradeOffer } from '../types';

const PLAYER_REFRESH_INTERVAL_MS = 2500;
const ADMIN_REFRESH_INTERVAL_MS = 3500;

export function useTrades(countryId: string | undefined) {
  const [incomingOffers, setIncomingOffers] = useState<TradeOffer[]>([]);
  const [sentOffers, setSentOffers] = useState<TradeOffer[]>([]);
  const [allTrades, setAllTrades] = useState<TradeOffer[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const toastCallback = useRef<((offer: TradeOffer) => void) | null>(null);

  const fetchTrades = useCallback(async () => {
    if (!countryId) {
      setIncomingOffers([]);
      setSentOffers([]);
      setError(null);
      setLoading(false);
      return;
    }

    try {
      // Incoming offers
      const { data: incoming, error: inErr } = await supabase
        .from('trade_offers')
        .select('*, from_country:countries!trade_offers_from_country_id_fkey(*), to_country:countries!trade_offers_to_country_id_fkey(*)')
        .eq('to_country_id', countryId)
        .order('created_at', { ascending: false });
      if (inErr) throw inErr;

      // Sent offers
      const { data: sent, error: sentErr } = await supabase
        .from('trade_offers')
        .select('*, from_country:countries!trade_offers_from_country_id_fkey(*), to_country:countries!trade_offers_to_country_id_fkey(*)')
        .eq('from_country_id', countryId)
        .order('created_at', { ascending: false });
      if (sentErr) throw sentErr;

      setIncomingOffers(incoming || []);
      setSentOffers(sent || []);
      setError(null);
    } catch (e: unknown) {
      const msg = e instanceof Error ? e.message : 'Failed to fetch trades';
      setError(msg);
    } finally {
      setLoading(false);
    }
  }, [countryId]);

  const fetchAllTrades = useCallback(async () => {
    try {
      const { data, error: err } = await supabase
        .from('trade_offers')
        .select('*, from_country:countries!trade_offers_from_country_id_fkey(*), to_country:countries!trade_offers_to_country_id_fkey(*)')
        .order('created_at', { ascending: false });
      if (err) throw err;
      setAllTrades(data || []);
    } catch {
      // silent fail for admin view
    }
  }, []);

  useEffect(() => {
    void fetchTrades();

    if (!countryId) return;

    const refresh = () => {
      void fetchTrades();
    };

    const intervalId = window.setInterval(refresh, PLAYER_REFRESH_INTERVAL_MS);
    const handleFocus = () => refresh();
    const handleVisibility = () => {
      if (document.visibilityState === 'visible') {
        refresh();
      }
    };

    window.addEventListener('focus', handleFocus);
    document.addEventListener('visibilitychange', handleVisibility);

    const channel = supabase
      .channel(`trades_${countryId}`)
      .on(
        'postgres_changes',
        { event: '*', schema: 'public', table: 'trade_offers' },
        (payload) => {
          const offer = payload.new as Partial<TradeOffer>;
          // Notify if new incoming offer
          if (payload.eventType === 'INSERT' && offer.to_country_id === countryId) {
            toastCallback.current?.(offer as TradeOffer);
          }
          // Refresh all trades
          refresh();
        }
      )
      .subscribe();

    return () => {
      window.clearInterval(intervalId);
      window.removeEventListener('focus', handleFocus);
      document.removeEventListener('visibilitychange', handleVisibility);
      supabase.removeChannel(channel);
    };
  }, [countryId, fetchTrades]);

  useEffect(() => {
    if (countryId) return;

    void fetchAllTrades();

    const refreshAll = () => {
      void fetchAllTrades();
    };

    const intervalId = window.setInterval(refreshAll, ADMIN_REFRESH_INTERVAL_MS);
    const handleFocus = () => refreshAll();
    const handleVisibility = () => {
      if (document.visibilityState === 'visible') {
        refreshAll();
      }
    };

    window.addEventListener('focus', handleFocus);
    document.addEventListener('visibilitychange', handleVisibility);

    const channel = supabase
      .channel('trades_all')
      .on(
        'postgres_changes',
        { event: '*', schema: 'public', table: 'trade_offers' },
        () => {
          refreshAll();
        }
      )
      .subscribe();

    return () => {
      window.clearInterval(intervalId);
      window.removeEventListener('focus', handleFocus);
      document.removeEventListener('visibilitychange', handleVisibility);
      supabase.removeChannel(channel);
    };
  }, [countryId, fetchAllTrades]);

  const setOnNewOffer = (cb: (offer: TradeOffer) => void) => {
    toastCallback.current = cb;
  };

  return {
    incomingOffers,
    sentOffers,
    allTrades,
    loading,
    error,
    refetch: fetchTrades,
    fetchAllTrades,
    setOnNewOffer,
  };
}
