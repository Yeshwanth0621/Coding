import { useState, useEffect, useCallback } from 'react';
import { supabase } from '../lib/supabase';
import type { Resource } from '../types';

const REFRESH_INTERVAL_MS = 1500;

export function useResources(countryId: string | undefined) {
  const [resources, setResources] = useState<Resource[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchResources = useCallback(async () => {
    if (!countryId) {
      setResources([]);
      setError(null);
      setLoading(false);
      return;
    }

    try {
      const { data, error: fbError } = await supabase
        .from('resources')
        .select('*')
        .eq('country_id', countryId);

      if (fbError) throw fbError;
      setResources(data || []);
      setError(null);
    } catch (e: unknown) {
      const msg = e instanceof Error ? e.message : 'Failed to fetch resources';
      setError(msg);
    } finally {
      setLoading(false);
    }
  }, [countryId]);

  useEffect(() => {
    void fetchResources();

    if (!countryId) return;

    const refresh = () => {
      void fetchResources();
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
      .channel(`resources_${countryId}`)
      .on(
        'postgres_changes',
        { event: '*', schema: 'public', table: 'resources', filter: `country_id=eq.${countryId}` },
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
  }, [countryId, fetchResources]);

  return { resources, loading, error, refetch: fetchResources };
}
