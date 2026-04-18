import { useState, useEffect, useCallback } from 'react';
import { supabase } from '../lib/supabase';
import type { GameState } from '../types';

const REFRESH_INTERVAL_MS = 1500;

export function useGameState() {
  const [gameState, setGameState] = useState<GameState | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchGameState = useCallback(async () => {
    try {
      const { data, error: fbError } = await supabase
        .from('game_state')
        .select('*')
        .eq('is_active', true)
        .single();

      if (fbError) throw fbError;
      setGameState(data);
      setError(null);
    } catch (e: unknown) {
      const msg = e instanceof Error ? e.message : 'Failed to fetch game state';
      setError(msg);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void fetchGameState();

    const refresh = () => {
      void fetchGameState();
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
      .channel('game_state_changes')
      .on(
        'postgres_changes',
        { event: '*', schema: 'public', table: 'game_state' },
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
  }, [fetchGameState]);

  return { gameState, loading, error, refetch: fetchGameState };
}
