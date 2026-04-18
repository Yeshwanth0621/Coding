import React from 'react';
import { NavLink } from 'react-router-dom';
import { motion } from 'framer-motion';
import { 
  LayoutDashboard, 
  ArrowLeftRight, 
  Hammer, 
  Trophy 
} from 'lucide-react';
import { cn } from '../../lib/utils';

const BottomNav: React.FC = () => {
  const navItems = [
    { to: '/', icon: LayoutDashboard, label: 'Dash' },
    { to: '/trade', icon: ArrowLeftRight, label: 'Trade' },
    { to: '/crafting', icon: Hammer, label: 'Craft' },
    { to: '/leaderboard', icon: Trophy, label: 'Rank' },
  ];

  return (
    <nav className="fixed bottom-0 left-0 right-0 z-50 px-4 pb-6 pt-2">
      <div className="max-w-md mx-auto glass-panel rounded-2xl flex items-center justify-around p-2 border-white/10 shadow-[0_-10px_30px_rgba(0,0,0,0.5)]">
        {navItems.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            className={({ isActive }) => cn(
              "relative flex flex-col items-center justify-center py-2 px-4 transition-all duration-300",
              isActive ? "text-neon-cyan" : "text-zinc-500"
            )}
          >
            {({ isActive }) => (
              <>
                <item.icon className={cn("w-6 h-6 mb-1 transition-transform", isActive && "scale-110")} />
                <span className="text-[10px] font-bold font-heading uppercase tracking-tighter">
                  {item.label}
                </span>
                {isActive && (
                  <motion.div
                    layoutId="bottom-nav-indicator"
                    className="absolute -top-1 w-1 h-1 bg-neon-cyan rounded-full shadow-[0_0_10px_rgba(0,240,255,0.8)]"
                  />
                )}
              </>
            )}
          </NavLink>
        ))}
      </div>
    </nav>
  );
};

export default BottomNav;
