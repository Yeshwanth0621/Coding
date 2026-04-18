import { useState, useEffect, useCallback } from 'react';
import { motion } from 'framer-motion';
import { 
  Trophy, 
  Activity, 
  Globe, 
  Search, 
  Crown,
  Target
} from 'lucide-react';
import { supabase } from '../lib/supabase';
import { calculateScores } from '../lib/gameLogic';
import { Card } from '../components/ui/Card';
import { Badge } from '../components/ui/Badge';
import { Button } from '../components/ui/Button';
import type { Resource, Industry, TradeOffer, LeaderboardEntry } from '../types';

const REFRESH_INTERVAL_MS = 10000;

export function Leaderboard() {
  const [leaderboard, setLeaderboard] = useState<LeaderboardEntry[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');

  const fetchData = useCallback(async () => {
    try {
      const [countriesRes, resourcesRes, industriesRes, tradesRes] = await Promise.all([
        supabase.from('countries').select('*'),
        supabase.from('resources').select('*'),
        supabase.from('industries').select('*'),
        supabase.from('trade_offers').select('*'),
      ]);

      const countries = countriesRes.data || [];
      const resources = resourcesRes.data || [];
      const industries = industriesRes.data || [];
      const trades = tradesRes.data || [];

      const entries = countries.map(country => {
        const res = resources.filter((r: Resource) => r.country_id === country.id);
        const ind = industries.filter((i: Industry) => i.country_id === country.id);
        return calculateScores(country, res, ind, trades as TradeOffer[]);
      });
      entries.sort((a, b) => b.scores.total - a.scores.total);
      setLeaderboard(entries);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchData();
    const interval = setInterval(fetchData, REFRESH_INTERVAL_MS);
    return () => clearInterval(interval);
  }, [fetchData]);

  const filtered = leaderboard.filter(e => 
    e.country.name.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className="space-y-8 pb-12">
      {/* Header */}
      <section className="relative overflow-hidden rounded-3xl glass-panel p-8 md:p-12 border-white/5">
        <div className="absolute top-0 right-0 p-8 opacity-10">
          <Trophy className="w-64 h-64 rotate-12 text-neon-gold" />
        </div>
        
        <div className="relative z-10 space-y-4">
          <div className="flex items-center space-x-3">
            <Badge variant="neon" size="md" icon={<Target className="w-3 h-3" />}>
              Global Rankings
            </Badge>
            <span className="text-zinc-500 font-mono text-[10px] tracking-[0.4em] uppercase">Live Broadcast</span>
          </div>
          <h1 className="text-4xl md:text-6xl font-black font-heading tracking-tighter text-white uppercase">
            Hall of <span className="text-neon-gold">Power</span>
          </h1>
          <p className="text-zinc-400 text-lg max-w-md font-medium">
            The most influential nations currently dominating the global landscape.
          </p>
        </div>

        <div className="absolute bottom-0 left-0 right-0 h-1 bg-white/5">
          <motion.div 
            initial={{ width: 0 }}
            animate={{ width: '100%' }}
            transition={{ duration: 15, repeat: Infinity, ease: "linear" }}
            className="h-full bg-gradient-to-r from-neon-gold via-neon-magenta to-neon-gold bg-[length:200%_100%] animate-[gradient_3s_linear_infinite]"
          />
        </div>
      </section>

      {/* Search & Stats */}
      <div className="flex flex-col md:flex-row items-center justify-between gap-6">
        <div className="relative group w-full md:w-96">
          <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-zinc-500 group-focus-within:text-neon-gold transition-colors" />
          <input
            type="text"
            placeholder="Search Nations..."
            value={search}
            onChange={e => setSearch(e.target.value)}
            className="w-full bg-white/5 border border-white/10 rounded-xl py-3 pl-12 pr-4 text-sm text-white focus:outline-none focus:border-neon-gold/50 focus:bg-white/10 transition-all font-medium"
          />
        </div>
        <div className="flex items-center space-x-6">
          <div className="text-right">
            <div className="text-[10px] font-black text-zinc-500 uppercase tracking-widest">Active Operatives</div>
            <div className="text-xl font-heading font-black text-white">{leaderboard.length}</div>
          </div>
          <div className="h-10 w-px bg-white/10" />
          <div className="text-right">
            <div className="text-[10px] font-black text-zinc-500 uppercase tracking-widest">Global Stability</div>
            <div className="text-xl font-heading font-black text-neon-cyan">94.2%</div>
          </div>
        </div>
      </div>

      {/* Leaderboard Table */}
      <div className="space-y-4">
        {loading ? (
          <div className="py-24 text-center">
            <Activity className="w-12 h-12 text-neon-cyan animate-spin mx-auto mb-4" />
            <span className="text-zinc-500 font-heading font-black uppercase tracking-widest">Synchronizing Rankings...</span>
          </div>
        ) : (
          filtered.map((entry, idx) => (
            <motion.div
              key={entry.country.id}
              initial={{ opacity: 0, x: -20 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ delay: idx * 0.05 }}
            >
              <Card 
                variant={idx === 0 ? 'neon' : 'glass'} 
                padding="md" 
                className={idx === 0 ? 'border-neon-gold/30' : ''}
              >
                <div className="flex flex-col md:flex-row items-center justify-between gap-6">
                  <div className="flex items-center gap-8 w-full md:w-auto">
                    <div className="flex items-center justify-center w-12 h-12 rounded-xl bg-white/5 border border-white/10 font-heading font-black text-xl text-white">
                      {idx === 0 ? <Crown className="w-6 h-6 text-neon-gold" /> : idx + 1}
                    </div>
                    <div className="flex items-center gap-4">
                      <div className="w-10 h-10 rounded-lg bg-zinc-800 flex items-center justify-center">
                        <Globe className="w-5 h-5 text-zinc-400" />
                      </div>
                      <div>
                        <h3 className="text-lg font-heading font-black text-white uppercase tracking-tight">
                          {entry.country.name}
                        </h3>
                        <div className="flex items-center gap-3">
                          <span className="text-[10px] font-black text-zinc-500 uppercase tracking-widest">Rank {idx + 1}</span>
                          <div className="w-1 h-1 rounded-full bg-zinc-700" />
                          <span className="text-[10px] font-black text-neon-cyan uppercase tracking-widest">Level 42</span>
                        </div>
                      </div>
                    </div>
                  </div>

                  <div className="grid grid-cols-3 gap-8 w-full md:w-auto px-4 md:px-0">
                    <div className="text-center md:text-right">
                      <div className="text-[9px] font-black text-zinc-500 uppercase tracking-widest mb-1">Economy</div>
                      <div className="text-sm font-heading font-bold text-white">{entry.scores.economic_strength}</div>
                    </div>
                    <div className="text-center md:text-right">
                      <div className="text-[9px] font-black text-zinc-500 uppercase tracking-widest mb-1">Industrial</div>
                      <div className="text-sm font-heading font-bold text-white">{entry.scores.resilience}</div>
                    </div>
                    <div className="text-center md:text-right">
                      <div className="text-[9px] font-black text-zinc-500 uppercase tracking-widest mb-1">Diplomacy</div>
                      <div className="text-sm font-heading font-bold text-white">{entry.scores.diplomacy}</div>
                    </div>
                  </div>

                  <div className="flex items-center gap-6 w-full md:w-auto justify-between md:justify-end border-t md:border-t-0 border-white/5 pt-4 md:pt-0">
                    <div className="text-right">
                      <div className="text-[10px] font-black text-zinc-500 uppercase tracking-widest">Power Score</div>
                      <div className={`text-2xl font-heading font-black ${idx === 0 ? 'text-neon-gold' : 'text-neon-cyan'}`}>
                        {entry.scores.total.toLocaleString()}
                      </div>
                    </div>
                    <Button variant="ghost" size="sm" className="hidden md:flex">
                      <Activity className="w-4 h-4" />
                    </Button>
                  </div>
                </div>
              </Card>
            </motion.div>
          ))
        )}
      </div>
    </div>
  );
}
