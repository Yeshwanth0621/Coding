import React from 'react';
import { NavLink } from 'react-router-dom';
import { motion } from 'framer-motion';
import { 
  LayoutDashboard, 
  ArrowLeftRight, 
  Hammer, 
  Trophy, 
  Settings, 
  LogOut,
  User
} from 'lucide-react';
import { cn } from '../../lib/utils';
import { useAuth } from '../../hooks/useAuth';

const Navbar: React.FC = () => {
  const { user, signOut } = useAuth();

  const navItems = [
    { to: '/', icon: LayoutDashboard, label: 'Dashboard' },
    { to: '/trade', icon: ArrowLeftRight, label: 'Trade' },
    { to: '/crafting', icon: Hammer, label: 'Crafting' },
    { to: '/leaderboard', icon: Trophy, label: 'Rankings' },
  ];

  return (
    <nav className="fixed top-0 left-0 right-0 z-50 px-6 py-4">
      <div className="max-w-7xl mx-auto flex items-center justify-between">
        {/* Logo */}
        <NavLink to="/" className="flex items-center space-x-2 group">
          <div className="w-10 h-10 bg-white rounded-lg flex items-center justify-center group-hover:bg-neon-cyan transition-colors duration-300">
            <span className="text-black font-black text-xl font-heading">K</span>
          </div>
          <div className="flex flex-col">
            <span className="text-white font-black text-lg font-heading tracking-tighter leading-none">KALAM</span>
            <span className="text-neon-cyan text-[10px] font-bold tracking-[0.2em] leading-none uppercase">Knowledge Club</span>
          </div>
        </NavLink>

        {/* Nav Links - Desktop */}
        <div className="hidden md:flex items-center space-x-1 glass-panel px-2 py-1.5 rounded-xl border-white/5">
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) => cn(
                "relative px-4 py-2 text-sm font-heading font-bold uppercase tracking-widest transition-all duration-300",
                isActive ? "text-white" : "text-zinc-500 hover:text-zinc-300"
              )}
            >
              {({ isActive }) => (
                <>
                  <div className="flex items-center space-x-2 relative z-10">
                    <item.icon className={cn("w-4 h-4", isActive && "text-neon-cyan")} />
                    <span>{item.label}</span>
                  </div>
                  {isActive && (
                    <motion.div
                      layoutId="nav-active"
                      className="absolute inset-0 bg-white/5 rounded-lg border border-white/10"
                      transition={{ type: "spring", bounce: 0.2, duration: 0.6 }}
                    />
                  )}
                </>
              )}
            </NavLink>
          ))}
        </div>

        {/* User Actions */}
        <div className="flex items-center space-x-4">
          {user ? (
            <div className="flex items-center space-x-3">
              <div className="hidden sm:flex flex-col items-end mr-2">
                <span className="text-white text-sm font-bold font-heading">{user.email?.split('@')[0]}</span>
                <span className="text-neon-cyan text-[10px] font-bold uppercase tracking-wider">Level 42 Operative</span>
              </div>
              <button 
                onClick={() => signOut()}
                className="w-10 h-10 rounded-full glass-panel flex items-center justify-center text-zinc-400 hover:text-white hover:border-white/20 transition-all"
              >
                <LogOut className="w-5 h-5" />
              </button>
              <div className="w-10 h-10 rounded-full bg-gradient-to-tr from-neon-cyan to-neon-violet p-[1px]">
                <div className="w-full h-full rounded-full bg-zinc-900 flex items-center justify-center">
                  <User className="w-5 h-5 text-white" />
                </div>
              </div>
            </div>
          ) : (
            <NavLink to="/login">
              <button className="px-6 py-2 bg-white text-black font-heading font-black text-sm uppercase tracking-widest rounded-md hover:bg-neon-cyan transition-colors">
                Initialize
              </button>
            </NavLink>
          )}
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
