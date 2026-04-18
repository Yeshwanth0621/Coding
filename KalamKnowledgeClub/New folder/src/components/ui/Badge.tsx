import React from 'react';
import { motion } from 'framer-motion';
import { cn } from '../../lib/utils';

interface BadgeProps {
  children: React.ReactNode;
  variant?: 'primary' | 'secondary' | 'outline' | 'neon' | 'success' | 'warning' | 'error' | 'info';
  size?: 'xs' | 'sm' | 'md';
  className?: string;
  icon?: React.ReactNode;
}

const Badge: React.FC<BadgeProps> = ({ 
  children, 
  variant = 'primary', 
  size = 'sm', 
  className,
  icon 
}) => {
  const variants = {
    primary: 'bg-white/10 text-white border-white/20',
    secondary: 'bg-zinc-800 text-zinc-300 border-zinc-700',
    outline: 'bg-transparent border border-white/20 text-white/80',
    neon: 'bg-neon-cyan/10 text-neon-cyan border-neon-cyan/50 shadow-[0_0_10px_rgba(0,240,255,0.1)]',
    success: 'bg-green-500/10 text-green-400 border-green-500/30',
    warning: 'bg-yellow-500/10 text-yellow-400 border-yellow-500/30',
    error: 'bg-red-500/10 text-red-400 border-red-500/30',
    info: 'bg-blue-500/10 text-blue-400 border-blue-500/30',
  };

  const sizes = {
    xs: 'px-1.5 py-0.5 text-[10px]',
    sm: 'px-2 py-0.5 text-xs',
    md: 'px-3 py-1 text-sm',
  };

  return (
    <motion.span
      initial={{ scale: 0.9, opacity: 0 }}
      animate={{ scale: 1, opacity: 1 }}
      className={cn(
        'inline-flex items-center font-heading font-semibold tracking-wider rounded-sm border uppercase',
        variants[variant],
        sizes[size],
        className
      )}
    >
      {icon && <span className="mr-1.5">{icon}</span>}
      {children}
    </motion.span>
  );
};

export { Badge };
