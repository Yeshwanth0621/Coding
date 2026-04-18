import React from 'react';
import { motion, HTMLMotionProps } from 'framer-motion';
import { cn } from '../../lib/utils';

interface CardProps extends HTMLMotionProps<"div"> {
  variant?: 'default' | 'glass' | 'neon' | 'accent';
  padding?: 'none' | 'sm' | 'md' | 'lg' | 'xl';
  hoverEffect?: boolean;
}

const Card = React.forwardRef<HTMLDivElement, CardProps>(
  ({ className, variant = 'glass', padding = 'md', hoverEffect = true, children, ...props }, ref) => {
    const variants = {
      default: 'bg-zinc-900/50 border border-zinc-800',
      glass: 'glass-panel',
      neon: 'glass-panel border-neon-cyan/30 shadow-[0_0_20px_rgba(0,240,255,0.05)]',
      accent: 'glass-panel border-neon-magenta/30 shadow-[0_0_20px_rgba(255,0,110,0.05)]',
    };

    const paddings = {
      none: '',
      sm: 'p-3',
      md: 'p-5',
      lg: 'p-8',
      xl: 'p-12',
    };

    return (
      <motion.div
        ref={ref}
        initial={{ opacity: 0, y: 20 }}
        whileInView={{ opacity: 1, y: 0 }}
        viewport={{ once: true }}
        transition={{ duration: 0.5, ease: [0.23, 1, 0.32, 1] }}
        className={cn(
          'relative rounded-2xl overflow-hidden',
          variants[variant],
          paddings[padding],
          hoverEffect && 'transition-transform hover:-translate-y-1 hover:border-white/20',
          className
        )}
        {...props}
      >
        {/* Subtle Inner Glow */}
        <div className="absolute inset-0 bg-gradient-to-br from-white/5 to-transparent pointer-events-none" />
        
        {/* Content */}
        <div className="relative z-10">
          {children}
        </div>

        {/* Shine Animation on Hover */}
        {hoverEffect && (
          <div className="absolute inset-0 opacity-0 group-hover:opacity-100 transition-opacity duration-500 pointer-events-none overflow-hidden">
            <div className="absolute inset-0 bg-gradient-to-r from-transparent via-white/5 to-transparent -translate-x-full animate-[shimmer_2s_infinite]" />
          </div>
        )}
      </motion.div>
    );
  }
);

Card.displayName = 'Card';

export { Card };
