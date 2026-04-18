import { useState, useEffect } from 'react';
import { Link, useLocation } from 'react-router-dom';
import './Header.css';

const Header = () => {
    const [isScrolled, setIsScrolled] = useState(false);
    const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
    const location = useLocation();

    useEffect(() => {
        const handleScroll = () => {
            setIsScrolled(window.scrollY > 50);
        };
        window.addEventListener('scroll', handleScroll);
        return () => window.removeEventListener('scroll', handleScroll);
    }, []);

    const navLinks = [
        { path: '/', label: 'Home' },
        { path: '/members', label: 'Members' },
        { path: '/events', label: 'Events' },
        { path: '/contact', label: 'Contact Us' },
    ];

    return (
        <header className={`header ${isScrolled ? 'scrolled' : ''}`}>
            <div className="header-container">
                <Link to="/" className="logo">
                    <div className="logo-icon">
                        <span className="logo-k">K</span>
                        <span className="logo-k">K</span>
                        <span className="logo-c">C</span>
                    </div>
                    <div className="logo-text">
                        <span className="logo-name">Kalam Knowledge Club</span>
                        <span className="logo-tagline">Igniting Minds, Inspiring Futures</span>
                    </div>
                </Link>

                <nav className={`nav ${isMobileMenuOpen ? 'nav-open' : ''}`}>
                    {navLinks.map((link) => (
                        <Link
                            key={link.path}
                            to={link.path}
                            className={`nav-link ${location.pathname === link.path ? 'active' : ''}`}
                            onClick={() => setIsMobileMenuOpen(false)}
                        >
                            {link.label}
                            <span className="nav-link-underline"></span>
                        </Link>
                    ))}
                </nav>

                <button
                    className={`mobile-menu-btn ${isMobileMenuOpen ? 'open' : ''}`}
                    onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
                    aria-label="Toggle menu"
                >
                    <span></span>
                    <span></span>
                    <span></span>
                </button>
            </div>
        </header>
    );
};

export default Header;
