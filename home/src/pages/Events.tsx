import { useState } from 'react';
import './Page.css';

interface Event {
    id: number;
    title: string;
    date: string;
    time: string;
    description: string;
    fullDetails: string;
    location: string;
    status: 'Upcoming' | 'Completed';
    category: string;
    attendees?: number;
}

const Events = () => {
    const [selectedEvent, setSelectedEvent] = useState<Event | null>(null);
    const [filterStatus, setFilterStatus] = useState<'All' | 'Upcoming' | 'Completed'>('All');

    const allEvents: Event[] = [
        {
            id: 1,
            title: 'Tech Talk 2025: AI & Machine Learning',
            date: 'March 15, 2025',
            time: '3:00 PM - 5:00 PM',
            description: 'A comprehensive session on emerging AI technologies and their real-world applications.',
            fullDetails: 'Join us for an in-depth discussion on Artificial Intelligence and Machine Learning. Industry experts will share insights on current trends, future possibilities, and career opportunities in AI.',
            location: 'Main Auditorium',
            status: 'Upcoming',
            category: 'Technical',
            attendees: 250
        },
        {
            id: 2,
            title: 'Hackathon v3.0',
            date: 'April 20-21, 2025',
            time: '9:00 AM - 9:00 PM',
            description: '24-hour coding challenge to solve real-world problems and showcase innovation.',
            fullDetails: 'A 24-hour hackathon where students form teams to conceptualize, develop, and present innovative solutions to real-world problems. Cash prizes and internship opportunities await the winners!',
            location: 'Innovation Lab',
            status: 'Upcoming',
            category: 'Competition',
            attendees: 300
        },
        {
            id: 3,
            title: 'Science Exhibition & Innovation Showcase',
            date: 'January 10, 2025',
            time: '10:00 AM - 4:00 PM',
            description: 'Showcasing innovative projects by students from across the college.',
            fullDetails: 'An annual event where students display their innovative projects, research work, and technical achievements. Experts from various fields evaluate and recognize outstanding contributions.',
            location: 'Central Field',
            status: 'Completed',
            category: 'Exhibition',
            attendees: 450
        },
        {
            id: 4,
            title: 'Web Development Workshop',
            date: 'February 28, 2025',
            time: '2:00 PM - 6:00 PM',
            description: 'Hands-on workshop on modern web development technologies and frameworks.',
            fullDetails: 'Learn the latest web development practices including React, Node.js, and cloud deployment. This interactive workshop covers frontend, backend, and full-stack development.',
            location: 'Computer Lab 101',
            status: 'Upcoming',
            category: 'Workshop',
            attendees: 120
        },
        {
            id: 5,
            title: 'Career Connect - Industry Interaction',
            date: 'February 15, 2025',
            time: '4:00 PM - 6:00 PM',
            description: 'Meet and interact with industry professionals from leading tech companies.',
            fullDetails: 'A networking event where students can interact with professionals from companies like Google, Microsoft, and Amazon. Learn about career paths, internships, and job opportunities.',
            location: 'Conference Hall',
            status: 'Upcoming',
            category: 'Networking',
            attendees: 200
        },
        {
            id: 6,
            title: 'Entrepreneurship Summit 2024',
            date: 'January 5, 2025',
            time: '9:00 AM - 5:00 PM',
            description: 'A full-day summit showcasing entrepreneurial ventures and startup ecosystem.',
            fullDetails: 'Join founders, investors, and mentors in this comprehensive summit about building and scaling startups. Includes keynote speeches, panel discussions, and networking sessions.',
            location: 'Grand Convention Center',
            status: 'Completed',
            category: 'Summit',
            attendees: 600
        },
    ];

    const filteredEvents = filterStatus === 'All' 
        ? allEvents 
        : allEvents.filter(event => event.status === filterStatus);

    const upcomingCount = allEvents.filter(e => e.status === 'Upcoming').length;
    const completedCount = allEvents.filter(e => e.status === 'Completed').length;

    return (
        <div className="page-container section">
            <div className="container">
                <h1 className="section-title fade-in-up">Our <span className="gradient-text-alt">Events</span></h1>
                <p className="section-subtitle fade-in-up">Join us for exciting workshops, hackathons, seminars, and networking opportunities.</p>

                {/* Stats */}
                <div className="events-stats">
                    <div className="stat-box fade-in-up">
                        <div className="stat-number">{upcomingCount}</div>
                        <div className="stat-text">Upcoming Events</div>
                    </div>
                    <div className="stat-box fade-in-up" style={{ animationDelay: '0.1s' }}>
                        <div className="stat-number">{completedCount}</div>
                        <div className="stat-text">Completed Events</div>
                    </div>
                    <div className="stat-box fade-in-up" style={{ animationDelay: '0.2s' }}>
                        <div className="stat-number">{allEvents.reduce((sum, e) => sum + (e.attendees || 0), 0)}</div>
                        <div className="stat-text">Total Attendees</div>
                    </div>
                </div>

                {/* Filter Buttons */}
                <div className="events-filter fade-in-up">
                    {(['All', 'Upcoming', 'Completed'] as const).map((status) => (
                        <button
                            key={status}
                            className={`filter-btn ${filterStatus === status ? 'active' : ''}`}
                            onClick={() => setFilterStatus(status)}
                        >
                            {status}
                        </button>
                    ))}
                </div>

                {/* Events List */}
                <div className="events-list">
                    {filteredEvents.length > 0 ? (
                        filteredEvents.map((event, index) => (
                            <div 
                                key={event.id} 
                                className="glass-card event-card fade-in-up" 
                                style={{ animationDelay: `${index * 0.15}s` }}
                                onClick={() => setSelectedEvent(event)}
                            >
                                <div className="event-date">
                                    <span className="date-day">{event.date.split(' ')[1].replace(',', '')}</span>
                                    <span className="date-month">{event.date.split(' ')[0]}</span>
                                </div>
                                <div className="event-details">
                                    <div className="event-header">
                                        <h3>{event.title}</h3>
                                        <span className={`event-status status-${event.status.toLowerCase()}`}>{event.status}</span>
                                    </div>
                                    <p className="event-time">⏰ {event.time}</p>
                                    <p className="event-location">📍 {event.location}</p>
                                    <p>{event.description}</p>
                                    <div className="event-meta">
                                        <span className="event-category">{event.category}</span>
                                        {event.attendees && <span className="event-attendees">👥 {event.attendees} attendees</span>}
                                    </div>
                                </div>
                                <button className="btn btn-secondary btn-sm">Details</button>
                            </div>
                        ))
                    ) : (
                        <div className="no-events">
                            <p>No events found for the selected filter.</p>
                        </div>
                    )}
                </div>

                {/* Event Details Modal */}
                {selectedEvent && (
                    <div className="event-modal-overlay" onClick={() => setSelectedEvent(null)}>
                        <div className="event-modal glass-card" onClick={(e) => e.stopPropagation()}>
                            <button className="modal-close" onClick={() => setSelectedEvent(null)}>✕</button>
                            <div className="modal-content">
                                <div className="modal-header">
                                    <h2>{selectedEvent.title}</h2>
                                    <span className={`event-status status-${selectedEvent.status.toLowerCase()}`}>{selectedEvent.status}</span>
                                </div>
                                <div className="modal-info">
                                    <div className="info-item">
                                        <span className="info-label">📅 Date:</span>
                                        <span>{selectedEvent.date}</span>
                                    </div>
                                    <div className="info-item">
                                        <span className="info-label">⏰ Time:</span>
                                        <span>{selectedEvent.time}</span>
                                    </div>
                                    <div className="info-item">
                                        <span className="info-label">📍 Location:</span>
                                        <span>{selectedEvent.location}</span>
                                    </div>
                                    <div className="info-item">
                                        <span className="info-label">🏷️ Category:</span>
                                        <span>{selectedEvent.category}</span>
                                    </div>
                                    {selectedEvent.attendees && (
                                        <div className="info-item">
                                            <span className="info-label">👥 Expected Attendees:</span>
                                            <span>{selectedEvent.attendees}</span>
                                        </div>
                                    )}
                                </div>
                                <p className="modal-description">{selectedEvent.fullDetails}</p>
                                {selectedEvent.status === 'Upcoming' && (
                                    <a href="/contact" className="btn btn-primary">Register Interest</a>
                                )}
                            </div>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

export default Events;
