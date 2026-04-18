import { useState, useEffect, useCallback, useMemo } from 'react';
import { motion } from 'framer-motion';
import { 
  Shield, 
  Zap, 
  TrendingUp, 
  Users, 
  Activity, 
  Globe, 
  AlertTriangle,
  Database,
  Cpu,
  Coins,
  Factory
} from 'lucide-react';
import { useAuth } from '../hooks/useAuth';
import { useCountry } from '../hooks/useCountry';
import { useResources } from '../hooks/useResources';
import { useGameState } from '../hooks/useGameState';
import { useToast } from '../components/ui/Toast';
import { Card } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Badge } from '../components/ui/Badge';
import { supabase } from '../lib/supabase';
import { formatGC } from '../lib/constants';
import { computeResourceBonusesForCountry } from '../lib/gameLogic';
import { cn } from '../lib/utils';
import type { Industry, EventLog, ResourceType } from '../types';

const INDUSTRY_REFRESH_INTERVAL_MS = 2000;

export function Dashboard() {
  const { user } = useAuth();
  const { country } = useCountry(user?.id);
  const { resources } = useResources(country?.id);
  const { gameState } = useGameState();

  const [industries, setIndustries] = useState<Industry[]>([]);
  const [events, setEvents] = useState<EventLog[]>([]);

  const resourceBonuses = useMemo(() => 
    computeResourceBonusesForCountry(industries, country?.id), 
    [industries, country?.id]
  );

  const fetchIndustries = useCallback(async () => {
    if (!country?.id) return;
    try {
      const { data } = await supabase
        .from('industries')
        .select('*')
        .eq('country_id', country.id)
        .order('built_at_round', { ascending: false });
      setIndustries(data || []);
    } catch (error) {
      console.error('Error fetching industries:', error);
    }
  }, [country?.id]);

  const fetchEvents = useCallback(async () => {
    const { data } = await supabase
      .from('event_log')
      .select('*')
      .order('created_at', { ascending: false })
      .limit(10);
    setEvents(data || []);
  }, []);

  useEffect(() => {
    fetchIndustries();
    fetchEvents();
    const interval = setInterval(() => {
      fetchIndustries();
      fetchEvents();
    }, INDUSTRY_REFRESH_INTERVAL_MS);
    return () => clearInterval(interval);
  }, [fetchIndustries, fetchEvents]);

  const resourceIcons: Record<ResourceType, any> = {
    manpower: Users,
    energy: Zap,
    food: Activity,
    technology: Cpu,
    finance: Coins,
    minerals: Database,
    manufacturing: Factory,
    influence: Globe,
  };

  return (
    <div className="space-y-8 pb-12">
      {/* Nation Header */}
      <section className="relative overflow-hidden rounded-3xl glass-panel p-8 md:p-12 border-white/5">
        <div className="absolute top-0 right-0 p-8 opacity-10">
          <Globe className="w-64 h-64 rotate-12" />
        </div>
        
        <div className="relative z-10 flex flex-col md:flex-row md:items-end justify-between gap-6">
          <div className="space-y-4">
            <div className="flex items-center space-x-3">
              <Badge variant="neon" size="md">Operational</Badge>
              <span className="text-zinc-500 font-mono text-xs tracking-[0.3em] uppercase">Sector 7-G</span>
            </div>
            <h1 className="text-4xl md:text-6xl font-black font-heading tracking-tighter text-white uppercase">
              {country?.name || 'INITIALIZING...'}
            </h1>
            <div className="flex items-center space-x-6">
              <div className="flex items-center space-x-2">
                <Shield className="w-5 h-5 text-neon-cyan" />
                <span className="text-zinc-400 font-bold uppercase tracking-widest text-xs">Stability: 98%</span>
              </div>
              <div className="flex items-center space-x-2">
                <TrendingUp className="w-5 h-5 text-neon-magenta" />
                <span className="text-zinc-400 font-bold uppercase tracking-widest text-xs">Growth: +2.4%</span>
              </div>
            </div>
          </div>

          <div className="flex flex-col items-end">
            <div className="text-right mb-2">
              <span className="text-zinc-500 text-[10px] font-black uppercase tracking-[0.4em]">Global Credits</span>
              <div className="text-3xl font-heading font-black text-neon-gold">
                  {formatGC(country?.global_credits || 0)}
                </div>
            </div>
            <Button variant="neon" size="sm" icon={<Activity className="w-4 h-4" />}>
              Status Report
            </Button>
          </div>
        </div>

        {/* Animated Progress Bar */}
        <div className="absolute bottom-0 left-0 right-0 h-1 bg-white/5">
          <motion.div 
            initial={{ width: 0 }}
            animate={{ width: '100%' }}
            transition={{ duration: 10, repeat: Infinity, ease: "linear" }}
            className="h-full bg-gradient-to-r from-neon-cyan via-neon-magenta to-neon-cyan bg-[length:200%_100%] animate-[gradient_3s_linear_infinite]"
          />
        </div>
      </section>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Resource Grid */}
        <div className="lg:col-span-2 space-y-6">
          <div className="flex items-center justify-between">
            <h2 className="text-xl font-heading font-black uppercase tracking-widest flex items-center gap-3">
              <Database className="w-6 h-6 text-neon-cyan" />
              Strategic Resources
            </h2>
            <span className="text-zinc-500 text-[10px] font-mono uppercase tracking-[0.2em]">Live Telemetry</span>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            {resources?.map((res, idx) => (
              <motion.div
                key={res.type}
                initial={{ opacity: 0, x: -20 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ delay: idx * 0.05 }}
              >
                <Card variant="glass" padding="md" className="group">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center space-x-4">
                      <div className={cn(
                        "p-3 rounded-xl bg-white/5 border border-white/10 transition-colors duration-300 group-hover:border-neon-cyan/50",
                        `text-res-${res.type}`
                      )}>
                        {(() => {
                          const Icon = resourceIcons[res.type as ResourceType] || Database;
                          return <Icon className="w-6 h-6" />;
                        })()}
                      </div>
                      <div>
                        <div className="text-[10px] font-black uppercase tracking-[0.2em] text-zinc-500 mb-1">
                          {res.type}
                        </div>
                        <div className="text-2xl font-heading font-bold text-white">
                          {Math.floor(res.amount)}
                        </div>
                      </div>
                    </div>
                    <div className="text-right">
                      <div className="text-xs font-bold text-green-400 flex items-center justify-end gap-1">
                        <TrendingUp className="w-3 h-3" />
                        +{resourceBonuses[res.type as ResourceType] || 0}
                      </div>
                      <div className="text-[10px] text-zinc-600 font-mono mt-1">PER ROUND</div>
                    </div>
                  </div>
                </Card>
              </motion.div>
            ))}
          </div>
        </div>

        {/* Sidebar Info */}
        <div className="space-y-8">
          {/* Game State Card */}
          <Card variant="neon" padding="lg" className="border-neon-cyan/20">
            <div className="space-y-6">
              <div className="flex items-center justify-between">
                <h3 className="text-sm font-heading font-black uppercase tracking-widest text-neon-cyan">
                  Phase Protocol
                </h3>
                <Badge variant="neon" size="xs">Active</Badge>
              </div>
              
              <div className="flex items-center justify-between">
                <div className="text-4xl font-heading font-black text-white">
                  ROUND {gameState?.current_round || 1}
                </div>
                <div className="text-right">
                  <div className="text-[10px] font-black text-zinc-500 uppercase tracking-widest">Status</div>
                  <div className="text-sm font-bold text-white uppercase">{gameState?.phase || 'PRE-GAME'}</div>
                </div>
              </div>

              <div className="pt-4 border-t border-white/10 space-y-4">
                <div className="flex justify-between text-xs">
                  <span className="text-zinc-400 font-bold uppercase tracking-wider">Next Sync</span>
                  <span className="text-neon-cyan font-mono">00:42:15</span>
                </div>
                <div className="h-1 bg-white/5 rounded-full overflow-hidden">
                  <motion.div 
                    initial={{ width: 0 }}
                    animate={{ width: '65%' }}
                    className="h-full bg-neon-cyan shadow-[0_0_10px_rgba(0,240,255,0.5)]"
                  />
                </div>
              </div>
            </div>
          </Card>

          {/* Event Log */}
          <div className="space-y-4">
            <h3 className="text-sm font-heading font-black uppercase tracking-widest flex items-center gap-2">
              <Activity className="w-4 h-4 text-neon-magenta" />
              Event Stream
            </h3>
            <div className="space-y-2 max-h-[400px] overflow-y-auto pr-2 custom-scrollbar">
              {events.map((event, idx) => (
                <motion.div
                  key={event.id}
                  initial={{ opacity: 0, scale: 0.95 }}
                  animate={{ opacity: 1, scale: 1 }}
                  transition={{ delay: idx * 0.1 }}
                  className="glass-card p-4 rounded-xl border-white/5 flex items-start gap-4"
                >
                  <div className="mt-1">
                    <AlertTriangle className="w-4 h-4 text-neon-magenta" />
                  </div>
                  <div>
                    <div className="text-[10px] font-black uppercase tracking-widest text-zinc-500 mb-1">
                      {new Date(event.created_at).toLocaleTimeString()}
                    </div>
                    <div className="text-xs text-white/90 leading-relaxed">
                      {event.message}
                    </div>
                  </div>
                </motion.div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
