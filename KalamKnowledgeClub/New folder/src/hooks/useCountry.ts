import { useState, useEffect, useCallback } from 'react';
import { supabase } from '../lib/supabase';
import type { Country } from '../types';

const REFRESH_INTERVAL_MS = 1500;

export function useCountry(userId: string | undefined) {
  const [country, setCountry] = useState<Country | null>(null);
  const [allCountries, setAllCountries] = useState<Country[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchCountry = useCallback(async () => {
    if (!userId) {
      setCountry(null);
      setError(null);
      setLoading(false);
      return;
    }

    try {
      const { data, error: fbError } = await supabase
        .from('countries')
        .select('*')
        .eq('user_id', userId)
        .single();

      if (fbError) throw fbError;
      setCountry(data);
      setError(null);
    } catch (e: unknown) {
      const msg = e instanceof Error ? e.message : 'Failed to fetch country';
      setError(msg);
    } finally {
      setLoading(false);
    }
  }, [userId]);

  const fetchAllCountries = useCallback(async () => {
    try {
      const { data, error: fbError } = await supabase
        .from('countries')
        .select('*')
        .order('name');

      if (fbError) throw fbError;
      setAllCountries(data || []);
    } catch {
      // silent
    }
  }, []);

  useEffect(() => {
    void fetchCountry();
    void fetchAllCountries();

    const refresh = () => {
      void fetchCountry();
      void fetchAllCountries();
    };

    const intervalId = window.setInterval(refresh, REFRESH_INTERVAL_MS);
    const handleFocus = () => refresh();
    const handleVisibility = () => {
      if (document.visibilityState === 'visible') {
        refresh();
      }
    };

    window.addEventListener('focus', handleFocus);
    document.addEventListener('visibilitychange', handleVisibility);

    const channel = supabase
      .channel(`countries_changes_${userId ?? 'viewer'}`)
      .on(
        'postgres_changes',
        { event: '*', schema: 'public', table: 'countries' },
        () => {
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
  }, [fetchCountry, fetchAllCountries, userId]);

  return { country, allCountries, loading, error, refetch: fetchCountry, refetchAll: fetchAllCountries };
}
