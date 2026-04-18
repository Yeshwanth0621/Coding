import React from 'react';
import { motion, HTMLMotionProps } from 'framer-motion';
import { cn } from '../../lib/utils';

interface ButtonProps extends Omit<HTMLMotionProps<"button">, "children"> {
  variant?: 'primary' | 'secondary' | 'outline' | 'ghost' | 'neon' | 'danger';
  size?: 'sm' | 'md' | 'lg' | 'xl';
  isLoading?: boolean;
  children: React.ReactNode;
  icon?: React.ReactNode;
}

const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, variant = 'primary', size = 'md', isLoading, children, icon, ...props }, ref) => {
    const variants = {
      primary: 'bg-white text-black hover:bg-white/90 shadow-[0_0_20px_rgba(255,255,255,0.1)]',
      secondary: 'glass-panel text-white hover:bg-white/5 border-white/10',
      outline: 'border border-white/20 text-white hover:border-white/40 hover:bg-white/5',
      ghost: 'text-white/70 hover:text-white hover:bg-white/5',
      neon: 'bg-transparent border border-neon-cyan text-neon-cyan hover:bg-neon-cyan/10 shadow-[0_0_15px_rgba(0,240,255,0.2)]',
      danger: 'bg-red-500/10 border border-red-500/50 text-red-500 hover:bg-red-500/20 shadow-[0_0_15px_rgba(239,68,68,0.2)]',
    };

    const sizes = {
      sm: 'px-3 py-1.5 text-xs font-medium rounded-sm',
      md: 'px-5 py-2.5 text-sm font-semibold rounded-md tracking-wider uppercase',
      lg: 'px-8 py-3.5 text-base font-bold rounded-lg tracking-widest uppercase',
      xl: 'px-10 py-4.5 text-lg font-black rounded-xl tracking-[0.2em] uppercase',
    };

    return (
      <motion.button
        ref={ref}
        whileHover={{ scale: 1.02, translateY: -1 }}
        whileTap={{ scale: 0.98 }}
        className={cn(
          'relative overflow-hidden inline-flex items-center justify-center transition-colors focus:outline-none disabled:opacity-50 disabled:pointer-events-none font-heading',
          variants[variant],
          sizes[size],
          className
        )}
        {...props}
      >
        {isLoading ? (
          <div className="flex items-center space-x-2">
            <div className="w-4 h-4 border-2 border-current border-t-transparent rounded-full animate-spin" />
            <span>Loading...</span>
          </div>
        ) : (
          <>
            {icon && <span className="mr-2">{icon}</span>}
            <span className="relative z-10">{children}</span>
          </>
        )}
        
        {/* Hover Shine Effect */}
        <motion.div
          initial={{ x: '-100%' }}
          whileHover={{ x: '100%' }}
          transition={{ duration: 0.6, ease: 'easeInOut' }}
          className="absolute inset-0 bg-gradient-to-r from-transparent via-white/10 to-transparent pointer-events-none"
        />
      </motion.button>
    );
  }
);

Button.displayName = 'Button';

export { Button };
