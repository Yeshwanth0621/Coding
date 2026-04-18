import { useEffect, useRef, useState } from 'react';
import './Home.css';

interface Achievement {
    icon: string;
    number: string;
    label: string;
    description: string;
}

interface Value {
    icon: string;
    title: string;
    description: string;
}

const Home = () => {
    const observerRef = useRef<IntersectionObserver | null>(null);
    const [achievementData] = useState<Achievement[]>([
        { icon: '🎓', number: '50+', label: 'Events Organized', description: 'Educational seminars, workshops, and conferences' },
        { icon: '👥', number: '1000+', label: 'Active Members', description: 'A thriving community of passionate learners' },
        { icon: '🏆', number: '25+', label: 'Awards Won', description: 'Recognizing excellence and innovation' },
        { icon: '📅', number: '5+', label: 'Years of Legacy', description: 'Building excellence since 2020' },
    ]);

    const [valuesData] = useState<Value[]>([
        { icon: '🎯', title: 'Innovation', description: 'Fostering creative thinking and technological advancement' },
        { icon: '💡', title: 'Excellence', description: 'Pursuing highest standards in everything we do' },
        { icon: '🤝', title: 'Collaboration', description: 'Building strong partnerships and teamwork' },
        { icon: '🌍', title: 'Impact', description: 'Creating positive change in society' },
    ]);

    useEffect(() => {
        observerRef.current = new IntersectionObserver(
            (entries) => {
                entries.forEach((entry) => {
                    if (entry.isIntersecting) {
                        entry.target.classList.add('visible');
                    }
                });
            },
            { threshold: 0.1 }
        );

        document.querySelectorAll('.fade-in-section').forEach((el) => {
            observerRef.current?.observe(el);
        });

        return () => observerRef.current?.disconnect();
    }, []);

    return (
        <div className="home">
            {/* Hero Section */}
            <section className="hero-section">
                <div className="bg-blob bg-blob-pink" style={{ top: '10%', left: '10%', width: '500px', height: '500px' }}></div>
                <div className="bg-blob bg-blob-blue" style={{ bottom: '10%', right: '10%', width: '600px', height: '600px' }}></div>

                <div className="container hero-content">
                    <div className="hero-text fade-in-up">
                        <h1 className="hero-title">
                            Welcome to <br />
                            <span className="gradient-text">Kalam Knowledge Club</span>
                        </h1>
                        <p className="hero-subtitle">
                            Empowering students through innovation, knowledge, and creativity.
                            Join us in our journey to explore the unknown and build the future.
                        </p>
                        <div className="hero-buttons">
                            <a href="#about" className="btn btn-primary">Discover More</a>
                            <a href="/contact" className="btn btn-secondary">Join Us</a>
                        </div>
                    </div>
                    <div className="hero-visual fade-in-up" style={{ animationDelay: '0.2s' }}>
                        <div className="glass-card hero-card">
                            <div className="logo-placeholder">
                                <span className="logo-icon-large">KKC</span>
                            </div>
                            <div className="hero-card-content">
                                <h3>Innovate. Create. Inspire.</h3>
                                <p>The official club for future leaders and innovators.</p>
                            </div>
                        </div>
                    </div>
                </div>
            </section>

            {/* About Section */}
            <section id="about" className="section about-section">
                <div className="container">
                    <div className="fade-in-section">
                        <h2 className="section-title">Who We Are</h2>
                        <p className="section-subtitle">
                            Kalam Knowledge Club is more than just a student organization. We are a community of dreamers, doers, and thinkers inspired by the vision of Dr. A.P.J. Abdul Kalam. Our mission is to foster intellectual growth, technological innovation, and social responsibility among students.
                        </p>
                    </div>

                    <div className="about-grid">
                        <div className="glass-card about-card fade-in-section">
                            <div className="card-icon">🎯</div>
                            <h3>Our Mission</h3>
                            <p>To foster a culture of continuous learning, technical excellence, and innovation among students, preparing them to be future leaders and change-makers.</p>
                        </div>
                        <div className="glass-card about-card fade-in-section" style={{ transitionDelay: '0.1s' }}>
                            <div className="card-icon">💡</div>
                            <h3>Our Vision</h3>
                            <p>To become a leading hub for innovation, knowledge, and student development, creating a platform where ideas transform into reality and impact society positively.</p>
                        </div>
                        <div className="glass-card about-card fade-in-section" style={{ transitionDelay: '0.2s' }}>
                            <div className="card-icon">🤝</div>
                            <h3>Our Values</h3>
                            <p>Integrity, Innovation, Collaboration, Continuous Learning, and Social Responsibility guide every action we take.</p>
                        </div>
                    </div>
                </div>
            </section>

            {/* Values Section */}
            <section className="section values-section">
                <div className="bg-blob bg-blob-yellow" style={{ top: '20%', right: '10%', width: '400px', height: '400px', opacity: 0.15 }}></div>
                <div className="container">
                    <div className="fade-in-section">
                        <h2 className="section-title">Our Core <span className="gradient-text-alt">Values</span></h2>
                        <p className="section-subtitle">
                            These principles define our character and guide our journey towards excellence.
                        </p>
                    </div>

                    <div className="values-grid">
                        {valuesData.map((value, index) => (
                            <div key={index} className="glass-card value-card fade-in-section" style={{ animationDelay: `${index * 0.1}s` }}>
                                <div className="value-icon">{value.icon}</div>
                                <h3>{value.title}</h3>
                                <p>{value.description}</p>
                            </div>
                        ))}
                    </div>
                </div>
            </section>

            {/* Accomplishments Section */}
            <section className="section accomplishments-section">
                <div className="bg-blob bg-blob-blue" style={{ top: '50%', left: '50%', transform: 'translate(-50%, -50%)', width: '800px', height: '800px', opacity: 0.1 }}></div>

                <div className="container">
                    <div className="fade-in-section">
                        <h2 className="section-title">Our <span className="gradient-text-alt">Accomplishments</span></h2>
                        <p className="section-subtitle">
                            Milestones that mark our journey of excellence, innovation, and impact on the student community.
                        </p>
                    </div>

                    <div className="stats-grid">
                        {achievementData.map((achievement, index) => (
                            <div key={index} className="stat-item fade-in-section" style={{ animationDelay: `${index * 0.15}s` }}>
                                <span className="stat-icon">{achievement.icon}</span>
                                <span className="stat-number gradient-text">{achievement.number}</span>
                                <span className="stat-label">{achievement.label}</span>
                                <span className="stat-description">{achievement.description}</span>
                            </div>
                        ))}
                    </div>
                </div>
            </section>

            {/* Features Section */}
            <section className="section features-section">
                <div className="bg-blob bg-blob-pink" style={{ bottom: '10%', left: '5%', width: '400px', height: '400px', opacity: 0.15 }}></div>
                <div className="container">
                    <div className="fade-in-section">
                        <h2 className="section-title">What We <span className="gradient-text-alt">Offer</span></h2>
                        <p className="section-subtitle">
                            A comprehensive platform for learning, growing, and making an impact.
                        </p>
                    </div>

                    <div className="features-grid">
                        <div className="glass-card feature-card fade-in-section">
                            <div className="feature-icon">📚</div>
                            <h3>Technical Workshops</h3>
                            <p>Learn cutting-edge technologies through hands-on workshops and seminars led by industry experts and experienced mentors.</p>
                        </div>
                        <div className="glass-card feature-card fade-in-section" style={{ animationDelay: '0.1s' }}>
                            <div className="feature-icon">🏆</div>
                            <h3>Competitions</h3>
                            <p>Participate in hackathons, coding competitions, and innovation challenges to test your skills and win exciting prizes.</p>
                        </div>
                        <div className="glass-card feature-card fade-in-section" style={{ animationDelay: '0.2s' }}>
                            <div className="feature-icon">💼</div>
                            <h3>Career Development</h3>
                            <p>Get guidance on resume building, interview preparation, and networking opportunities with leading companies.</p>
                        </div>
                        <div className="glass-card feature-card fade-in-section" style={{ animationDelay: '0.3s' }}>
                            <div className="feature-icon">🌐</div>
                            <h3>Networking Events</h3>
                            <p>Connect with industry leaders, alumni, and fellow students in an inclusive and supportive community.</p>
                        </div>
                        <div className="glass-card feature-card fade-in-section" style={{ animationDelay: '0.4s' }}>
                            <div className="feature-icon">💬</div>
                            <h3>Community Support</h3>
                            <p>Join a vibrant community where you can share ideas, collaborate on projects, and grow together.</p>
                        </div>
                        <div className="glass-card feature-card fade-in-section" style={{ animationDelay: '0.5s' }}>
                            <div className="feature-icon">🚀</div>
                            <h3>Innovation Hub</h3>
                            <p>Incubate your ideas and turn them into reality with resources, mentorship, and support from the club.</p>
                        </div>
                    </div>
                </div>
            </section>

            {/* CTA Section */}
            <section className="section cta-section">
                <div className="container">
                    <div className="glass-card cta-card fade-in-section">
                        <h2>Ready to be part of the change?</h2>
                        <p>Join Kalam Knowledge Club today and start your journey towards excellence, innovation, and impact.</p>
                        <div className="cta-buttons">
                            <a href="/contact" className="btn btn-primary">Get in Touch</a>
                            <a href="/members" className="btn btn-secondary">Meet Our Team</a>
                        </div>
                    </div>
                </div>
            </section>
        </div>
    );
};

export default Home;
