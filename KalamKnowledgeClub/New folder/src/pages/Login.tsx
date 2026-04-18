import { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { 
  Shield, 
  Lock, 
  Mail, 
  Globe, 
  ArrowRight, 
  Activity,
  Zap,
  Cpu
} from 'lucide-react';
import { useAuth } from '../hooks/useAuth';
import { Button } from '../components/ui/Button';
import { Badge } from '../components/ui/Badge';

export function Login() {
  const { signIn } = useAuth();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [isReady, setIsReady] = useState(false);

  useEffect(() => {
    const timer = setTimeout(() => setIsReady(true), 500);
    return () => clearTimeout(timer);
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await signIn(email, password);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Authentication failed';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  const containerVariants = {
    hidden: { opacity: 0 },
    visible: { 
      opacity: 1,
      transition: { 
        staggerChildren: 0.1,
        delayChildren: 0.3
      }
    }
  };

  const itemVariants = {
    hidden: { opacity: 0, y: 20 },
    visible: { 
      opacity: 1, 
      y: 0,
      transition: { duration: 0.8, ease: "easeOut" }
    }
  };

  return (
    <div className="min-h-screen bg-base flex items-center justify-center p-6 relative overflow-hidden">
      {/* Cinematic Background */}
      <div className="fixed inset-0 z-0">
        <div className="grid-bg opacity-30" />
        <motion.div 
          animate={{ 
            scale: [1, 1.1, 1],
            opacity: [0.3, 0.5, 0.3] 
          }}
          transition={{ duration: 20, repeat: Infinity }}
          className="absolute top-[-10%] left-[-10%] w-[60%] h-[60%] bg-neon-cyan/10 blur-[150px] rounded-full"
        />
        <motion.div 
          animate={{ 
            scale: [1.1, 1, 1.1],
            opacity: [0.2, 0.4, 0.2] 
          }}
          transition={{ duration: 25, repeat: Infinity }}
          className="absolute bottom-[-10%] right-[-10%] w-[50%] h-[50%] bg-neon-magenta/5 blur-[120px] rounded-full"
        />
        <div className="absolute inset-0 bg-gradient-to-b from-base via-transparent to-base opacity-60" />
      </div>

      <AnimatePresence>
        {isReady && (
          <motion.div 
            variants={containerVariants}
            initial="hidden"
            animate="visible"
            className="relative z-10 w-full max-w-6xl mx-auto grid grid-cols-1 lg:grid-cols-2 gap-16 items-center"
          >
            {/* Left Side: Brand & Stats */}
            <div className="space-y-12">
              <motion.div variants={itemVariants} className="space-y-6">
                <div className="flex items-center space-x-3">
                  <Badge variant="neon" size="md" icon={<Shield className="w-3 h-3" />}>
                    Secure Terminal
                  </Badge>
                  <div className="h-px w-12 bg-white/10" />
                  <span className="text-[10px] font-mono font-black text-zinc-500 uppercase tracking-[0.4em]">
                    v4.2.0-stable
                  </span>
                </div>

                <h1 className="text-6xl md:text-8xl font-black font-heading tracking-tighter text-white leading-[0.9]">
                  KALAM <br />
                  <span className="text-transparent bg-clip-text bg-gradient-to-r from-neon-cyan via-neon-violet to-neon-magenta">
                    KNOWLEDGE.
                  </span>
                </h1>

                <p className="text-zinc-400 text-lg max-w-md leading-relaxed font-medium">
                  Forge your nation's destiny in the most advanced geopolitical simulation ever created.
                </p>
              </motion.div>

              {/* Real-time Metrics */}
              <motion.div variants={itemVariants} className="grid grid-cols-3 gap-8">
                {[
                  { label: 'Active Nations', value: '12', icon: Globe },
                  { label: 'Market Cap', value: '$4.2B', icon: Zap },
                  { label: 'System Load', value: '14%', icon: Cpu },
                ].map((stat, i) => (
                  <div key={i} className="space-y-2">
                    <div className="flex items-center space-x-2 text-neon-cyan">
                      <stat.icon className="w-4 h-4" />
                      <span className="text-[10px] font-black uppercase tracking-widest text-zinc-500">{stat.label}</span>
                    </div>
                    <div className="text-2xl font-heading font-black text-white">{stat.value}</div>
                  </div>
                ))}
              </motion.div>
            </div>

            {/* Right Side: Login Form */}
            <motion.div variants={itemVariants}>
              <div className="relative group">
                {/* Glow Effect */}
                <div className="absolute -inset-1 bg-gradient-to-r from-neon-cyan/20 to-neon-magenta/20 rounded-[2.5rem] blur-2xl opacity-50 group-hover:opacity-100 transition duration-1000" />
                
                <div className="relative glass-panel rounded-[2rem] p-10 md:p-12 border-white/5 shadow-2xl overflow-hidden">
                  <div className="absolute top-0 left-0 right-0 h-1 bg-gradient-to-r from-transparent via-white/10 to-transparent" />
                  
                  <div className="space-y-8">
                    <div className="space-y-2">
                      <h2 className="text-2xl font-heading font-black text-white uppercase tracking-widest">
                        Initialize
                      </h2>
                      <p className="text-zinc-500 text-xs font-bold uppercase tracking-widest flex items-center gap-2">
                        <Lock className="w-3 h-3" /> Secure Credential Entry
                      </p>
                    </div>

                    <form onSubmit={handleSubmit} className="space-y-6">
                      <div className="space-y-4">
                        <div className="space-y-2">
                          <label className="text-[10px] font-black text-zinc-500 uppercase tracking-[0.3em] ml-1">
                            Identification
                          </label>
                          <div className="relative">
                            <Mail className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-zinc-500" />
                            <input
                              type="email"
                              value={email}
                              onChange={(e) => setEmail(e.target.value)}
                              className="w-full bg-white/5 border border-white/10 rounded-xl py-4 pl-12 pr-4 text-white placeholder:text-zinc-700 focus:outline-none focus:border-neon-cyan/50 focus:bg-white/10 transition-all font-medium"
                              placeholder="operator@kalam.net"
                              required
                            />
                          </div>
                        </div>

                        <div className="space-y-2">
                          <label className="text-[10px] font-black text-zinc-500 uppercase tracking-[0.3em] ml-1">
                            Access Key
                          </label>
                          <div className="relative">
                            <Lock className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-zinc-500" />
                            <input
                              type="password"
                              value={password}
                              onChange={(e) => setPassword(e.target.value)}
                              className="w-full bg-white/5 border border-white/10 rounded-xl py-4 pl-12 pr-4 text-white placeholder:text-zinc-700 focus:outline-none focus:border-neon-cyan/50 focus:bg-white/10 transition-all font-medium"
                              placeholder="••••••••••••"
                              required
                            />
                          </div>
                        </div>
                      </div>

                      {error && (
                        <motion.div 
                          initial={{ opacity: 0, height: 0 }}
                          animate={{ opacity: 1, height: 'auto' }}
                          className="text-red-400 text-xs font-bold uppercase tracking-wider flex items-center gap-2 bg-red-500/10 p-3 rounded-lg border border-red-500/20"
                        >
                          <Activity className="w-4 h-4" /> {error}
                        </motion.div>
                      )}

                      <Button
                        type="submit"
                        variant="primary"
                        size="xl"
                        className="w-full"
                        isLoading={loading}
                        icon={<ArrowRight className="w-5 h-5" />}
                      >
                        Authenticate
                      </Button>
                    </form>

                    <div className="flex items-center justify-between pt-4 border-t border-white/5">
                      <button className="text-[10px] font-black text-zinc-600 uppercase tracking-widest hover:text-white transition-colors">
                        Request Access
                      </button>
                      <button className="text-[10px] font-black text-zinc-600 uppercase tracking-widest hover:text-white transition-colors">
                        Protocol Guide
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>
      
      {/* Footer Decoration */}
      <div className="fixed bottom-8 left-1/2 -translate-x-1/2 flex items-center space-x-8 opacity-20">
        <div className="h-px w-32 bg-gradient-to-r from-transparent to-white" />
        <span className="text-[10px] font-mono tracking-[0.5em] text-white uppercase whitespace-nowrap">
          Neural Interface Stable
        </span>
        <div className="h-px w-32 bg-gradient-to-l from-transparent to-white" />
      </div>
    </div>
  );
}
