import { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { 
  ArrowLeftRight, 
  Plus, 
  History, 
  Send, 
  Inbox, 
  ShoppingBag,
  Globe
} from 'lucide-react';
import { useAuth } from '../hooks/useAuth';
import { useCountry } from '../hooks/useCountry';
import { useResources } from '../hooks/useResources';
import { useGameState } from '../hooks/useGameState';
import { useTrades } from '../hooks/useTrades';
import { useMarket } from '../hooks/useMarket';
import { useToast } from '../components/ui/Toast';
import { Card } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Badge } from '../components/ui/Badge';
import { supabase } from '../lib/supabase';
import { RESOURCE_TYPES } from '../lib/constants';
import { validateTrade } from '../lib/gameLogic';
import type { ResourceType, TradePayload } from '../types';

export function Trade() {
  const { user } = useAuth();
  const { country, allCountries } = useCountry(user?.id);
  const { resources } = useResources(country?.id);
  const { gameState } = useGameState();
  const { incomingOffers, sentOffers, refetch: refetchTrades } = useTrades(country?.id);
  const { openPosts, myPosts, refetch: refetchMarket } = useMarket(country?.id);
  const { addToast } = useToast();

  const [activeTab, setActiveTab] = useState<'marketplace' | 'propose' | 'incoming' | 'sent'>('marketplace');
  const [loading, setLoading] = useState(false);

  // Propose state
  const [toCountryId, setToCountryId] = useState('');
  const [offerResource, setOfferResource] = useState<ResourceType | ''>('');
  const [offerQty, setOfferQty] = useState(0);
  const [offerGC, setOfferGC] = useState(0);
  const [wantResource, setWantResource] = useState<ResourceType | ''>('');
  const [wantQty, setWantQty] = useState(0);
  const [wantGC, setWantGC] = useState(0);
  const [message, setMessage] = useState('');

  const otherCountries = allCountries.filter(c => c.id !== country?.id);
  const pendingIncoming = incomingOffers.filter(o => o.status === 'pending');

  const handlePropose = async () => {
    if (!country || !gameState) return;

    const offering: TradePayload = {};
    const requesting: TradePayload = {};

    if (offerResource && offerQty > 0) { offering.resource = offerResource; offering.qty = offerQty; }
    if (offerGC > 0) offering.gc = offerGC;
    if (wantResource && wantQty > 0) { requesting.resource = wantResource; requesting.qty = wantQty; }
    if (wantGC > 0) requesting.gc = wantGC;

    const validation = validateTrade({ offering_json: offering, requesting_json: requesting }, resources, country);
    if (!validation.valid) {
      addToast(validation.reason, 'error');
      return;
    }

    setLoading(true);
    try {
      const { error } = await supabase.from('trade_offers').insert({
        from_country_id: country.id,
        to_country_id: toCountryId,
        round_number: gameState.current_round,
        status: 'pending',
        offering_json: offering,
        requesting_json: requesting,
        message,
      });
      if (error) throw error;

      addToast('Trade proposal broadcasted', 'success', '📤');
      setActiveTab('sent');
      refetchTrades();
    } catch (e: any) {
      addToast(e.message, 'error');
    } finally {
      setLoading(false);
    }
  };

  const tabs = [
    { id: 'marketplace', label: 'Marketplace', icon: ShoppingBag },
    { id: 'propose', label: 'New Proposal', icon: Plus },
    { id: 'incoming', label: 'Incoming', icon: Inbox, count: pendingIncoming.length },
    { id: 'sent', label: 'Sent', icon: Send },
  ];

  return (
    <div className="space-y-8 pb-12">
      {/* Header */}
      <section className="flex flex-col md:flex-row md:items-end justify-between gap-6">
        <div className="space-y-2">
          <div className="flex items-center space-x-3">
            <Badge variant="neon" size="md">Global Network</Badge>
            <span className="text-zinc-500 font-mono text-[10px] tracking-[0.4em] uppercase">Encrypted Channel</span>
          </div>
          <h1 className="text-4xl md:text-5xl font-black font-heading tracking-tighter text-white uppercase">
            Trade <span className="text-neon-cyan">Terminal</span>
          </h1>
        </div>

        <div className="flex items-center space-x-1 glass-panel p-1.5 rounded-2xl border-white/5">
          {tabs.map((tab) => (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id as any)}
              className={`
                relative px-6 py-2.5 rounded-xl text-xs font-heading font-black uppercase tracking-widest transition-all duration-300 flex items-center gap-2
                ${activeTab === tab.id ? 'text-white' : 'text-zinc-500 hover:text-zinc-300'}
              `}
            >
              <tab.icon className="w-4 h-4" />
              <span>{tab.label}</span>
              {tab.count ? (
                <span className="ml-1 w-5 h-5 rounded-full bg-neon-magenta text-white text-[10px] flex items-center justify-center animate-pulse">
                  {tab.count}
                </span>
              ) : null}
              {activeTab === tab.id && (
                <motion.div
                  layoutId="trade-tab-active"
                  className="absolute inset-0 bg-white/5 border border-white/10 rounded-xl"
                  transition={{ type: "spring", bounce: 0.2, duration: 0.6 }}
                />
              )}
            </button>
          ))}
        </div>
      </section>

      <AnimatePresence mode="wait">
        <motion.div
          key={activeTab}
          initial={{ opacity: 0, y: 10 }}
          animate={{ opacity: 1, y: 0 }}
          exit={{ opacity: 0, y: -10 }}
          transition={{ duration: 0.3 }}
        >
          {activeTab === 'marketplace' && (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {openPosts.length > 0 ? (
                openPosts.map((post, idx) => (
                  <Card key={post.id} variant="glass" className="group">
                    <div className="space-y-4">
                      <div className="flex justify-between items-start">
                        <div className="flex items-center gap-3">
                          <div className="w-10 h-10 rounded-lg bg-white/5 flex items-center justify-center border border-white/10">
                            <Globe className="w-5 h-5 text-neon-cyan" />
                          </div>
                          <div>
                            <div className="text-[10px] font-black text-zinc-500 uppercase tracking-widest">Origin</div>
                            <div className="text-sm font-bold text-white">{post.from_country?.name}</div>
                          </div>
                        </div>
                        <Badge variant="outline" size="xs">Round {post.round_number}</Badge>
                      </div>

                      <div className="grid grid-cols-2 gap-4 py-4 border-y border-white/5">
                        <div className="space-y-1">
                          <div className="text-[9px] font-black text-zinc-500 uppercase tracking-widest">Offering</div>
                          <div className="text-lg font-heading font-black text-neon-cyan">
                            {post.offering_json.qty} {post.offering_json.resource}
                          </div>
                        </div>
                        <div className="space-y-1 text-right">
                          <div className="text-[9px] font-black text-zinc-500 uppercase tracking-widest">Requesting</div>
                          <div className="text-lg font-heading font-black text-neon-magenta">
                            {post.requesting_json.qty} {post.requesting_json.resource}
                          </div>
                        </div>
                      </div>

                      <Button variant="neon" size="sm" className="w-full">
                        Initialize Trade
                      </Button>
                    </div>
                  </Card>
                ))
              ) : (
                <div className="col-span-full py-24 text-center glass-panel rounded-3xl border-dashed border-white/10">
                  <ShoppingBag className="w-12 h-12 text-zinc-700 mx-auto mb-4" />
                  <h3 className="text-xl font-heading font-black text-zinc-500 uppercase">No Active Listings</h3>
                  <p className="text-zinc-600 text-sm mt-2">Global markets are currently quiet.</p>
                </div>
              )}
            </div>
          )}

          {activeTab === 'propose' && (
            <div className="max-w-4xl mx-auto">
              <Card variant="neon" padding="xl" className="border-neon-cyan/20">
                <form onSubmit={(e) => { e.preventDefault(); handlePropose(); }} className="space-y-8">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                    {/* Receiver */}
                    <div className="space-y-4 col-span-full">
                      <label className="text-[10px] font-black text-zinc-500 uppercase tracking-[0.4em] ml-1">
                        Select Counterparty
                      </label>
                      <select
                        value={toCountryId}
                        onChange={(e) => setToCountryId(e.target.value)}
                        className="w-full bg-white/5 border border-white/10 rounded-xl py-4 px-4 text-white focus:outline-none focus:border-neon-cyan/50 focus:bg-white/10 transition-all font-medium appearance-none"
                        required
                      >
                        <option value="" className="bg-zinc-900">Select Nation...</option>
                        {otherCountries.map(c => (
                          <option key={c.id} value={c.id} className="bg-zinc-900">{c.name}</option>
                        ))}
                      </select>
                    </div>

                    {/* Offering */}
                    <div className="space-y-4">
                      <h3 className="text-sm font-heading font-black text-neon-cyan uppercase tracking-widest border-b border-neon-cyan/20 pb-2">
                        Your Offer
                      </h3>
                      <div className="space-y-4">
                        <select
                          value={offerResource}
                          onChange={(e) => setOfferResource(e.target.value as any)}
                          className="w-full bg-white/5 border border-white/10 rounded-xl py-3 px-4 text-white focus:outline-none focus:border-neon-cyan/50 transition-all"
                        >
                          <option value="" className="bg-zinc-900">Select Resource...</option>
                          {RESOURCE_TYPES.map(r => (
                            <option key={r} value={r} className="bg-zinc-900">{r.toUpperCase()}</option>
                          ))}
                        </select>
                        <input
                          type="number"
                          value={offerQty}
                          onChange={(e) => setOfferQty(Number(e.target.value))}
                          placeholder="Quantity"
                          className="w-full bg-white/5 border border-white/10 rounded-xl py-3 px-4 text-white focus:outline-none focus:border-neon-cyan/50 transition-all"
                        />
                      </div>
                    </div>

                    {/* Requesting */}
                    <div className="space-y-4">
                      <h3 className="text-sm font-heading font-black text-neon-magenta uppercase tracking-widest border-b border-neon-magenta/20 pb-2">
                        Your Request
                      </h3>
                      <div className="space-y-4">
                        <select
                          value={wantResource}
                          onChange={(e) => setWantResource(e.target.value as any)}
                          className="w-full bg-white/5 border border-white/10 rounded-xl py-3 px-4 text-white focus:outline-none focus:border-neon-magenta/50 transition-all"
                        >
                          <option value="" className="bg-zinc-900">Select Resource...</option>
                          {RESOURCE_TYPES.map(r => (
                            <option key={r} value={r} className="bg-zinc-900">{r.toUpperCase()}</option>
                          ))}
                        </select>
                        <input
                          type="number"
                          value={wantQty}
                          onChange={(e) => setWantQty(Number(e.target.value))}
                          placeholder="Quantity"
                          className="w-full bg-white/5 border border-white/10 rounded-xl py-3 px-4 text-white focus:outline-none focus:border-neon-magenta/50 transition-all"
                        />
                      </div>
                    </div>

                    <div className="col-span-full space-y-4">
                      <label className="text-[10px] font-black text-zinc-500 uppercase tracking-[0.4em] ml-1">
                        Diplomatic Message
                      </label>
                      <textarea
                        value={message}
                        onChange={(e) => setMessage(e.target.value)}
                        className="w-full bg-white/5 border border-white/10 rounded-xl py-4 px-4 text-white focus:outline-none focus:border-neon-cyan/50 min-h-[100px] resize-none"
                        placeholder="State your terms, operator..."
                      />
                    </div>
                  </div>

                  <Button
                    type="submit"
                    variant="neon"
                    size="xl"
                    className="w-full"
                    isLoading={loading}
                    icon={<Send className="w-5 h-5" />}
                  >
                    Transmit Proposal
                  </Button>
                </form>
              </Card>
            </div>
          )}

          {(activeTab === 'incoming' || activeTab === 'sent') && (
            <div className="space-y-4">
              {(activeTab === 'incoming' ? incomingOffers : sentOffers).length > 0 ? (
                (activeTab === 'incoming' ? incomingOffers : sentOffers).map((offer, idx) => (
                  <Card key={offer.id} variant="glass" padding="md" className="group">
                    <div className="flex flex-col md:flex-row items-center justify-between gap-6">
                      <div className="flex items-center gap-6">
                        <div className="w-12 h-12 rounded-xl bg-white/5 flex items-center justify-center border border-white/10">
                          {activeTab === 'incoming' ? <Inbox className="w-6 h-6 text-neon-cyan" /> : <Send className="w-6 h-6 text-neon-magenta" />}
                        </div>
                        <div>
                          <div className="text-[10px] font-black text-zinc-500 uppercase tracking-widest">
                            {activeTab === 'incoming' ? 'From' : 'To'} {activeTab === 'incoming' ? offer.from_country?.name : offer.to_country?.name}
                          </div>
                          <div className="flex items-center gap-2 mt-1">
                            <span className="text-white font-bold">{offer.offering_json.qty} {offer.offering_json.resource}</span>
                            <ArrowLeftRight className="w-3 h-3 text-zinc-600" />
                            <span className="text-white font-bold">{offer.requesting_json.qty} {offer.requesting_json.resource}</span>
                          </div>
                        </div>
                      </div>

                      <div className="flex items-center gap-4">
                        <Badge variant={offer.status === 'pending' ? 'warning' : offer.status === 'accepted' ? 'success' : 'error'}>
                          {offer.status}
                        </Badge>
                        {offer.status === 'pending' && activeTab === 'incoming' && (
                          <div className="flex items-center gap-2">
                            <Button variant="primary" size="sm">Accept</Button>
                            <Button variant="danger" size="sm">Reject</Button>
                          </div>
                        )}
                        {offer.status === 'pending' && activeTab === 'sent' && (
                          <Button variant="ghost" size="sm" className="text-red-400">Cancel</Button>
                        )}
                      </div>
                    </div>
                  </Card>
                ))
              ) : (
                <div className="py-24 text-center glass-panel rounded-3xl border-dashed border-white/10">
                  <History className="w-12 h-12 text-zinc-700 mx-auto mb-4" />
                  <h3 className="text-xl font-heading font-black text-zinc-500 uppercase">No Trade History</h3>
                  <p className="text-zinc-600 text-sm mt-2">Your diplomatic records are currently empty.</p>
                </div>
              )}
            </div>
          )}
        </motion.div>
      </AnimatePresence>
    </div>
  );
}
