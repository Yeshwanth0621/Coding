import React from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import Navbar from './Navbar';
import BottomNav from './BottomNav';
import { Particles } from '../ui/Particles';

const Layout: React.FC = () => {
  const location = useLocation();

  return (
    <div className="min-h-screen relative flex flex-col">
      {/* Global Background Elements */}
      <div className="fixed inset-0 z-0 pointer-events-none">
        <div className="grid-bg opacity-40" />
        <Particles />
        <div className="absolute inset-0 bg-gradient-to-b from-transparent via-base to-base opacity-80" />
        {/* Subtle animated flares */}
        <motion.div 
          animate={{ 
            opacity: [0.1, 0.2, 0.1],
            scale: [1, 1.2, 1],
          }}
          transition={{ duration: 10, repeat: Infinity }}
          className="absolute -top-[20%] -left-[10%] w-[50%] h-[50%] bg-neon-cyan/10 blur-[120px] rounded-full"
        />
        <motion.div 
          animate={{ 
            opacity: [0.05, 0.15, 0.05],
            scale: [1.2, 1, 1.2],
          }}
          transition={{ duration: 15, repeat: Infinity }}
          className="absolute top-[40%] -right-[10%] w-[40%] h-[40%] bg-neon-magenta/5 blur-[100px] rounded-full"
        />
      </div>

      <Navbar />

      <main className="flex-1 relative z-10 pt-24 pb-20 md:pb-8 px-4 md:px-8 max-w-7xl mx-auto w-full">
        <AnimatePresence mode="wait">
          <motion.div
            key={location.pathname}
            initial={{ opacity: 0, y: 10, filter: 'blur(10px)' }}
            animate={{ opacity: 1, y: 0, filter: 'blur(0px)' }}
            exit={{ opacity: 0, y: -10, filter: 'blur(10px)' }}
            transition={{ duration: 0.4, ease: [0.23, 1, 0.32, 1] }}
            className="h-full"
          >
            <Outlet />
          </motion.div>
        </AnimatePresence>
      </main>

      <div className="md:hidden">
        <BottomNav />
      </div>

      {/* Subtle Scanline Overlay */}
      <div className="fixed inset-0 pointer-events-none z-[9999] opacity-[0.03] scanline" />
    </div>
  );
};

export default Layout;
