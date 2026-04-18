import { useState, useEffect, useCallback, Fragment } from 'react';
import { supabase } from '../lib/supabase';
import { useAuth } from '../hooks/useAuth';
import { useGameState } from '../hooks/useGameState';
import { useTrades } from '../hooks/useTrades';
import { useToast } from '../components/ui/Toast';
import { Navbar } from '../components/layout/Navbar';
import { AdminSidebar } from '../components/layout/AdminSidebar';
import { Card } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Modal } from '../components/ui/Modal';
import { TradeOfferCard } from '../components/game/TradeOfferCard';
import { LeaderboardRow } from '../components/game/LeaderboardRow';
import { useMarket } from '../hooks/useMarket';
import { MarketPostCard } from '../components/game/MarketPostCard';
import { PHASE_CONFIG, PHASE_ORDER, EVENT_CARDS, formatGC, RESOURCE_TYPES, RESOURCE_CONFIG } from '../lib/constants';
import { calculateScores, computeCountryResourceBonuses } from '../lib/gameLogic';
import type { Country, Resource, Industry, IndustryCatalogItem, LeaderboardEntry, ResourceType } from '../types';

const ADMIN_TABS = [
  { id: 'control', label: 'Game Control', icon: '🎮' },
  { id: 'countries', label: 'Countries', icon: '🌍' },
  { id: 'trades', label: 'Trade Monitor', icon: '🤝' },
  { id: 'events', label: 'Event Cards', icon: '⚡' },
  { id: 'leaderboard', label: 'Leaderboard', icon: '🏆' },
];

const ADMIN_REFRESH_INTERVAL_MS = 1500;

interface AdminSnapshot {
  countries: Country[];
  resources: Resource[];
  industries: Industry[];
  catalog: IndustryCatalogItem[];
}

export function Admin() {
  const { signOut } = useAuth();
  const { gameState, refetch: refetchGame } = useGameState();
  const { allTrades, fetchAllTrades } = useTrades(undefined);
  const { addToast } = useToast();

  const [activeTab, setActiveTab] = useState('control');
  const [countries, setCountries] = useState<Country[]>([]);
  const [allResources, setAllResources] = useState<Resource[]>([]);
  const [allIndustries, setAllIndustries] = useState<Industry[]>([]);
  const [loading, setLoading] = useState(false);
  
  const { openPosts, refetch: refetchMarket } = useMarket(undefined);
  const [customGC, setCustomGC] = useState(10);
  const [customRes, setCustomRes] = useState<ResourceType | 'ALL'>('ALL');
  const [customResQty, setCustomResQty] = useState(1);
  const [selectedCountryIds, setSelectedCountryIds] = useState<string[]>([]);
  const [bulkResType, setBulkResType] = useState<ResourceType>('Food');
  const [bulkResQty, setBulkResQty] = useState(3);

  // Event modal
  const [eventModalOpen, setEventModalOpen] = useState(false);
  const [selectedEvent, setSelectedEvent] = useState<typeof EVENT_CARDS[0] | null>(null);
  const [affectedCountries, setAffectedCountries] = useState<string[]>([]);

  // Edit country modal
  const [editCountry, setEditCountry] = useState<Country | null>(null);
  const [editGC, setEditGC] = useState(0);
  const [editPopulation, setEditPopulation] = useState(0);
  const [editFoodProduced, setEditFoodProduced] = useState(0);
  const [editFoodReq, setEditFoodReq] = useState(0);
  const [editResources, setEditResources] = useState<Record<string, number>>({});

  // Leaderboard
  const [leaderboard, setLeaderboard] = useState<LeaderboardEntry[]>([]);

  const fetchAdminSnapshot = useCallback(async (): Promise<AdminSnapshot> => {
    const [countriesRes, resourcesRes, industriesRes, catalogRes] = await Promise.all([
      supabase.from('countries').select('*').order('name'),
      supabase.from('resources').select('*'),
      supabase.from('industries').select('*'),
      supabase.from('industry_catalog').select('*'),
    ]);

    return {
      countries: countriesRes.data || [],
      resources: resourcesRes.data || [],
      industries: industriesRes.data || [],
      catalog: catalogRes.data || [],
    };
  }, []);

  const fetchData = useCallback(async () => {
    const snapshot = await fetchAdminSnapshot();
    setCountries(snapshot.countries);
    setAllResources(snapshot.resources);
    setAllIndustries(snapshot.industries);
  }, [fetchAdminSnapshot]);

  useEffect(() => {
    void fetchData();
    void fetchAllTrades();
    void refetchMarket();

    const refresh = () => {
      void fetchData();
      void fetchAllTrades();
      void refetchMarket();
    };

    const intervalId = window.setInterval(refresh, ADMIN_REFRESH_INTERVAL_MS);
    const handleFocus = () => refresh();
    const handleVisibility = () => {
      if (document.visibilityState === 'visible') {
        refresh();
      }
    };

    window.addEventListener('focus', handleFocus);
    document.addEventListener('visibilitychange', handleVisibility);

    const channel = supabase
      .channel('admin_data_changes')
      .on('postgres_changes', { event: '*', schema: 'public', table: 'countries' }, refresh)
      .on('postgres_changes', { event: '*', schema: 'public', table: 'resources' }, refresh)
      .on('postgres_changes', { event: '*', schema: 'public', table: 'industries' }, refresh)
      .subscribe();

    return () => {
      window.clearInterval(intervalId);
      window.removeEventListener('focus', handleFocus);
      document.removeEventListener('visibilitychange', handleVisibility);
      supabase.removeChannel(channel);
    };
  }, [fetchData, fetchAllTrades, refetchMarket]);

  // Calculate leaderboard
  useEffect(() => {
    if (countries.length === 0) return;
    const entries = countries.map(country => {
      const res = allResources.filter(r => r.country_id === country.id);
      const ind = allIndustries.filter(i => i.country_id === country.id);
      return calculateScores(country, res, ind, allTrades);
    });
    entries.sort((a, b) => b.scores.total - a.scores.total);
    setLeaderboard(entries);
  }, [countries, allResources, allIndustries, allTrades]);

  const advancePhase = async () => {
    if (!gameState) return;
    setLoading(true);
    try {
      const currentIdx = PHASE_ORDER.indexOf(gameState.phase as typeof PHASE_ORDER[number]);
      let nextPhase: string;
      let nextRound = gameState.current_round;

      if (currentIdx === PHASE_ORDER.length - 1 || currentIdx === -1) {
        // Move to next round
        nextPhase = 'income';
        nextRound = gameState.current_round + 1;
      } else {
        nextPhase = PHASE_ORDER[currentIdx + 1];
      }

      const { error } = await supabase
        .from('game_state')
        .update({ phase: nextPhase, current_round: nextRound })
        .eq('id', gameState.id);
      if (error) throw error;

      // If income phase, process income
      if (nextPhase === 'income') {
        await processIncome(nextRound);
      }

      addToast(`Phase advanced to ${nextPhase.toUpperCase()}`, 'success');
      await refetchGame();
      await fetchData();
    } catch (e: unknown) {
      const msg = e instanceof Error ? e.message : 'Failed to advance phase';
      addToast(msg, 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleProcessIncome = async () => {
    if (!gameState || !window.confirm('Process income now?')) return;
    setLoading(true);
    try {
      await processIncome(gameState.current_round);
      addToast('Income processed successfully!', 'success', '💰');
      await fetchData();
    } catch (e: unknown) {
      addToast(e instanceof Error ? e.message : 'Error processing income', 'error');
    } finally { setLoading(false); }
  };

  const addGCToAll = async (amount: number) => {
    if (!window.confirm(`Add ${amount} GC to all countries?`)) return;
    setLoading(true);
    try {
      for (const c of countries) await supabase.from('countries').update({ gc_balance: c.gc_balance + amount }).eq('id', c.id);
      addToast(`Added ${amount} GC to all countries`, 'success');
      await fetchData();
    } finally { setLoading(false); }
  };

  const addResToAll = async (qty: number, type: ResourceType | 'ALL') => {
    if (!window.confirm(`Add +${qty} ${type} to all countries?`)) return;
    setLoading(true);
    try {
      const typesToUpdate = type === 'ALL' ? RESOURCE_TYPES : [type];
      for (const r of allResources) {
        if (typesToUpdate.includes(r.resource_type)) {
          await supabase.from('resources').update({ quantity: r.quantity + qty }).eq('id', r.id);
        }
      }
      addToast(`Added +${qty} ${type} to all`, 'success');
      await fetchData();
    } finally { setLoading(false); }
  };

  const toggleCountrySelection = (countryId: string) => {
    setSelectedCountryIds(prev =>
      prev.includes(countryId)
        ? prev.filter(id => id !== countryId)
        : [...prev, countryId]
    );
  };

  const selectAllCountries = () => {
    setSelectedCountryIds(countries.map(country => country.id));
  };

  const clearSelectedCountries = () => {
    setSelectedCountryIds([]);
  };

  const applyBulkResourceToSelected = async () => {
    if (selectedCountryIds.length === 0) {
      addToast('Select at least one country first.', 'info');
      return;
    }

    if (bulkResQty === 0) {
      addToast('Enter a non-zero quantity.', 'info');
      return;
    }

    const countryCount = selectedCountryIds.length;
    const sign = bulkResQty > 0 ? '+' : '';
    if (!window.confirm(`Apply ${sign}${bulkResQty} ${bulkResType} to ${countryCount} selected countr${countryCount === 1 ? 'y' : 'ies'}?`)) {
      return;
    }

    setLoading(true);
    try {
      const selectedSet = new Set(selectedCountryIds);
      const targetResources = allResources.filter(resource =>
        selectedSet.has(resource.country_id) && resource.resource_type === bulkResType
      );

      await Promise.all(
        targetResources.map(resource =>
          supabase
            .from('resources')
            .update({ quantity: Math.max(0, resource.quantity + bulkResQty) })
            .eq('id', resource.id)
        )
      );

      setAllResources(prev => prev.map(resource => {
        if (!selectedSet.has(resource.country_id) || resource.resource_type !== bulkResType) {
          return resource;
        }

        return { ...resource, quantity: Math.max(0, resource.quantity + bulkResQty) };
      }));

      addToast(`Applied ${sign}${bulkResQty} ${bulkResType} to ${countryCount} selected countries.`, 'success');
      await fetchData();
    } catch (e: unknown) {
      const msg = e instanceof Error ? e.message : 'Failed bulk resource update';
      addToast(msg, 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleHardReset = async () => {
    const confirmation = window.prompt('WARNING: This will delete all industries, trades, market posts, events, and reset countries to starting stats. Type "RESET" to confirm:');
    if (confirmation !== 'RESET') {
      addToast('Reset cancelled', 'info');
      return;
    }

    setLoading(true);
    try {
      const { error } = await supabase.rpc('reset_game_state');
      if (error) throw error;
      
      addToast('GAME HARD RESET COMPLETE', 'success', '🧨');
      await Promise.all([refetchGame(), fetchData(), fetchAllTrades(), refetchMarket()]);
    } catch (e: any) {
      console.error("Supabase Reset Error:", e);
      addToast(e.message || JSON.stringify(e) || 'Error resetting game', 'error');
    } finally {
      setLoading(false);
    }
  };

  const processIncome = async (round: number) => {
    const { data: existingIncomeLog, error: incomeLogError } = await supabase
      .from('event_log')
      .select('id')
      .eq('event_type', 'income')
      .eq('round_number', round)
      .limit(1);

    if (incomeLogError) {
      throw incomeLogError;
    }

    if ((existingIncomeLog || []).length > 0) {
      throw new Error(`Income for round ${round} has already been processed.`);
    }

    const snapshot = await fetchAdminSnapshot();
    const buildRoundsByName = new Map(snapshot.catalog.map(item => [item.name, item.build_rounds]));

    const newlyActivatedIndustries = snapshot.industries.filter(industry => {
      if (industry.is_active) return false;
      const buildRounds = buildRoundsByName.get(industry.industry_name) ?? 1;
      return industry.built_at_round + buildRounds <= round;
    });

    if (newlyActivatedIndustries.length > 0) {
      await Promise.all(
        newlyActivatedIndustries.map(industry =>
          supabase.from('industries').update({ is_active: true }).eq('id', industry.id)
        )
      );
    }

    const newlyActivatedIds = new Set(newlyActivatedIndustries.map(ind => ind.id));
    const industriesForIncome = snapshot.industries.map(industry =>
      newlyActivatedIds.has(industry.id) ? { ...industry, is_active: true } : industry
    );
    const activeIndustries = industriesForIncome.filter(industry => industry.is_active);
    const resourceBonuses = computeCountryResourceBonuses(activeIndustries);

    const foodIndustryModifiersByCountry = activeIndustries.reduce((acc, industry) => {
      if (!acc[industry.country_id]) {
        acc[industry.country_id] = { producedBonus: 0, requiredReduction: 0 };
      }

      if (industry.industry_name === 'Farm') {
        acc[industry.country_id].producedBonus += 2;
      }

      if (industry.industry_name === 'Water Treatment') {
        acc[industry.country_id].requiredReduction += 1;
      }

      return acc;
    }, {} as Record<string, { producedBonus: number; requiredReduction: number }>);

    await Promise.all(
      snapshot.resources.map(resource => {
        const bonusMap = resourceBonuses[resource.country_id];
        const bonus = bonusMap ? bonusMap[resource.resource_type] : 0;
        const increment = resource.replenish_per_round + bonus;

        return supabase
          .from('resources')
          .update({ quantity: resource.quantity + increment })
          .eq('id', resource.id);
      })
    );

    await Promise.all(
      snapshot.countries.map(async country => {
        const industrialIncome = activeIndustries
          .filter(industry => industry.country_id === country.id)
          .reduce((sum, industry) => sum + industry.income_per_round, 0);

        const foodProducedBonus = foodIndustryModifiersByCountry[country.id]?.producedBonus ?? 0;
        const foodReqReduction = foodIndustryModifiersByCountry[country.id]?.requiredReduction ?? 0;
        const effectiveFoodProduced = country.food_produced + foodProducedBonus;
        const effectiveFoodReq = Math.max(0, country.food_req - foodReqReduction);

        const updates: { gc_balance?: number; population?: number } = {};

        if (industrialIncome > 0) {
          updates.gc_balance = country.gc_balance + industrialIncome;
        }

        if (effectiveFoodProduced < effectiveFoodReq) {
          const deficit = effectiveFoodReq - effectiveFoodProduced;
          updates.population = Math.max(0, country.population - deficit);
        }

        if (Object.keys(updates).length > 0) {
          await supabase.from('countries').update(updates).eq('id', country.id);
        }
      })
    );

    await supabase.from('event_log').insert({
      round_number: round,
      event_type: 'income',
      description: `Round ${round} income processed. Resources replenished, industry income distributed.`,
      affected_countries: snapshot.countries.map(country => country.name),
    });

    await fetchData();
  };

  const startGame = async () => {
    if (!gameState) return;
    await supabase.from('game_state')
      .update({ phase: 'income', is_active: true, started_at: new Date().toISOString() })
      .eq('id', gameState.id);
    addToast('Game started!', 'success', '🎮');
    await Promise.all([refetchGame(), fetchData()]);
  };

  const pauseGame = async () => {
    if (!gameState) return;
    await supabase.from('game_state')
      .update({ phase: 'paused' })
      .eq('id', gameState.id);
    addToast('Game paused', 'info');
    await Promise.all([refetchGame(), fetchData()]);
  };

  const triggerEvent = async () => {
    if (!selectedEvent || !gameState) return;
    setLoading(true);
    try {
      await supabase.from('event_log').insert({
        round_number: gameState.current_round,
        event_type: selectedEvent.id,
        description: selectedEvent.description + ' — ' + selectedEvent.effect,
        affected_countries: affectedCountries,
      });
      addToast(`Event "${selectedEvent.name}" triggered!`, 'success', selectedEvent.icon);
      setEventModalOpen(false);
      setSelectedEvent(null);
      setAffectedCountries([]);
    } catch (e: unknown) {
      const msg = e instanceof Error ? e.message : 'Failed to trigger event';
      addToast(msg, 'error');
    } finally {
      setLoading(false);
    }
  };

  const saveCountryEdit = async () => {
    if (!editCountry) return;
    setLoading(true);
    try {
      const editingCountryId = editCountry.id;

      await supabase.from('countries')
        .update({
          gc_balance: editGC,
          population: editPopulation,
          food_produced: editFoodProduced,
          food_req: editFoodReq,
        })
        .eq('id', editingCountryId);

      await Promise.all(
        Object.entries(editResources).map(([resType, qty]) =>
          supabase
            .from('resources')
            .update({ quantity: qty })
            .eq('country_id', editingCountryId)
            .eq('resource_type', resType)
        )
      );

      setCountries(prev => prev.map(country =>
        country.id === editingCountryId
          ? {
            ...country,
            gc_balance: editGC,
            population: editPopulation,
            food_produced: editFoodProduced,
            food_req: editFoodReq,
          }
          : country
      ));

      setAllResources(prev => prev.map(resource => {
        if (resource.country_id !== editingCountryId) {
          return resource;
        }

        const updatedQty = editResources[resource.resource_type];
        return updatedQty === undefined ? resource : { ...resource, quantity: updatedQty };
      }));

      addToast(`${editCountry.name} updated`, 'success');
      setEditCountry(null);
      await fetchData();
    } catch (e: unknown) {
      const msg = e instanceof Error ? e.message : 'Failed to update';
      addToast(msg, 'error');
    } finally {
      setLoading(false);
    }
  };

  const voidTrade = async (tradeId: string) => {
    await supabase.from('trade_offers').update({
      status: 'rejected',
      responded_at: new Date().toISOString(),
    }).eq('id', tradeId);
    addToast('Trade voided by admin', 'info');
    await Promise.all([fetchAllTrades(), fetchData()]);
  };

  const voidMarketPost = async (postId: string) => {
    await supabase.from('market_posts').update({ status: 'cancelled' }).eq('id', postId);
    addToast('Market post voided', 'info');
    await Promise.all([refetchMarket(), fetchData()]);
  };

  const getCountryResources = (countryId: string) =>
    allResources.filter(r => r.country_id === countryId);

  const getCountryIndustries = (countryId: string) =>
    allIndustries.filter(i => i.country_id === countryId);

  return (
    <div className="min-h-screen bg-base flex">
      {/* Sidebar (desktop) */}
      <AdminSidebar tabs={ADMIN_TABS} activeTab={activeTab} onChange={setActiveTab} />

      {/* Main */}
      <div className="flex-1">
        <Navbar country={null} gameState={gameState} onSignOut={signOut} isAdmin />

        {/* Mobile tab selector */}
        <div className="md:hidden flex overflow-x-auto border-b border-[rgba(0,240,255,0.06)] bg-surface/60 backdrop-blur-xl">
          {ADMIN_TABS.map(tab => (
            <button key={tab.id} onClick={() => setActiveTab(tab.id)}
              className={`px-4 py-3 font-mono text-[9px] uppercase tracking-[3px] border-b-2 whitespace-nowrap transition-all duration-300
                ${activeTab === tab.id ? 'text-neon-cyan border-neon-cyan' : 'text-muted border-transparent'}`}
              style={activeTab === tab.id ? { textShadow: '0 0 10px rgba(0,240,255,0.5)' } : {}}>
              {tab.icon} {tab.label}
            </button>
          ))}
        </div>

        {/* Persistent Game Controls */}
        <div className="bg-base/80 border-b border-[rgba(0,240,255,0.06)] px-4 py-2 flex items-center justify-between sticky top-0 z-40 backdrop-blur-xl">
          <div className="flex items-center gap-2">
             <span className="text-[10px] font-mono text-muted tracking-wide">PHASE:</span>
             <span className="text-xs font-mono font-bold" style={{ color: PHASE_CONFIG[gameState?.phase || 'paused']?.color }}>{gameState?.phase.toUpperCase()}</span>
             <span className="text-[10px] font-mono text-muted tracking-wide ml-2">ROUND: {gameState?.current_round}</span>
          </div>
          <div className="flex items-center gap-2">
             {gameState?.phase === 'paused' ? (
               <Button variant="primary" size="sm" onClick={startGame}>START</Button>
             ) : (
               <>
                 <Button variant="ghost" size="sm" onClick={pauseGame} className="text-muted border-border hover:text-white">⏸</Button>
                 <Button variant="cyan" size="sm" onClick={advancePhase} loading={loading}>NEXT PHASE →</Button>
               </>
             )}
          </div>
        </div>

        <main className="max-w-7xl mx-auto px-4 py-6 space-y-6">

          {/* ─── GAME CONTROL ─── */}
          {activeTab === 'control' && (
            <div className="space-y-6 fade-in">
              <h2 className="font-heading text-xl text-primary tracking-[4px] flex items-center gap-3">
                <span className="w-8 h-px bg-gradient-to-r from-neon-cyan to-transparent" />
                Game Control & Quick Actions
              </h2>

              {/* Quick Actions Panel */}
              <Card className="border-[rgba(57,255,20,0.15)] bg-[rgba(57,255,20,0.02)]">
                <div className="flex items-center justify-between mb-4">
                  <h3 className="text-[10px] font-mono text-neon-lime uppercase tracking-[3px]">Quick Actions (Admin QoL)</h3>
                </div>
                
                <div className="space-y-6">
                  {/* GC Actions */}
                  <div>
                    <div className="text-[9px] font-mono text-muted tracking-wider mb-2">ADD GC TO EVERYONE</div>
                    <div className="flex flex-wrap gap-2">
                       <Button variant="ghost" size="sm" onClick={() => addGCToAll(10)} className="border border-[rgba(255,215,0,0.3)] text-neon-gold hover:bg-[rgba(255,215,0,0.1)]">+10 GC</Button>
                       <Button variant="ghost" size="sm" onClick={() => addGCToAll(25)} className="border border-[rgba(255,215,0,0.3)] text-neon-gold hover:bg-[rgba(255,215,0,0.1)]">+25 GC</Button>
                       <Button variant="ghost" size="sm" onClick={() => addGCToAll(50)} className="border border-[rgba(255,215,0,0.3)] text-neon-gold hover:bg-[rgba(255,215,0,0.1)]">+50 GC</Button>
                       <Button variant="ghost" size="sm" onClick={() => addGCToAll(100)} className="border border-[rgba(255,215,0,0.3)] text-neon-gold hover:bg-[rgba(255,215,0,0.1)]">+100 GC</Button>
                       <div className="flex gap-1 ml-4">
                         <input type="number" className="w-20 bg-base border border-[rgba(255,215,0,0.2)] rounded-lg px-2 text-xs font-mono text-neon-gold" value={customGC} onChange={e => setCustomGC(parseInt(e.target.value)||0)} />
                         <Button variant="ghost" size="sm" onClick={() => addGCToAll(customGC)} className="border border-[rgba(255,215,0,0.3)] text-neon-gold hover:bg-[rgba(255,215,0,0.1)]">ADD</Button>
                       </div>
                    </div>
                  </div>
                  
                  {/* Resource Actions */}
                  <div>
                    <div className="text-[9px] font-mono text-muted tracking-wider mb-2">ADD RESOURCES TO EVERYONE</div>
                    <div className="flex flex-wrap gap-2 items-center">
                       <Button variant="ghost" size="sm" onClick={() => addResToAll(1, 'ALL')} className="border border-[rgba(0,240,255,0.3)] text-neon-cyan hover:bg-[rgba(0,240,255,0.1)]">+1 ALL RESOURCES</Button>
                       <Button variant="ghost" size="sm" onClick={() => addResToAll(3, 'ALL')} className="border border-[rgba(0,240,255,0.3)] text-neon-cyan hover:bg-[rgba(0,240,255,0.1)]">+3 ALL RESOURCES</Button>
                       
                       <div className="flex items-center gap-1 ml-4">
                         <input type="number" className="w-16 bg-base border border-[rgba(0,240,255,0.2)] rounded-lg px-2 text-xs font-mono text-neon-cyan" value={customResQty} onChange={e => setCustomResQty(parseInt(e.target.value)||0)} />
                         <select className="bg-base border border-[rgba(0,240,255,0.2)] rounded flex-1 py-1 text-xs font-mono text-primary" value={customRes} onChange={e => setCustomRes(e.target.value as ResourceType | 'ALL')}>
                           <option value="ALL">ALL RESOURCES</option>
                           {RESOURCE_TYPES.map(r => <option key={r} value={r}>{r}</option>)}
                         </select>
                         <Button variant="ghost" size="sm" onClick={() => addResToAll(customResQty, customRes)} className="border border-[rgba(0,240,255,0.3)] text-neon-cyan hover:bg-[rgba(0,240,255,0.1)]">ADD</Button>
                       </div>
                    </div>
                  </div>

                  {/* Flow Actions */}
                  <div className="pt-2 border-t border-[rgba(57,255,20,0.15)] flex flex-wrap gap-4 items-center justify-between">
                    <div>
                      <div className="text-[9px] font-mono text-muted tracking-wider mb-2">FLOW ACTIONS</div>
                      <Button variant="primary" size="md" onClick={handleProcessIncome} loading={loading} className="w-full sm:w-auto tracking-[3px]">
                        💰 MANUAL PROCESS INCOME NOW
                      </Button>
                    </div>
                    <div>
                      <div className="text-[9px] font-mono text-neon-red tracking-wider mb-2 text-right">DANGER ZONE</div>
                      <Button variant="danger" size="md" onClick={handleHardReset} loading={loading} className="w-full sm:w-auto tracking-[3px]">
                        🧨 HARD RESET GAME
                      </Button>
                    </div>
                  </div>
                </div>
              </Card>

              {/* Big round display */}
              <Card className="text-center py-8">
                <div className="text-[9px] font-mono text-muted uppercase tracking-[3px] mb-2">Current Round</div>
                <div className="text-7xl font-heading text-neon-cyan mb-2" style={{ textShadow: '0 0 30px rgba(0,240,255,0.4)' }}>{gameState?.current_round ?? 0}</div>
                <div className="text-lg font-mono text-muted/50">/ 20</div>

                {gameState && (
                  <div className="mt-4 inline-flex items-center gap-2 px-4 py-2 rounded-full border"
                    style={{
                      borderColor: `${PHASE_CONFIG[gameState.phase]?.color || '#6B7280'}40`,
                      backgroundColor: `${PHASE_CONFIG[gameState.phase]?.color || '#6B7280'}10`,
                    }}>
                    <span className="w-2 h-2 rounded-full pulse-dot"
                      style={{ backgroundColor: PHASE_CONFIG[gameState.phase]?.color }} />
                    <span className="font-mono text-sm uppercase tracking-[2px]"
                      style={{ color: PHASE_CONFIG[gameState.phase]?.color }}>
                      {PHASE_CONFIG[gameState.phase]?.label}
                    </span>
                  </div>
                )}
              </Card>

              {/* Controls */}
              <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
                <Button variant="primary" size="lg" onClick={startGame}
                  disabled={gameState?.phase !== 'paused'}>
                  ▶ START GAME
                </Button>
                <Button variant="secondary" size="lg" onClick={pauseGame}
                  disabled={gameState?.phase === 'paused'}>
                  ⏸ PAUSE
                </Button>
                <Button variant="cyan" size="lg" className="col-span-2" onClick={advancePhase}
                  loading={loading} disabled={gameState?.phase === 'paused'}>
                  NEXT PHASE →
                </Button>
              </div>

              {/* Phase order */}
              <div className="flex items-center gap-2 flex-wrap">
                {PHASE_ORDER.map((p, i) => (
                  <div key={p} className="flex items-center gap-2">
                    <span className={`text-[10px] font-mono uppercase px-3 py-1.5 rounded-lg border tracking-[2px]
                      ${gameState?.phase === p ? 'border-[rgba(0,240,255,0.3)] bg-[rgba(0,240,255,0.06)] text-neon-cyan' : 'border-[rgba(0,240,255,0.06)] text-muted'}`}>
                      {p}
                    </span>
                    {i < PHASE_ORDER.length - 1 && <span className="text-muted">→</span>}
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* ─── COUNTRIES OVERVIEW ─── */}
          {activeTab === 'countries' && (
            <div className="space-y-4 fade-in">
              <h2 className="font-heading text-xl text-primary tracking-[4px] flex items-center gap-3">
                <span className="w-8 h-px bg-gradient-to-r from-neon-cyan to-transparent" />
                All Countries
              </h2>
              <Card className="border-[rgba(0,240,255,0.15)] bg-[rgba(0,240,255,0.03)]">
                <div className="flex flex-col gap-3 md:flex-row md:items-end md:justify-between">
                  <div className="space-y-2">
                    <div className="text-[10px] font-mono text-muted tracking-[2px] uppercase">Bulk Quick Edit (Selected Countries)</div>
                    <div className="flex flex-wrap items-center gap-2">
                      <span className="text-xs font-mono text-primary">Selected: {selectedCountryIds.length}</span>
                      <Button variant="ghost" size="sm" onClick={selectAllCountries}>SELECT ALL</Button>
                      <Button variant="ghost" size="sm" onClick={clearSelectedCountries}>CLEAR</Button>
                    </div>
                  </div>

                  <div className="flex flex-wrap items-center gap-2">
                    <input
                      type="number"
                      value={bulkResQty}
                      onChange={e => setBulkResQty(parseInt(e.target.value) || 0)}
                      className="w-20 bg-base border border-[rgba(0,240,255,0.2)] rounded-lg px-2 py-1.5 text-xs font-mono text-neon-cyan"
                    />
                    <select
                      value={bulkResType}
                      onChange={e => setBulkResType(e.target.value as ResourceType)}
                      className="bg-base border border-[rgba(0,240,255,0.2)] rounded py-1.5 px-2 text-xs font-mono text-primary"
                    >
                      {RESOURCE_TYPES.map(type => <option key={type} value={type}>{type}</option>)}
                    </select>
                    <Button variant="cyan" size="sm" onClick={applyBulkResourceToSelected} loading={loading}>
                      APPLY TO SELECTED
                    </Button>
                  </div>
                </div>
              </Card>
              <div className="overflow-x-auto">
                <table className="w-full text-sm font-mono">
                  <thead>
                    <tr className="border-b-2 border-[rgba(0,240,255,0.08)] text-[9px] text-muted uppercase tracking-[3px]">
                      <th className="px-3 py-2 text-center">Sel</th>
                      <th className="px-3 py-2 text-left">Country</th>
                      <th className="px-3 py-2 text-right">GC</th>
                      <th className="px-3 py-2 text-right">Pop</th>
                      <th className="px-3 py-2 text-center">Food</th>
                      <th className="px-3 py-2 text-right">Industries</th>
                      <th className="px-3 py-2 text-right">Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {countries.map(c => {
                      const ind = getCountryIndustries(c.id);
                      const activeCountryIndustries = ind.filter(i => i.is_active);
                      const producedBonus = activeCountryIndustries.filter(i => i.industry_name === 'Farm').length * 2;
                      const requiredReduction = activeCountryIndustries.filter(i => i.industry_name === 'Water Treatment').length;
                      const effectiveFoodProduced = c.food_produced + producedBonus;
                      const effectiveFoodReq = Math.max(0, c.food_req - requiredReduction);
                      const foodOk = effectiveFoodProduced >= effectiveFoodReq;
                      const isEditing = editCountry?.id === c.id;

                      return (
                        <Fragment key={c.id}>
                          <tr className={`border-b border-[rgba(0,240,255,0.04)] hover:bg-[rgba(0,240,255,0.03)] transition-colors ${isEditing ? 'bg-[rgba(0,240,255,0.05)]' : ''}`}>
                            <td className="px-3 py-2 text-center">
                              <input
                                type="checkbox"
                                checked={selectedCountryIds.includes(c.id)}
                                onChange={() => toggleCountrySelection(c.id)}
                                className="accent-[#00f0ff]"
                              />
                            </td>
                            <td className="px-3 py-2">
                              <span className="mr-2">{c.flag_emoji}</span>
                              {c.name}
                            </td>
                            <td className="px-3 py-2.5 text-right text-neon-gold" style={{ textShadow: '0 0 6px rgba(255,215,0,0.3)' }}>{formatGC(c.gc_balance)}</td>
                            <td className="px-3 py-2 text-right">{c.population}M</td>
                            <td className="px-3 py-2 text-center">
                              <span className={`text-[10px] px-2 py-0.5 rounded-lg font-mono tracking-wider ${foodOk ? 'bg-[rgba(57,255,20,0.08)] text-neon-lime border border-[rgba(57,255,20,0.15)]' : 'bg-[rgba(255,45,91,0.08)] text-neon-red border border-[rgba(255,45,91,0.15)]'}`}>
                                {foodOk ? 'OK' : 'SHORTAGE'}
                              </span>
                            </td>
                            <td className="px-3 py-2 text-right">{ind.filter(i => i.is_active).length}</td>
                            <td className="px-3 py-2 text-right">
                              <Button variant={isEditing ? 'primary' : 'ghost'} size="sm" onClick={() => {
                                if (isEditing) {
                                  setEditCountry(null);
                                } else {
                                  setEditCountry(c);
                                  setEditGC(c.gc_balance);
                                  setEditPopulation(c.population);
                                  setEditFoodProduced(c.food_produced);
                                  setEditFoodReq(c.food_req);
                                  const res = getCountryResources(c.id);
                                  const resMap: Record<string, number> = {};
                                  res.forEach(r => { resMap[r.resource_type] = r.quantity; });
                                  setEditResources(resMap);
                                }
                              }}>
                                {isEditing ? 'CANCEL' : 'QUICK EDIT'}
                              </Button>
                            </td>
                          </tr>
                          
                          {/* Inline Edit Row */}
                          {isEditing && (
                            <tr className="bg-base/80 border-b-2 border-[rgba(0,240,255,0.15)]">
                              <td colSpan={7} className="px-6 py-4">
                                <div className="flex flex-col gap-4">
                                  <div className="flex flex-wrap md:flex-nowrap gap-4 items-end">
                                    <div className="w-32">
                                      <label className="text-[9px] font-mono text-muted uppercase tracking-[2px] block mb-1">GC Balance</label>
                                      <input type="number" value={editGC} onChange={e => setEditGC(parseInt(e.target.value) || 0)} 
                                        className="w-full bg-card/60 border border-[rgba(255,215,0,0.3)] rounded-xl px-3 py-2 font-mono text-sm text-neon-gold focus:outline-none" />
                                    </div>
                                    <div className="w-28">
                                      <label className="text-[9px] font-mono text-muted uppercase tracking-[2px] block mb-1">Population</label>
                                      <input type="number" value={editPopulation} onChange={e => setEditPopulation(parseInt(e.target.value) || 0)} 
                                        className="w-full bg-card/60 border border-[rgba(255,45,91,0.3)] rounded-xl px-3 py-2 font-mono text-sm text-neon-red focus:outline-none" />
                                    </div>
                                    <div className="w-32">
                                      <label className="text-[9px] font-mono text-muted uppercase tracking-[2px] block mb-1">Food Produced</label>
                                      <input type="number" value={editFoodProduced} onChange={e => setEditFoodProduced(parseInt(e.target.value) || 0)} 
                                        className="w-full bg-card/60 border border-[rgba(57,255,20,0.3)] rounded-xl px-3 py-2 font-mono text-sm text-neon-lime focus:outline-none" />
                                    </div>
                                    <div className="w-32">
                                      <label className="text-[9px] font-mono text-muted uppercase tracking-[2px] block mb-1">Food Required</label>
                                      <input type="number" value={editFoodReq} onChange={e => setEditFoodReq(parseInt(e.target.value) || 0)} 
                                        className="w-full bg-card/60 border border-[rgba(0,240,255,0.3)] rounded-xl px-3 py-2 font-mono text-sm text-neon-cyan focus:outline-none" />
                                    </div>
                                    <div className="flex-1 grid grid-cols-4 sm:grid-cols-4 lg:grid-cols-8 gap-2">
                                      {RESOURCE_TYPES.map(rt => (
                                        <div key={rt}>
                                          <label className="text-[9px] font-mono text-muted uppercase tracking-[2px] block mb-1 truncate text-center" title={rt}>{RESOURCE_CONFIG[rt].emoji} {rt}</label>
                                          <input type="number" value={editResources[rt] ?? 0} onChange={e => setEditResources({ ...editResources, [rt]: parseInt(e.target.value) || 0 })} 
                                            className="w-full text-center bg-card/60 border border-[rgba(0,240,255,0.2)] rounded-xl px-2 py-2 font-mono text-sm text-primary focus:outline-none focus:border-neon-cyan" />
                                        </div>
                                      ))}
                                    </div>
                                    <Button variant="cyan" size="lg" loading={loading} onClick={saveCountryEdit} className="md:h-[52px]">
                                      ✓ SAVE
                                    </Button>
                                  </div>
                                </div>
                              </td>
                            </tr>
                          )}
                        </Fragment>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {/* ─── TRADE MONITOR ─── */}
          {activeTab === 'trades' && (
            <div className="space-y-4 fade-in">
              <div className="flex items-center justify-between">
                <h2 className="font-heading text-xl text-primary tracking-[4px] flex items-center gap-3">
                  <span className="w-8 h-px bg-gradient-to-r from-neon-cyan to-transparent" />
                  Trade Monitor
                </h2>
                <Button variant="ghost" size="sm" onClick={fetchAllTrades}>⟳ REFRESH</Button>
              </div>
              {allTrades.length === 0 ? (
                <Card><p className="text-center text-muted font-mono text-xs py-4 tracking-wider">No trades yet</p></Card>
              ) : (
                <div className="space-y-3">
                  {allTrades.map(trade => (
                    <TradeOfferCard
                      key={trade.id}
                      offer={trade}
                      type="monitor"
                      onVoid={() => voidTrade(trade.id)}
                    />
                  ))}
                </div>
              )}

              <h2 className="font-heading text-xl text-primary tracking-[4px] flex items-center gap-3 mt-8 pt-8 border-t border-[rgba(0,240,255,0.08)]">
                <span className="w-8 h-px bg-gradient-to-r from-neon-lime to-transparent" />
                Market Posts (Open)
              </h2>
              {openPosts.length === 0 ? (
                <Card><p className="text-center text-muted font-mono text-xs py-4 tracking-wider">No open market posts</p></Card>
              ) : (
                <div className="space-y-3">
                  {openPosts.map(post => (
                    <MarketPostCard key={post.id} post={post} type="admin" onCancel={() => voidMarketPost(post.id)} />
                  ))}
                </div>
              )}
            </div>
          )}

          {/* ─── EVENT CARDS ─── */}
          {activeTab === 'events' && (
            <div className="space-y-4 fade-in">
              <h2 className="font-heading text-xl text-primary tracking-[4px] flex items-center gap-3">
                <span className="w-8 h-px bg-gradient-to-r from-neon-magenta to-transparent" />
                Event Cards
              </h2>
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
                {EVENT_CARDS.map(event => (
                  <Card key={event.id} hover>
                    <div className="space-y-2">
                      <div className="flex items-start justify-between">
                        <span className="text-3xl">{event.icon}</span>
                        <span className={`text-[9px] font-mono uppercase px-2 py-0.5 rounded-lg border tracking-wider
                          ${event.severity === 'critical' ? 'text-neon-red border-[rgba(255,45,91,0.25)] bg-[rgba(255,45,91,0.06)]' :
                            event.severity === 'high' ? 'text-neon-gold border-[rgba(255,215,0,0.25)] bg-[rgba(255,215,0,0.06)]' :
                            'text-neon-lime border-[rgba(57,255,20,0.25)] bg-[rgba(57,255,20,0.06)]'}`}>
                          {event.severity}
                        </span>
                      </div>
                      <h3 className="font-heading text-xs text-primary tracking-[2px]">{event.name}</h3>
                      <p className="text-[10px] font-body text-muted">{event.description}</p>
                      <p className="text-[10px] font-mono text-neon-cyan/80">{event.effect}</p>
                      <Button variant="danger" size="sm" className="w-full" onClick={() => {
                        setSelectedEvent(event);
                        setAffectedCountries([]);
                        setEventModalOpen(true);
                      }}>
                        ⚡ TRIGGER
                      </Button>
                    </div>
                  </Card>
                ))}
              </div>
            </div>
          )}

          {/* ─── LEADERBOARD ─── */}
          {activeTab === 'leaderboard' && (
            <div className="space-y-4 fade-in">
              <div className="flex items-center justify-between">
                <h2 className="font-heading text-xl text-primary tracking-[4px] flex items-center gap-3">
                  <span className="w-8 h-px bg-gradient-to-r from-neon-gold to-transparent" />
                  Leaderboard
                </h2>
                <Button variant="ghost" size="sm" onClick={() => fetchData()}>⟳ REFRESH</Button>
              </div>
              <div className="overflow-x-auto">
                <table className="w-full">
                  <thead>
                    <tr className="border-b-2 border-[rgba(0,240,255,0.08)] text-[9px] font-mono text-muted uppercase tracking-[3px]">
                      <th className="px-3 py-2 text-left">Rank</th>
                      <th className="px-3 py-2 text-left">Country</th>
                      <th className="px-3 py-2 text-center">Economic</th>
                      <th className="px-3 py-2 text-center">Sustain.</th>
                      <th className="px-3 py-2 text-center">Diplomacy</th>
                      <th className="px-3 py-2 text-center">Social</th>
                      <th className="px-3 py-2 text-center">Resilience</th>
                      <th className="px-3 py-2 text-center">TOTAL</th>
                    </tr>
                  </thead>
                  <tbody>
                    {leaderboard.map((entry, i) => (
                      <LeaderboardRow key={entry.country.id} entry={entry} rank={i + 1} />
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}
        </main>
      </div>

      {/* Event Trigger Modal */}
      <Modal isOpen={eventModalOpen} onClose={() => setEventModalOpen(false)} title="TRIGGER EVENT">
        {selectedEvent && (
          <div className="space-y-4">
            <div className="flex items-center gap-3">
              <span className="text-4xl">{selectedEvent.icon}</span>
              <div>
                <h3 className="font-heading text-lg text-primary">{selectedEvent.name}</h3>
                <p className="text-xs text-muted">{selectedEvent.effect}</p>
              </div>
            </div>
            <div>
              <label className="block text-[9px] font-mono text-muted uppercase tracking-[3px] mb-1.5">Affected Countries</label>
              <div className="grid grid-cols-2 gap-1">
                {countries.map(c => (
                  <label key={c.id} className="flex items-center gap-2 px-3 py-2 rounded-xl bg-card/60 border border-[rgba(0,240,255,0.06)] cursor-pointer hover:bg-[rgba(0,240,255,0.04)] transition-colors">
                    <input
                      type="checkbox"
                      checked={affectedCountries.includes(c.name)}
                      onChange={e => {
                        if (e.target.checked) {
                          setAffectedCountries([...affectedCountries, c.name]);
                        } else {
                          setAffectedCountries(affectedCountries.filter(n => n !== c.name));
                        }
                      }}
                      className="accent-[#00f0ff]"
                    />
                    <span className="text-[10px] font-mono tracking-wider">{c.flag_emoji} {c.name}</span>
                  </label>
                ))}
              </div>
              <button onClick={() => setAffectedCountries(countries.map(c => c.name))}
                className="text-[10px] font-mono text-neon-cyan mt-2 hover:underline tracking-wider">Select All</button>
            </div>
            <Button variant="danger" size="md" className="w-full" loading={loading} onClick={triggerEvent}>
              ⚡ CONFIRM & TRIGGER EVENT
            </Button>
          </div>
        )}
      </Modal>
    </div>
  );
}
