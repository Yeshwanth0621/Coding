import { useState } from 'react';
import emailjs from '@emailjs/browser';
import './Page.css';

interface FormData {
    name: string;
    email: string;
    subject: string;
    message: string;
}

const Contact = () => {
    const [formData, setFormData] = useState<FormData>({
        name: '',
        email: '',
        subject: '',
        message: ''
    });

    const [submitted, setSubmitted] = useState(false);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
        setError(null); // Clear error when user types
    };

    const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();

        if (!formData.name || !formData.email || !formData.message) {
            setError('Please fill in all required fields');
            return;
        }

        setIsSubmitting(true);
        setError(null);

        try {
            // Send email using EmailJS
            const result = await emailjs.send(
                import.meta.env.VITE_EMAILJS_SERVICE_ID,
                import.meta.env.VITE_EMAILJS_TEMPLATE_ID,
                {
                    from_name: formData.name,
                    reply_to: formData.email,
                    user_email: formData.email,
                    user_subject: formData.subject || 'General Inquiry',
                    message: formData.message,
                },
                import.meta.env.VITE_EMAILJS_PUBLIC_KEY
            );

            console.log('Email sent successfully:', result.text);
            setSubmitted(true);

            // Reset form after 5 seconds
            setTimeout(() => {
                setFormData({ name: '', email: '', subject: '', message: '' });
                setSubmitted(false);
            }, 5000);
        } catch (err) {
            console.error('Failed to send email:', err);
            setError('Failed to send message. Please try again or contact us directly.');
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="page-container section">
            <div className="container">
                <h1 className="section-title fade-in-up">Get in <span className="gradient-text">Touch</span></h1>
                <p className="section-subtitle fade-in-up">Have questions? We'd love to hear from you. Send us a message or reach out through any of our contact channels.</p>

                <div className="contact-wrapper fade-in-up" style={{ animationDelay: '0.2s' }}>
                    <form className="glass-card contact-form" onSubmit={handleSubmit}>
                        <div className="form-group">
                            <label htmlFor="name">Full Name *</label>
                            <input
                                type="text"
                                id="name"
                                name="name"
                                value={formData.name}
                                onChange={handleChange}
                                placeholder="Your Name"
                                required
                            />
                        </div>
                        <div className="form-group">
                            <label htmlFor="email">Email Address *</label>
                            <input
                                type="email"
                                id="email"
                                name="email"
                                value={formData.email}
                                onChange={handleChange}
                                placeholder="your.email@example.com"
                                required
                            />
                        </div>
                        <div className="form-group">
                            <label htmlFor="subject">Subject *</label>
                            <select
                                id="subject"
                                name="subject"
                                value={formData.subject}
                                onChange={handleChange}
                                required
                            >
                                <option value="">Select a subject</option>
                                <option value="general">General Inquiry</option>
                                <option value="membership">Membership</option>
                                <option value="events">Events & Workshops</option>
                                <option value="collaboration">Collaboration</option>
                                <option value="feedback">Feedback</option>
                                <option value="other">Other</option>
                            </select>
                        </div>
                        <div className="form-group">
                            <label htmlFor="message">Message *</label>
                            <textarea
                                id="message"
                                name="message"
                                rows={6}
                                value={formData.message}
                                onChange={handleChange}
                                placeholder="Tell us how we can help..."
                                required
                            ></textarea>
                        </div>
                        <button type="submit" className="btn btn-primary" disabled={submitted || isSubmitting}>
                            {isSubmitting ? 'Sending...' : submitted ? '✓ Message Sent!' : 'Send Message'}
                        </button>
                        {submitted && (
                            <div className="success-message">
                                <span>✓ Thank you for reaching out! We'll get back to you soon.</span>
                            </div>
                        )}
                        {error && (
                            <div className="error-message">
                                <span>⚠ {error}</span>
                            </div>
                        )}
                    </form>

                    <div className="contact-info glass-card">
                        <h3>Contact Information</h3>

                        <div className="info-section">
                            <h4>Office Location</h4>
                            <div className="info-item">
                                <span className="icon">📍</span>
                                <p>
                                    Kalam Knowledge Club<br />
                                    123 College Campus<br />
                                    University Road<br />
                                    City - 500001, India
                                </p>
                            </div>
                        </div>

                        <div className="info-section">
                            <h4>Communication</h4>
                            <div className="info-item">
                                <span className="icon">📧</span>
                                <p>
                                    <a href="mailto:contact@kalamknowledgeclub.com">contact@kalamknowledgeclub.com</a>
                                </p>
                            </div>
                            <div className="info-item">
                                <span className="icon">📱</span>
                                <p>
                                    <a href="tel:+919876543210">+91 98765 43210</a>
                                </p>
                            </div>
                        </div>

                        <div className="info-section">
                            <h4>Office Hours</h4>
                            <div className="info-item">
                                <span className="icon">🕐</span>
                                <p>
                                    Monday - Friday: 10:00 AM - 5:00 PM<br />
                                    Saturday: 2:00 PM - 6:00 PM<br />
                                    Sunday: Closed
                                </p>
                            </div>
                        </div>

                        <div className="info-section">
                            <h4>Follow Us</h4>
                            <div className="social-links">
                                <a href="#" className="social-icon" title="Instagram">
                                    <span>📷</span> Instagram
                                </a>
                                <a href="#" className="social-icon" title="LinkedIn">
                                    <span>💼</span> LinkedIn
                                </a>
                                <a href="#" className="social-icon" title="Twitter">
                                    <span>𝕏</span> Twitter
                                </a>
                                <a href="#" className="social-icon" title="Facebook">
                                    <span>👥</span> Facebook
                                </a>
                            </div>
                        </div>
                    </div>
                </div>

                {/* Quick Links Section */}
                <div className="quick-links-section fade-in-up">
                    <h2>Quick Links</h2>
                    <div className="quick-links">
                        <a href="/members" className="quick-link-card glass-card">
                            <div className="link-icon">👥</div>
                            <h3>Our Team</h3>
                            <p>Meet our dedicated team members</p>
                        </a>
                        <a href="/events" className="quick-link-card glass-card">
                            <div className="link-icon">📅</div>
                            <h3>Upcoming Events</h3>
                            <p>Check out our latest events</p>
                        </a>
                        <a href="#" className="quick-link-card glass-card">
                            <div className="link-icon">📚</div>
                            <h3>Resources</h3>
                            <p>Learning materials and guides</p>
                        </a>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Contact;
