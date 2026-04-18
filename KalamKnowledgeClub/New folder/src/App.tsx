import { lazy, Suspense, useEffect, useState } from 'react';
import { HashRouter, Routes, Route, Navigate, useLocation } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { useAuth } from './hooks/useAuth';
import Layout from './components/layout/Layout';

// Lazy load pages
const Login = lazy(() => import('./pages/Login').then(m => ({ default: m.Login })));
const Dashboard = lazy(() => import('./pages/Dashboard').then(m => ({ default: m.Dashboard })));
const Trade = lazy(() => import('./pages/Trade').then(m => ({ default: m.Trade })));
const Crafting = lazy(() => import('./pages/Crafting').then(m => ({ default: m.Crafting })));
const CraftingGuide = lazy(() => import('./pages/CraftingGuide').then(m => ({ default: m.CraftingGuide })));
const Leaderboard = lazy(() => import('./pages/Leaderboard').then(m => ({ default: m.Leaderboard })));
const Admin = lazy(() => import('./pages/Admin').then(m => ({ default: m.Admin })));

function CustomCursor() {
  const [position, setPosition] = useState({ x: 0, y: 0 });
  const [isPointer, setIsPointer] = useState(false);

  useEffect(() => {
    const handleMouseMove = (e: MouseEvent) => {
      setPosition({ x: e.clientX, y: e.clientY });
      const target = e.target as HTMLElement;
      setIsPointer(window.getComputedStyle(target).cursor === 'pointer');
    };
    window.addEventListener('mousemove', handleMouseMove);
    return () => window.removeEventListener('mousemove', handleMouseMove);
  }, []);

  return (
    <motion.div
      className="fixed top-0 left-0 w-8 h-8 pointer-events-none z-[10000] hidden md:block"
      animate={{
        x: position.x - 16,
        y: position.y - 16,
        scale: isPointer ? 1.5 : 1,
      }}
      transition={{ type: 'spring', damping: 25, stiffness: 250, mass: 0.5 }}
    >
      <div className="w-full h-full border border-neon-cyan/50 rounded-full flex items-center justify-center relative">
        <div className="w-1 h-1 bg-neon-cyan rounded-full shadow-[0_0_8px_rgba(0,240,255,1)]" />
        <div className="absolute inset-0 border border-neon-cyan/20 rounded-full scale-125" />
      </div>
    </motion.div>
  );
}

function LoadingScreen() {
  return (
    <div className="fixed inset-0 bg-base flex items-center justify-center z-[9999]">
      <div className="relative">
        <motion.div
          animate={{ rotate: 360 }}
          transition={{ duration: 2, repeat: Infinity, ease: "linear" }}
          className="w-24 h-24 border-t-2 border-r-2 border-neon-cyan rounded-full"
        />
        <div className="absolute inset-0 flex items-center justify-center">
          <span className="font-heading text-xs font-black tracking-[0.2em] text-neon-cyan animate-pulse">INIT</span>
        </div>
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: [0, 1, 0] }}
          transition={{ duration: 1.5, repeat: Infinity }}
          className="absolute -bottom-12 left-1/2 -translate-x-1/2 whitespace-nowrap text-[10px] font-mono tracking-[0.4em] text-muted uppercase"
        >
          Synchronizing Neural Interface
        </motion.div>
      </div>
    </div>
  );
}

function ProtectedRoute({ children, requireAdmin = false }: { children: React.ReactNode; requireAdmin?: boolean }) {
  const { user, loading, isAdmin } = useAuth();

  if (loading) return <LoadingScreen />;
  if (!user) return <Navigate to="/login" replace />;
  if (requireAdmin && !isAdmin) return <Navigate to="/" replace />;

  return <>{children}</>;
}

function App() {
  return (
    <HashRouter>
      <CustomCursor />
      <Suspense fallback={<LoadingScreen />}>
        <Routes>
          <Route path="/login" element={<Login />} />
          
          <Route element={<Layout />}>
            <Route path="/" element={
              <ProtectedRoute>
                <Dashboard />
              </ProtectedRoute>
            } />
            <Route path="/trade" element={
              <ProtectedRoute>
                <Trade />
              </ProtectedRoute>
            } />
            <Route path="/crafting" element={
              <ProtectedRoute>
                <Crafting />
              </ProtectedRoute>
            } />
            <Route path="/crafting-guide" element={
              <ProtectedRoute>
                <CraftingGuide />
              </ProtectedRoute>
            } />
            <Route path="/leaderboard" element={
              <ProtectedRoute>
                <Leaderboard />
              </ProtectedRoute>
            } />
            <Route path="/admin" element={
              <ProtectedRoute requireAdmin>
                <Admin />
              </ProtectedRoute>
            } />
          </Route>

          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </Suspense>
    </HashRouter>
  );
}

export default App;
