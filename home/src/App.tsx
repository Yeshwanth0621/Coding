import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import Header from './components/Header';
import Home from './pages/Home';
import Members from './pages/Members';
import Events from './pages/Events';
import Contact from './pages/Contact';
import './App.css';

// ScrollToTop component to handle scroll position on route change
function ScrollToTop() {
  const { pathname } = useLocation();

  useEffect(() => {
    window.scrollTo(0, 0);
  }, [pathname]);

  return null;
}

function App() {
  return (
    <Router basename={import.meta.env.BASE_URL}>
      <ScrollToTop />
      <div className="app">
        <div className="bg-fixed-blob"></div>
        <Header />
        <main>
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/members" element={<Members />} />
            <Route path="/events" element={<Events />} />
            <Route path="/contact" element={<Contact />} />
          </Routes>
        </main>

        <footer className="footer">
          <div className="container">
            <p>&copy; {new Date().getFullYear()} Kalam Knowledge Club. All rights reserved.</p>
            <p className="footer-credits">Designed with <span style={{ color: 'var(--primary-pink)' }}>❤</span> by KKC Tech Team</p>
          </div>
        </footer>
      </div>
    </Router>
  );
}

export default App;
